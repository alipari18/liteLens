package com.example.litelens.presentation.home.components

import android.util.Log
import androidx.compose.runtime.Composable
import com.example.litelens.domain.model.Detection


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



