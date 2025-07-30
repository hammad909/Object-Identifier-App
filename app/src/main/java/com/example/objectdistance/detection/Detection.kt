package com.example.objectdistance.detection


import android.graphics.RectF
import com.example.objectdistance.imageutils.smoothRect
import com.example.objectdistance.models.DetectionResult
import com.example.objectdistance.models.SmoothedDetection


fun processDetections(
    scores: FloatArray,
    locations: FloatArray,
    classes: FloatArray,
    labels: List<String>,
    smoothedDetections: List<SmoothedDetection>
): List<SmoothedDetection> {
    val newDetections = mutableListOf<DetectionResult>()

    for (i in scores.indices) {
        if (scores[i] > 0.6f) {
            val rect = RectF(
                locations[i * 4 + 1],
                locations[i * 4],
                locations[i * 4 + 3],
                locations[i * 4 + 2]
            )
            val label = labels.getOrElse(classes[i].toInt()) { "Unknown" }
            newDetections.add(DetectionResult(label, scores[i], rect))
        }
    }

    return newDetections.map { newDet ->
        val oldDet = smoothedDetections.find { it.label == newDet.label }
        val rect = oldDet?.let { smoothRect(it.rect, newDet.rect) } ?: newDet.rect
        SmoothedDetection(newDet.label, newDet.score, rect)
    }
}
