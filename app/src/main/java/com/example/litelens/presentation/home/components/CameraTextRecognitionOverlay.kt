package com.example.litelens.presentation.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CameraTextRecognitionOverlay(
    boxWidthPercentage: Float,
    boxHeightPercentage: Float,
    modifier: Modifier = Modifier,
    text: String
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Calculate the dimensions for the bounding box
            val width = size.width * boxWidthPercentage
            val height = size.height * boxHeightPercentage
            val left = (size.width - width) / 2
            val top = (size.height - height) / 2

            // Draw the bounding box with rounded corners
            drawRoundRect(
                color = Color.White.copy(alpha = 0.4f),  // Semi-transparent white
                topLeft = Offset(left, top),
                size = Size(width, height),
                cornerRadius = CornerRadius(16.dp.toPx()),
                style = Stroke(width = 4.dp.toPx())  // Outline stroke
            )
        }

        // Display instruction text above the bounding box
        Text(
            text = text,
            style = TextStyle(
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 148.dp)  // Adjust this value to position the text appropriately
                .padding(horizontal = 16.dp)
        )

    }
}