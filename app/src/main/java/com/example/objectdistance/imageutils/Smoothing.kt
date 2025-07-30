package com.example.objectdistance.imageutils

import android.graphics.RectF

fun smoothRect(oldRect: RectF, newRect: RectF, alpha: Float = 0.3f): RectF {
    return RectF(
        oldRect.left + alpha * (newRect.left - oldRect.left),
        oldRect.top + alpha * (newRect.top - oldRect.top),
        oldRect.right + alpha * (newRect.right - oldRect.right),
        oldRect.bottom + alpha * (newRect.bottom - oldRect.bottom)
    )
}

