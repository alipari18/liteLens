package com.example.litelens.presentation.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    text: String,
    isLoading: Boolean
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (!isLoading) {
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
                    .offset(y = 250.dp)  // Adjust this value to position the text appropriately
                    .padding(horizontal = 16.dp)
            )
        } else {
            PulsatingLoadingIndicator()
        }
    }
}

@Composable
fun PulsatingLoadingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(modifier = modifier.size(80.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = size.minDimension / 2 * scale,
                center = Offset(size.width / 2, size.height / 2)
            )
        }
        Text(
            text = "Processing",
            color = Color.White,
            modifier = Modifier.align(Alignment.Center),
            style = TextStyle(fontSize = 14.sp)
        )
    }
}