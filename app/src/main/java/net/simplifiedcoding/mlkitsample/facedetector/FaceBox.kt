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

/*
//source code first
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 6.0f
    }

    override fun draw(canvas: Canvas?) {
        val rect = getBoxRect(
            imageRectWidth = imageRect.width().toFloat(),
            imageRectHeight = imageRect.height().toFloat(),
            faceBoundingBox = face.boundingBox
        )
        canvas?.drawRect(rect, paint)
    }
    */

    /* its work for 3 classes
    private val boxPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 6.0f
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
        typeface = Typeface.DEFAULT_BOLD
    }

    override fun draw(canvas: Canvas?) {
        val rect = getBoxRect(
            imageRectWidth = imageRect.width().toFloat(),
            imageRectHeight = imageRect.height().toFloat(),
            faceBoundingBox = face.boundingBox
        )

        canvas?.drawRect(rect, boxPaint)

        canvas?.drawText(label, rect.left, rect.top - 10f, textPaint)
    } */
    private val paint = Paint().apply {
//        color = if (label == "Drowsy") Color.RED else Color.GREEN
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 6.0f
    }

    private val textPaint = Paint().apply {
//        color = Color.WHITE
        color = if (label == "Awake") Color.GREEN else Color.RED
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

        // Draw label text (e.g., "Awake" or "Drowsy") above the box
        canvas?.drawText(label, rect.left, rect.top - 10, textPaint)
    }
}