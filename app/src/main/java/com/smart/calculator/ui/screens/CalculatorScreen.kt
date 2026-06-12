package com.smart.calculator.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.multidex.BuildConfig
import com.smart.calculator.model.HistoryItem
import com.smart.calculator.ui.components.CalculatorButton
import com.smart.calculator.ui.theme.MathGreen
import com.smart.calculator.viewmodel.CalculatorAction
import com.smart.calculator.viewmodel.CalculatorViewModel
import com.smart.calculator.viewmodel.MathConstant
import com.smart.calculator.viewmodel.ScientificFunction

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var showHistoryDropdown by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Log.d("TAG", "CalculatorScreen: ${BuildConfig.VERSION_NAME} , ${BuildConfig.VERSION_CODE}")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // App Header & Toggles
        HeaderSection(
            isDarkMode = state.isDarkMode,
            isScientificActive = state.isScientificOpen,
            onToggleDarkTheme = { viewModel.toggleDarkMode() },
            onToggleScientific = { viewModel.toggleScientificPanel() },
            onToggleHistory = { showHistoryDropdown = !showHistoryDropdown }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // History Log - Collapsible Dropdown Drawer
        AnimatedVisibility(
            visible = showHistoryDropdown,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            HistoryPanel(
                historyList = state.historyList,
                onClearHistory = { viewModel.clearHistory() },
                onSelectHistory = { item ->
                    // Set historical equation as current
                    viewModel.onAction(CalculatorAction.Clear)
                    for (char in item.equation) {
                        viewModel.onAction(CalculatorAction.Number(char.toString()))
                    }
                    showHistoryDropdown = false
                }
            )
        }

        // Display panel for standard outputs
        DisplaySection(
            equation = state.equation,
            result = state.result,
            modifier = Modifier
                .heightIn(min = 150.dp, max = 220.dp)
                .fillMaxWidth()
        )

        // Custom Scientific Function Extensions
        AnimatedVisibility(
            visible = state.isScientificOpen,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            ScientificSection(onAction = { viewModel.onAction(it) })
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Basic Math Keypad Grid
        KeypadSection(
            onAction = { viewModel.onAction(it) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun HeaderSection(
    isDarkMode: Boolean,
    isScientificActive: Boolean,
    onToggleDarkTheme: () -> Unit,
    onToggleScientific: () -> Unit,
    onToggleHistory: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glowing status bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MathGreen)
            )
            Column {
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "MINIMAL EDITION",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 1.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Persistent History Drawer Toggle
            IconButton(onClick = onToggleHistory) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = "Show history",
                    tint = MathGreen
                )
            }

            // Scientific Advanced Keys Expand Trigger
            IconButton(onClick = onToggleScientific) {
                Icon(
                    imageVector = Icons.Rounded.Calculate,
                    contentDescription = "Scientific keyboard",
                    tint = if (isScientificActive) MathGreen else MaterialTheme.colorScheme.onSurface
                )
            }

            // Theme Switcher Click Action
            IconButton(onClick = onToggleDarkTheme) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                    contentDescription = "Change Theme",
                    tint = MathGreen
                )
            }
        }
    }
}

