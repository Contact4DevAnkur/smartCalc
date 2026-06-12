package com.smart.calculator.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.smart.calculator.ui.screens.CalculatorScreen
import com.smart.calculator.ui.theme.SmartCalculatorTheme
import com.smart.calculator.viewmodel.CalculatorViewModel
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.state.collectAsState()

            SmartCalculatorTheme(darkTheme = state.isDarkMode) {
                Surface(
                    modifier = Modifier.Companion.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalculatorScreen(viewModel = viewModel)
                }
            }
        }
    }
}