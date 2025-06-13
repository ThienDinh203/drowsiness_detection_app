package net.simplifiedcoding.mlkitsample.facedetector

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import com.google.mlkit.vision.face.Face

class FaceBox(
    overlay: FaceBoxOverlay,
    private val face: Face,
    private val imageRect: Rect,
    var label: String
) : FaceBoxOverlay.FaceBox(overlay) {

    private val paint = Paint().apply {
        color = if (label.contains("Awake")) Color.GREEN else Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 6.0f
    }

    private val textPaint = Paint().apply {
        color = if (label.contains("Awake")) Color.GREEN else Color.RED
        textSize = 50f
        style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas?) {
        val rect = getBoxRect(
            imageRectWidth = imageRect.width().toFloat(),
            imageRectHeight = imageRect.height().toFloat(),
            faceBoundingBox = face.boundingBox
        )
        canvas?.drawRect(rect, paint)

        canvas?.drawText(label, rect.left, rect.top - 10, textPaint)
    }
}