package com.example.litelens.presentation.home.components

import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.example.litelens.domain.model.Detection

@Composable
fun DrawDetectionBox(detection: Detection) {
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
    val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()

    val boxState = remember { mutableStateOf(detection) }

    LaunchedEffect(detection) {
        boxState.value = detection
    }

    // Calculate scaling
    val scaleX = screenWidth / detection.imageWidth
    val scaleY = screenHeight / detection.imageHeight
    val scale = minOf(scaleX, scaleY)

    // Calculate offset to center the image
    val offsetX = (screenWidth - detection.imageWidth * scale) / 2f
    val offsetY = (screenHeight - detection.imageHeight * scale) / 2f

    val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f * density
        color = Color.Magenta.toArgb() // Using a very visible color for testing
    }

    val textPaint = Paint().apply {
        color = paint.color
        textSize = 36f * density // Increased text size for visibility
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier.matchParentSize(),
            onDraw = {
                val currentBox = boxState.value
                Log.d("DrawDetectionBox", "onDraw called")

                // Calculate scaled box
                val left = currentBox.boundingBox.left * scale + offsetX
                val top = currentBox.boundingBox.top * scale + offsetY
                val right = currentBox.boundingBox.right * scale + offsetX
                val bottom = currentBox.boundingBox.bottom * scale + offsetY

                // Draw bounding box
                val strokeWidth = 4f * density
                drawRect(
                    color = Color(paint.color),
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    style = Stroke(strokeWidth)
                )

                val cornerSize = 20f * density
                drawLine(Color(paint.color), Offset(left, top), Offset(left + cornerSize, top), strokeWidth)
                drawLine(Color(paint.color), Offset(left, top), Offset(left, top + cornerSize), strokeWidth)

                // Draw text
                val text = "${currentBox.detectedObjectName} ${(currentBox.confidenceScore * 100).toInt()}%"
                drawIntoCanvas { canvas ->
                    val textRect = Rect()
                    textPaint.getTextBounds(text, 0, text.length, textRect)
                    val textX = left
                    val textY = top - textPaint.textSize / 2

                    // Draw a background for the text
                    val bgPaint = Paint(textPaint)
                    bgPaint.color = Color.Black.copy(alpha = 0.7f).toArgb()
                    canvas.nativeCanvas.drawRect(
                        textX - 5,
                        textY - textRect.height() - 5,
                        textX + textRect.width() + 5,
                        textY + 5,
                        bgPaint
                    )

                    canvas.nativeCanvas.drawText(text, textX, textY, textPaint)
                }


                Log.d("DrawDetectionBox", "Drew box at ($left, $top, $right, $bottom)")
            }
        )
    }

    // Log for debugging
    Log.d("DrawDetectionBox", "Screen: ${screenWidth}x${screenHeight}, Image: ${detection.imageWidth}x${detection.imageHeight}")
    Log.d("DrawDetectionBox", "Scale: $scale, Offset: ($offsetX, $offsetY)")
    Log.d("DrawDetectionBox", "Original Box: ${detection.boundingBox}")
}
