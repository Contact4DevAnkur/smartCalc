package com.smart.calculator.model

data class HistoryItem(
    val id: String,
    val equation: String,
    val result: String,
    val timestamp: Long
)

data class CalculatorState(
    val equation: String = "",
    val result: String = "",
    val historyList: List<HistoryItem> = emptyList(),
    val isDarkMode: Boolean = true, // Default to clean dark mode as requested
    val isScientificOpen: Boolean = false,
    
    // Mudra (Currency) Converter States
    val mudraAmount: String = "",
    val mudraFromCurrency: String = "USD",
    val mudraToCurrency: String = "INR",
    val mudraConvertedAmount: String = "",
    val isMudraActive: Boolean = false // Toggle converter overlay panel
)
