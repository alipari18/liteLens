package com.example.litelens.domain.model

import android.graphics.RectF

enum class DetectionType {
    OBJECT_DETECTION,
    TEXT_DETECTION
}

data class Detection (
    val boundingBox: RectF,
    val detectedObjectName: String,
    val confidenceScore: Float,
    val tensorImageHeight: Int,
    val tensorImageWidth: Int,
    val type: DetectionType
)