@Composable
fun DisplaySection(
    equation: String,
    result: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
            .padding(18.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Large Running Equation Text
            Text(
                text = equation.ifEmpty { "0" },
                fontSize = if (equation.length > 15) 24.sp else 34.sp,
                textAlign = TextAlign.Right,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 40.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Calculated real-time Result
            if (result.isNotEmpty()) {
                val displayAnnotatedString = buildAnnotatedString {
                    val parts = result.split(".")
                    if (parts.size == 2) {
                        append(parts[0])
                        withStyle(style = SpanStyle(color = MathGreen)) {
                            append(".")
                        }
                        append(parts[1])
                    } else {
                        append(result)
                    }
                }

                Text(
                    text = displayAnnotatedString,
                    fontSize = if (result.length > 8) 64.sp else 84.sp,
                    textAlign = TextAlign.Right,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Thin,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = if (result.length > 8) 72.sp else 94.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


@Composable
fun HistoryPanel(
    historyList: List<HistoryItem>,
    onClearHistory: () -> Unit,
    onSelectHistory: (HistoryItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HISTORY LOG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 2.sp
                )

                if (historyList.isNotEmpty()) {
                    Text(
                        text = "CLEAR ALL HISTORY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color(0xFFFF0055),
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onClearHistory() }
                            .padding(4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "History is clean. Keep calculating!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Light
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(historyList, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectHistory(item) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.equation,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1.5f),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Light
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "= ${item.result}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = MathGreen,
                                maxLines = 1,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    }
                }
            }
        }
    }
}


@Composable
fun ScientificSection(
    onAction: (CalculatorAction) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "SCIENTIFIC FUNCTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MathGreen,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Scientific functions laid out in responsive 4-column rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(
                    text = "√",
                    onClick = { onAction(CalculatorAction.Scientific(ScientificFunction.SQRT)) },
                    modifier = Modifier.weight(1f),
                    backgroundColor = com.smart.calculator.ui.theme.DeepDarkBlack,
                    textColor = MathGreen,
                    fontWeight = FontWeight.Light
                )
                CalculatorButton(
                    text = "sin",
                    onClick = { onAction(CalculatorAction.Scientific(ScientificFunction.SIN)) },
                    modifier = Modifier.weight(1f),
                    backgroundColor = com.smart.calculator.ui.theme.DeepDarkBlack,
                    textColor = MathGreen,
                    fontWeight = FontWeight.Light
                )
                CalculatorButton(
                    text = "cos",
                    onClick = { onAction(CalculatorAction.Scientific(ScientificFunction.COS)) },
                    modifier = Modifier.weight(1f),
                    backgroundColor = com.smart.calculator.ui.theme.DeepDarkBlack,
                    textColor = MathGreen,
                    fontWeight = FontWeight.Light
                )
                CalculatorButton(
                    text = "tan",
                    onClick = { onAction(CalculatorAction.Scientific(ScientificFunction.TAN)) },
                    modifier = Modifier.weight(1f),
                    backgroundColor = com.smart.calculator.ui.theme.DeepDarkBlack,
                    textColor = MathGreen,
                    fontWeight = FontWeight.Light
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(
                    text = "^",
                    onClick = { onAction(CalculatorAction.Scientific(ScientificFunction.POW)) },
                    modifier = Modifier.weight(1f),
                    backgroundColor = com.smart.calculator.ui.theme.DeepDarkBlack,
                    textColor = MathGreen,
                    fontWeight = FontWeight.Light
                )
                CalculatorButton(
                    text = "ln",
                    onClick = { onAction(CalculatorAction.Scientific(ScientificFunction.LN)) },
                    modifier = Modifier.weight(1f),
                    backgroundColor = com.smart.calculator.ui.theme.DeepDarkBlack,
                    textColor = MathGreen,
                    fontWeight = FontWeight.Light
                )
                CalculatorButton(
                    text = "log",
                    onClick = { onAction(CalculatorAction.Scientific(ScientificFunction.LOG)) },
                    modifier = Modifier.weight(1f),
                    backgroundColor = com.smart.calculator.ui.theme.DeepDarkBlack,
                    textColor = MathGreen,
                    fontWeight = FontWeight.Light
                )
                CalculatorButton(
                    text = "mod",
                    onClick = { onAction(CalculatorAction.Scientific(ScientificFunction.MOD)) },
                    modifier = Modifier.weight(1f),
                    backgroundColor = com.smart.calculator.ui.theme.DeepDarkBlack,
                    textColor = MathGreen,
                    fontWeight = FontWeight.Light
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(
                    text = "π",
                    onClick = { onAction(CalculatorAction.Constant(MathConstant.PI)) },
                    modifier = Modifier.weight(1f),
                    backgroundColor = com.smart.calculator.ui.theme.DeepDarkBlack,
                    textColor = MathGreen,
                    fontWeight = FontWeight.Light
                )
                CalculatorButton(
                    text = "e",
                    onClick = { onAction(CalculatorAction.Constant(MathConstant.E)) },
                    modifier = Modifier.weight(1f),
                    backgroundColor = com.smart.calculator.ui.theme.DeepDarkBlack,
                    textColor = MathGreen,
                    fontWeight = FontWeight.Light
                )
                Spacer(modifier = Modifier.weight(2f)) // keep alignments centered
            }
        }
    }
}

