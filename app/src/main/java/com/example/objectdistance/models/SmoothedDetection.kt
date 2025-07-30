package com.example.objectdistance.models

import android.graphics.RectF


data class SmoothedDetection(
    val label: String,
    val score: Float,
    var rect: RectF
)

