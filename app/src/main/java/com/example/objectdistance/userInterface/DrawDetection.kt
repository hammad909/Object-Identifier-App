package com.example.objectdistance.userInterface

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.objectdistance.models.DetectionResult


@SuppressLint("DefaultLocale")
@Composable
fun DrawDetections(detections: List<DetectionResult>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val knownObjectWidths = mapOf(
            "person" to 45f,      // in cm
            "bottle" to 7f,
            "cup" to 8f,
            "cell phone" to 7f
        )

        val focalLength = 870f // approximate value for smartphone camera

        detections.forEachIndexed { index, detection ->
            val color1: Color = listOf(
                Color.Red, Color.Green, Color.Blue, Color.Magenta,
                Color.Yellow, Color.Cyan, Color.Gray
            )[index % 7]

            val left = detection.rect.left * canvasWidth
            val top = detection.rect.top * canvasHeight
            val right = detection.rect.right * canvasWidth
            val bottom = detection.rect.bottom * canvasHeight

            // Draw bounding box
            drawRect(
                color = color1,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
            )

            // Distance estimation
            val perceivedWidth = right - left
            val realWidth = knownObjectWidths[detection.label.lowercase()] ?: 10f
            val distance = (realWidth * focalLength) / perceivedWidth

            // Prepare text (label + score + distance)
            val text = "${detection.label} ${String.format("%.2f", detection.score)}\n${String.format("%.1f", distance)} cm"

            // Paint setup
            val paint = Paint().apply {
                textSize = 40f
                typeface = Typeface.DEFAULT_BOLD
                style = Paint.Style.FILL
                color = android.graphics.Color.WHITE
            }
            val bgPaint = Paint().apply {
                alpha = 160
                style = Paint.Style.FILL
            }

            // Multiline support
            val textLines = text.split("\n")
            val maxTextWidth = textLines.maxOf { paint.measureText(it) }
            val textHeight = paint.textSize

            // Draw background box
            drawContext.canvas.nativeCanvas.drawRect(
                left + 5,
                top + 5,
                left + maxTextWidth + 15,
                top + (textLines.size * textHeight) + 15,
                bgPaint
            )

            // Draw each line of text
            textLines.forEachIndexed { i, line ->
                drawContext.canvas.nativeCanvas.drawText(
                    line,
                    left + 10,
                    top + (i + 1) * textHeight + 5,
                    paint
                )
            }
        }
    }
}