@Composable
fun KeypadSection(
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: AC, ( ), Delete/Backspace, Division
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                text = "AC",
                onClick = { onAction(CalculatorAction.Clear) },
                modifier = Modifier.weight(1f),
                backgroundColor = com.smart.calculator.ui.theme.SecondaryBlack,
                textColor = androidx.compose.ui.graphics.Color(0xFFFF0055),
                fontWeight = FontWeight.Light
            )
            CalculatorButton(
                text = "( )",
                onClick = { onAction(CalculatorAction.Parentheses) },
                modifier = Modifier.weight(1f),
                backgroundColor = com.smart.calculator.ui.theme.SecondaryBlack,
                textColor = MathGreen,
                fontSize = 22,
                fontWeight = FontWeight.Light
            )
            // Backspace icon button custom modeled inside the wrapper
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(com.smart.calculator.ui.theme.SecondaryBlack)
                    .clickable { onAction(CalculatorAction.Delete) }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Backspace,
                    contentDescription = "Backspace",
                    tint = MathGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
            CalculatorButton(
                text = "÷",
                onClick = { onAction(CalculatorAction.Operation("÷")) },
                modifier = Modifier.weight(1f),
                backgroundColor = com.smart.calculator.ui.theme.SecondaryBlack,
                textColor = MathGreen,
                fontWeight = FontWeight.Light
            )
        }

        // Row 2: 7, 8, 9, Multiplications
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                text = "7",
                onClick = { onAction(CalculatorAction.Number("7")) },
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Light
            )
            CalculatorButton(
                text = "8",
                onClick = { onAction(CalculatorAction.Number("8")) },
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Light
            )
            CalculatorButton(
                text = "9",
                onClick = { onAction(CalculatorAction.Number("9")) },
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Light
            )
            CalculatorButton(
                text = "×",
                onClick = { onAction(CalculatorAction.Operation("×")) },
                modifier = Modifier.weight(1f),
                backgroundColor = com.smart.calculator.ui.theme.SecondaryBlack,
                textColor = MathGreen,
                fontWeight = FontWeight.Light
            )
        }

        // Row 3: 4, 5, 6, Subtraction
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                text = "4",
                onClick = { onAction(CalculatorAction.Number("4")) },
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Light
            )
            CalculatorButton(
                text = "5",
                onClick = { onAction(CalculatorAction.Number("5")) },
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Light
            )
            CalculatorButton(
                text = "6",
                onClick = { onAction(CalculatorAction.Number("6")) },
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Light
            )
            CalculatorButton(
                text = "-",
                onClick = { onAction(CalculatorAction.Operation("-")) },
                modifier = Modifier.weight(1f),
                backgroundColor = com.smart.calculator.ui.theme.SecondaryBlack,
                textColor = MathGreen,
                fontWeight = FontWeight.Light
            )
        }

        // Row 4: 1, 2, 3, Addition
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                text = "1",
                onClick = { onAction(CalculatorAction.Number("1")) },
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Light
            )
            CalculatorButton(
                text = "2",
                onClick = { onAction(CalculatorAction.Number("2")) },
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Light
            )
            CalculatorButton(
                text = "3",
                onClick = { onAction(CalculatorAction.Number("3")) },
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Light
            )
            CalculatorButton(
                text = "+",
                onClick = { onAction(CalculatorAction.Operation("+")) },
                modifier = Modifier.weight(1f),
                backgroundColor = com.smart.calculator.ui.theme.SecondaryBlack,
                textColor = MathGreen,
                fontWeight = FontWeight.Light
            )
        }

        // Row 5: 0, Decimal, Equals
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalculatorButton(
                text = "0",
                onClick = { onAction(CalculatorAction.Number("0")) },
                modifier = Modifier.weight(1f), // Double wide aspect
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Light
            )
            CalculatorButton(
                text = ".",
                onClick = { onAction(CalculatorAction.Decimal) },
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                textColor = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Light
            )
            CalculatorButton(
                text = "=",
                onClick = { onAction(CalculatorAction.Calculate) },
                modifier = Modifier.weight(1f),
                backgroundColor = MathGreen,
                textColor = androidx.compose.ui.graphics.Color.Black,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
