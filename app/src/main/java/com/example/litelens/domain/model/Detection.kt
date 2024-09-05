package com.example.litelens.domain.model

import android.graphics.Rect

enum class DetectionType {
    OBJECT_DETECTION,
    TEXT_DETECTION
}

data class Detection (
    val boundingBox: Rect,
    var detectedObjectName: String,
    val confidenceScore: Float,
    val imageHeight: Int,
    val imageWidth: Int,
    val type: DetectionType
)