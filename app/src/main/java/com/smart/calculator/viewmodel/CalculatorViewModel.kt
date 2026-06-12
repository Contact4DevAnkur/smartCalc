package com.smart.calculator.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.smart.calculator.model.CalculatorState
import com.smart.calculator.model.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    private val sharedPrefs = application.getSharedPreferences("smart_calc_prefs", Context.MODE_PRIVATE)

    init {
        loadHistory()
        loadTheme()
    }

    // Handles button clicks from Keypad
    fun onAction(action: CalculatorAction) {
        handleCalculatorAction(action)
    }

    private fun handleCalculatorAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> {
                val currentEquation = _state.value.equation
                // If there's an error string, clear it on new input
                val newEquation = if (_state.value.result == "Math Error" || _state.value.result == "Format Error") {
                    _state.value.equation
                } else {
                    currentEquation
                }

                _state.update {
                    it.copy(
                        equation = newEquation + action.number,
                        result = if (newEquation.isEmpty()) "" else it.result
                    )
                }
            }
            is CalculatorAction.Operation -> {
                val currentEq = _state.value.equation
                if (currentEq.isNotEmpty() && !isOperator(currentEq.last())) {
                    _state.update { it.copy(equation = currentEq + action.symbol) }
                } else if (currentEq.isEmpty() && action.symbol == "-") {
                    // Allow unary minus at start
                    _state.update { it.copy(equation = "-") }
                }
            }
            is CalculatorAction.Scientific -> {
                // Prepend scientific function name with open parentheses
                val funcName = when (action.function) {
                    ScientificFunction.SQRT -> "√("
                    ScientificFunction.SIN -> "sin("
                    ScientificFunction.COS -> "cos("
                    ScientificFunction.TAN -> "tan("
                    ScientificFunction.LN -> "ln("
                    ScientificFunction.LOG -> "log("
                    ScientificFunction.POW -> "^"
                    ScientificFunction.MOD -> "%"
                }
                _state.update { it.copy(equation = it.equation + funcName) }
            }
            is CalculatorAction.Constant -> {
                val constantStr = when (action.constant) {
                    MathConstant.PI -> "π"
                    MathConstant.E -> "e"
                }
                _state.update { it.copy(equation = it.equation + constantStr) }
            }
            is CalculatorAction.Clear -> {
                _state.update { it.copy(equation = "", result = "") }
            }
            is CalculatorAction.Delete -> {
                val eq = _state.value.equation
                if (eq.isNotEmpty()) {
                    // If deleting a multi-char trig function
                    val parsedEq = when {
                        eq.endsWith("sin(") || eq.endsWith("cos(") || eq.endsWith("tan(") || eq.endsWith("log(") -> eq.dropLast(4)
                        eq.endsWith("ln(") -> eq.dropLast(3)
                        eq.endsWith("√(") -> eq.dropLast(2)
                        else -> eq.dropLast(1)
                    }
                    _state.update { it.copy(equation = parsedEq, result = "") }
                }
            }
            is CalculatorAction.Parentheses -> {
                val eq = _state.value.equation
                val openCount = eq.count { it == '(' }
                val closeCount = eq.count { it == ')' }
                val charToAppend = if (openCount > closeCount && eq.isNotEmpty() && eq.last().isDigit()) ")" else "("
                _state.update { it.copy(equation = eq + charToAppend) }
            }
            is CalculatorAction.Decimal -> {
                val eq = _state.value.equation
                if (eq.isEmpty() || !hasDecimalInLastNumber(eq)) {
                    _state.update { it.copy(equation = eq + ".") }
                }
            }
            is CalculatorAction.Calculate -> {
                evaluateEquation()
            }
        }
    }

    private fun isOperator(c: Char): Boolean {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '%' || c == '×' || c == '÷'
    }

    private fun hasDecimalInLastNumber(expr: String): Boolean {
        var i = expr.length - 1
        while (i >= 0) {
            val c = expr[i]
            if (isOperator(c) || c == '(' || c == ')') {
                break
            }
            if (c == '.') {
                return true
            }
            i--
        }
        return false
    }

    private fun evaluateEquation() {
        val eq = _state.value.equation
        if (eq.isEmpty()) return

        try {
            // Normalize display factors for standard processing
            val normalizedExpression = eq
                .replace("×", "*")
                .replace("÷", "/")
                // Apply smart implicit multiplication insertion:
                // 1) Digit, decimal point, constant, or closing parenthesis followed by math function, constant, or opening parenthesis
                .replace(Regex("(\\d|\\.|π|e|\\))(?=[a-zA-Z√πe\\(])"), "$1*")
                // 2) Moving constants or closing parenthesis followed by a digit
                .replace(Regex("([πe\\)])(?=\\d)"), "$1*")
                .replace("π", "3.141592653589793")
                .replace("e", "2.718281828459045")
                // Handle balancing parentheses implicitly
                .let { expr ->
                    val openCount = expr.count { it == '(' }
                    val closeCount = expr.count { it == ')' }
                    expr + ")".repeat(Math.max(0, openCount - closeCount))
                }

            val resultValue = ExpressionParser(normalizedExpression).parse()
            val formattedResult = formatResult(resultValue)

            val newHistoryItem = HistoryItem(
                id = UUID.randomUUID().toString(),
                equation = eq,
                result = formattedResult,
                timestamp = System.currentTimeMillis()
            )

            val updatedHistory = listOf(newHistoryItem) + _state.value.historyList
            _state.update { it.copy(result = formattedResult, historyList = updatedHistory) }
            saveHistory(updatedHistory)

        } catch (e: ArithmeticException) {
            _state.update { it.copy(result = "Math Error") }
        } catch (e: Exception) {
            _state.update { it.copy(result = "Format Error") }
        }
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN()) return "Math Error"
        if (value.isInfinite()) return "Math Error"

        var actualValue = value
        if (actualValue == -0.0) {
            actualValue = 0.0
        }

        // If it's effectively an integer
        if (actualValue == actualValue.toLong().toDouble()) {
            return actualValue.toLong().toString()
        }

        // Limit floating calculations to clean format
        val str = "%.6f".format(actualValue).replace(",", ".")
        // Trim trailing zeros
        var result = str
        while (result.contains(".") && (result.endsWith("0") || result.endsWith("."))) {
            result = result.dropLast(1)
        }
        return result
    }

    fun toggleScientificPanel() {
        _state.update { it.copy(isScientificOpen = !it.isScientificOpen) }
    }

    fun toggleDarkMode() {
        val newTheme = !_state.value.isDarkMode
        _state.update { it.copy(isDarkMode = newTheme) }
        sharedPrefs.edit().putBoolean("dark_mode_enabled", newTheme).apply()
    }

    fun clearHistory() {
        _state.update { it.copy(historyList = emptyList()) }
        saveHistory(emptyList())
    }

    private fun saveHistory(history: List<HistoryItem>) {
        val serialized = history.joinToString(";") { "${it.id}|${it.equation}|${it.result}|${it.timestamp}" }
        sharedPrefs.edit().putString("calc_history", serialized).apply()
    }

    private fun loadHistory() {
        val raw = sharedPrefs.getString("calc_history", null) ?: return
        if (raw.isEmpty()) return

        try {
            val items = raw.split(";").mapNotNull {
                val parts = it.split("|")
                if (parts.size == 4) {
                    HistoryItem(
                        id = parts[0],
                        equation = parts[1],
                        result = parts[2],
                        timestamp = parts[3].toLongOrNull() ?: 0L
                    )
                } else null
            }
            _state.update { it.copy(historyList = items) }
        } catch (e: Exception) {
            // Fallback for corrupted cache
        }
    }

    private fun loadTheme() {
        val systemUsesDark = true // Default as requested
        val savedTheme = sharedPrefs.getBoolean("dark_mode_enabled", systemUsesDark)
        _state.update { it.copy(isDarkMode = savedTheme) }
    }
}

