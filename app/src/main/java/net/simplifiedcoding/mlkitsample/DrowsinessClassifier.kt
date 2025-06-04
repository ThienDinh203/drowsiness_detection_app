package net.simplifiedcoding.mlkitsample.facedetector

import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.camera.core.ImageProxy
import net.simplifiedcoding.mlkitsample.R
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
//import net.simplifiedcoding.mlkitsample.assets.ModelStableV1
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min

/*
class DrowsinessClassifier(private val context: Context) {

    private val model: ModelStableV1 by lazy {
        ModelStableV1.newInstance(context)
    }

    // Run inference on a single face region
    fun classifyFace(imageProxy: ImageProxy, faceBoundingBox: Rect): String {
        val faceBitmap = cropFaceFromImage(imageProxy.toBitmap(), faceBoundingBox)
        val resizedBitmap = Bitmap.createScaledBitmap(faceBitmap, 224, 224, true)
        val tensorImage = TensorImage.fromBitmap(resizedBitmap)

        val inputFeature = tensorImage.tensorBuffer

        val output = model.process(inputFeature)
        val outputBuffer = output.outputFeature0AsTensorBuffer

        return interpretOutput(outputBuffer)
    }

    // Close model when done
    fun close() {
        model.close()
    }

    // Map float outputs to drowsiness labels
    private fun interpretOutput(outputBuffer: TensorBuffer): String {
        val outputArray = outputBuffer.floatArray

        return when (outputArray.indices.maxByOrNull { outputArray[it] } ?: -1) {
            0 -> "Drowsy"
            1 -> "Yawning"
            2 -> "Alert"
            else -> "Unknown"
        }
    }

    // Crop face safely from bitmap
    private fun cropFaceFromImage(bitmap: Bitmap, boundingBox: Rect): Bitmap {
        val safeRect = Rect(
            max(0, boundingBox.left),
            max(0, boundingBox.top),
            min(bitmap.width, boundingBox.right),
            min(bitmap.height, boundingBox.bottom)
        )
        return Bitmap.createBitmap(bitmap, safeRect.left, safeRect.top, safeRect.width(), safeRect.height())
    }

    // Convert ImageProxy to Bitmap (YUV to RGB)
    private fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}
*/

class DrowsinessClassifier(context: Context) {

    private val inputImageSize = 224  // adjust to your model’s input size
    private val modelPath = "drowsiness_detectorV2.tflite"
    private val interpreter: Interpreter

    init {
        val modelBuffer = loadModelFile(context)
        interpreter = Interpreter(modelBuffer)
        Log.d("DinhThien", "Model loaded successfully!")
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
//        val fileDescriptor = context.resources.openRawResourceFd(R.raw.model_stable_v1)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, inputImageSize, inputImageSize, true)
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputImageSize * inputImageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        for (y in 0 until inputImageSize) {
            for (x in 0 until inputImageSize) {
                val pixel = resized.getPixel(x, y)
                byteBuffer.putFloat(((pixel shr 16 and 0xFF) / 255f))
                byteBuffer.putFloat(((pixel shr 8 and 0xFF) / 255f))
                byteBuffer.putFloat(((pixel and 0xFF) / 255f))
            }
        }
        return byteBuffer
    }
/*
    fun classify(bitmap: Bitmap): String {
        val input = preprocess(bitmap)
        val output = Array(1) { FloatArray(3) } // adjust to match your model
        interpreter.run(input, output)

        val scores = output[0]
        val maxIndex = scores.indices.maxByOrNull { scores[it] } ?: -1

        val confidence = scores[maxIndex] * 100  // Convert to percentage
        val label = if (maxIndex == 0) "Awake" else "Drowsy" // Group Drowsy + Yawning

        return "$label (${confidence.toInt()}%)"
    }
*/

    fun classify(bitmap: Bitmap): String {
        val input = preprocess(bitmap)
        val output = Array(1) { FloatArray(3) } // [closed_eye, normal, Yawn]
        interpreter.run(input, output)

        val awakeProb = output[0][0]
        val drowsyProb = output[0][1]
        val yawnProb = output[0][2]
        val highThreshold = 0.5f

        val combinedDrowsy = drowsyProb + yawnProb

        val status = if (awakeProb > highThreshold) "Drowsy" else "Awake"

        Log.d("DinhThien", "Awake: %.2f, Drowsy: %.2f, Ignored Yawning: %.2f → Status: %s".format(
            awakeProb, drowsyProb, output[0][2], status
        ))

//        return status
        return "$status (%.0f%%)".format(
            if (status == "Drowsy") drowsyProb * 100 else awakeProb * 100
        )
    }

}
