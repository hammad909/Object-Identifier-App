package com.example.objectdistance.models

import android.graphics.RectF

data class DetectionResult(
    val label: String,
    val score: Float,
    val rect: RectF
)