// Actions definitions
sealed class CalculatorAction {
    data class Number(val number: String) : CalculatorAction()
    data class Operation(val symbol: String) : CalculatorAction()
    data class Scientific(val function: ScientificFunction) : CalculatorAction()
    data class Constant(val constant: MathConstant) : CalculatorAction()
    object Calculate : CalculatorAction()
    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    object Parentheses : CalculatorAction()
    object Decimal : CalculatorAction()
}

enum class ScientificFunction {
    SQRT, SIN, COS, TAN, LN, LOG, POW, MOD
}

enum class MathConstant {
    PI, E
}

// Recursive Descent Expression Parser
private class ExpressionParser(private val expression: String) {
    private var pos = -1
    private var ch = 0

    private fun nextChar() {
        pos++
        ch = if (pos < expression.length) expression[pos].code else -1
    }

    private fun eat(charToEat: Int): Boolean {
        while (ch == ' '.code) nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    fun parse(): Double {
        nextChar()
        val x = parseExpression()
        if (pos < expression.length) throw RuntimeException("Unexpected expression character: " + ch.toChar())
        return x
    }

    private fun parseExpression(): Double {
        var x = parseTerm()
        while (true) {
            if (eat('+'.code)) {
                x += parseTerm() // add
            } else if (eat('-'.code)) {
                x -= parseTerm() // subtract
            } else {
                return x
            }
        }
    }

    private fun parseTerm(): Double {
        var x = parseFactor()
        while (true) {
            if (eat('*'.code) || eat('x'.code) || eat('×'.code)) {
                x *= parseFactor() // multiply
            } else if (eat('/'.code) || eat('÷'.code)) {
                val nextFactor = parseFactor()
                if (nextFactor == 0.0) throw ArithmeticException("Division by zero")
                x /= nextFactor // divide
            } else if (eat('%'.code)) {
                x %= parseFactor() // mod
            } else {
                return x
            }
        }
    }

    private fun parseFactor(): Double {
        if (eat('+'.code)) return parseFactor() // unary plus
        if (eat('-'.code)) return -parseFactor() // unary minus

        var x: Double
        val startPos = this.pos
        if (eat('('.code)) { // parenthesis grouping
            x = parseExpression()
            eat(')'.code)
        } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numeric constants
            while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
            x = expression.substring(startPos, this.pos).toDouble()
        } else if (ch >= 'a'.code && ch <= 'z'.code || ch == '√'.code || ch == 'π'.code || ch == 'e'.code) { // math variables or functions
            while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
            val parsedWord = expression.substring(startPos, this.pos)
            if (parsedWord == "e") {
                x = Math.E
            } else if (parsedWord == "pi" || parsedWord == "π") {
                x = Math.PI
            } else if (parsedWord == "√" || (parsedWord.isEmpty() && ch == '√'.code)) {
                if (parsedWord.isEmpty()) nextChar() // Move past √
                x = parseFactor()
                if (x < 0) throw ArithmeticException("Square root of a negative value")
                x = Math.sqrt(x)
            } else {
                x = parseFactor()
                x = when (parsedWord) {
                    "sin" -> Math.sin(Math.toRadians(x))
                    "cos" -> Math.cos(Math.toRadians(x))
                    "tan" -> Math.tan(Math.toRadians(x))
                    "ln" -> Math.log(x)
                    "log" -> Math.log10(x)
                    else -> throw RuntimeException("Unsupported function: $parsedWord")
                }
            }
        } else {
            throw RuntimeException("Unexpected expression character: " + ch.toChar())
        }

        if (eat('^'.code)) x = Math.pow(x, parseFactor()) // power of

        return x
    }
}
