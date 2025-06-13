package net.simplifiedcoding.mlkitsample.facedetector

import android.content.Context
import android.graphics.*
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

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

    fun classify(bitmap: Bitmap): Int {
        val input = preprocess(bitmap)
        val output = Array(1) { FloatArray(3) } // [closed_eye, normal, Yawn]
        interpreter.run(input, output)

        val closeeyesProb = output[0][0]
        val awakeProb = output[0][1]
        val yawnProb = output[0][2]
        val highThreshold = 0.5f

        val combinedDrowsy = closeeyesProb + yawnProb

        val status = if (closeeyesProb > highThreshold) "Drowsy" else "Awake"

        Log.d("DinhThien", "Awake: %.2f, Drowsy: %.2f, Ignored Yawning: %.2f → Status: %s".format(
            awakeProb, closeeyesProb, output[0][2], status
        ))

        if(closeeyesProb > highThreshold) return 0
        return 1
//        return status
//        return (
//            awakeProb * 100
//        )
    }

}
