package com.example.litelens.presentation.home.components

import android.content.res.Resources.Theme
import android.graphics.PointF
import androidx.compose.ui.graphics.Color
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.example.litelens.domain.model.Detection
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toComposeRect


/**
 * Composable function that creates an overlay for the camera feed to display bounding boxes around detected objects.
 * It places each detection box on top of the camera preview based on the detection data provided.
 *
 * @param detections A list of [Detection] objects that contain the bounding box information and metadata
 *                   for each detected object in the camera feed.
 */
@Composable
fun CameraOverlay(detections: List<Detection>) {
        detections.forEach { detection ->
            Log.d("APP_LENS", "Drawing detection box for ${detection.type}")
            DrawDetectionBox(detection)
        }

}



