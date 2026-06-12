package com.smart.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    isRound: Boolean = false,
    fontSize: Int = 22,
    fontWeight: FontWeight = FontWeight.Light
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f) // Ensure perfect proportional circles/squares
            .clip(if (isRound) CircleShape else RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            color = textColor
        )
    }
}
