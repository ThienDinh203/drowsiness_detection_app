package net.simplifiedcoding.mlkitsample.facedetector

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import net.simplifiedcoding.mlkitsample.CameraXViewModel
import net.simplifiedcoding.mlkitsample.databinding.ActivityFaceDetectionBinding
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors


/*
class FaceDetectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaceDetectionBinding
    private lateinit var cameraSelector: CameraSelector
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis

    private val cameraXViewModel = viewModels<CameraXViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()
        cameraXViewModel.value.processCameraProvider.observe(this) { provider ->
            processCameraProvider = provider
            bindCameraPreview()
            bindInputAnalyser()
        }
    }

    private fun bindCameraPreview() {
        cameraPreview = Preview.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()
        cameraPreview.setSurfaceProvider(binding.previewView.surfaceProvider)
        try {
            processCameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message ?: "IllegalStateException")
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message ?: "IllegalArgumentException")
        }
    }

    private fun bindInputAnalyser() {
        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .build()
        )
        imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()

        val cameraExecutor = Executors.newSingleThreadExecutor()

//        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
//            processImageProxy(detector, imageProxy)
//        }

        imageAnalysis.setAnalyzer(cameraExecutor,
            FaceAndDrowsinessAnalyzer(
                graphicOverlay = binding.graphicOverlay,
                drowsinessModel = DrowsinessModel(),
                context = this
            )
        )


        try {
            processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message ?: "IllegalStateException")
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message ?: "IllegalArgumentException")
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(detector: FaceDetector, imageProxy: ImageProxy) {
        val inputImage =
            InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
        detector.process(inputImage).addOnSuccessListener { faces ->
            binding.graphicOverlay.clear()
            faces.forEach { face ->
                val faceBox = FaceBox(binding.graphicOverlay, face, imageProxy.image!!.cropRect)
                binding.graphicOverlay.add(faceBox)
            }
        }.addOnFailureListener {
            it.printStackTrace()
        }.addOnCompleteListener {
            imageProxy.close()
        }
    }

    companion object {
        private val TAG = FaceDetectionActivity::class.simpleName
        fun startActivity(context: Context) {
            Intent(context, FaceDetectionActivity::class.java).also {
                context.startActivity(it)
            }
        }
    }
}
*/

class FaceDetectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaceDetectionBinding
    private lateinit var cameraSelector: CameraSelector
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var drowsinessClassifier: DrowsinessClassifier

    private var awakeCount = 0
    private var drowsyCount = 0
    private val frameStates = mutableListOf<String>()

    private val cameraXViewModel = viewModels<CameraXViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drowsinessClassifier = DrowsinessClassifier(this)

        cameraSelector =
//            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()
        cameraXViewModel.value.processCameraProvider.observe(this) { provider ->
            processCameraProvider = provider
            bindCameraPreview()
            bindInputAnalyser()
        }
    }

    private fun bindCameraPreview() {
        cameraPreview = Preview.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()
        cameraPreview.setSurfaceProvider(binding.previewView.surfaceProvider)
        try {
            processCameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message ?: "IllegalStateException")
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message ?: "IllegalArgumentException")
        }
    }

    private fun bindInputAnalyser() {
        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .build()
        )
        imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()

        val cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            processImageProxy(detector, imageProxy)
        }

        try {
            processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message ?: "IllegalStateException")
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message ?: "IllegalArgumentException")
        }
    }
/*
        @SuppressLint("UnsafeOptInUsageError")
        private fun processImageProxy(detector: FaceDetector, imageProxy: ImageProxy) {
            val inputImage =
                InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
            detector.process(inputImage).addOnSuccessListener { faces ->
                binding.graphicOverlay.clear()
                faces.forEach { face ->
                    val faceBitmap = cropFaceFromImage(imageProxy.toBitmap(), face.boundingBox)
    //                val result = drowsinessClassifier.classify(faceBitmap)
                    val label = drowsinessClassifier.classify(faceBitmap)

                    if (label.contains("Awake")) {
                        awakeCount++
                    } else if (label.contains("Drowsy")) {
                        drowsyCount++
                    }

    // Log results
                    Log.d("DetectionStats", "Awake: $awakeCount, Drowsy: $drowsyCount")

                    // Overlay result (awake/drowsy/yawning) with face bounding box
                    val faceBox = FaceBox(binding.graphicOverlay, face, imageProxy.image!!.cropRect, label)
                    binding.graphicOverlay.add(faceBox)
                }
            }.addOnFailureListener {
                it.printStackTrace()
            }.addOnCompleteListener {
                imageProxy.close()
            }
        }
*/

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(detector: FaceDetector, imageProxy: ImageProxy) {
        val inputImage = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                binding.graphicOverlay.clear()

                faces.forEach { face ->
                    val faceBitmap = cropFaceFromImage(imageProxy.toBitmap(), face.boundingBox)
                    val status = drowsinessClassifier.classify(faceBitmap)  // e.g., "Awake (91%)" or "Drowsy (78%)"

                    val simpleStatus = if (status.contains("Drowsy")) "Drowsy" else "Awake"
                    frameStates.add(simpleStatus)
                    var displayLabel = status
                    // Check status after 5 frames
                    if (frameStates.size > 4) {
                        val drowsyCount = frameStates.count { it == "Drowsy" }
                        val awakeCount = frameStates.count { it == "Awake" }
                        val finalState = if (drowsyCount >= 3) "Drowsy" else "Awake"

                        Log.d("FinalState", "After 5 frames => Drowsy: $drowsyCount, Awake: $awakeCount → Final: $finalState")
                        displayLabel = finalState
                        // Keep sliding window of size 5
                        frameStates.removeAt(0)
                    }
                    // Overlay result on screen
                    val faceBox = FaceBox(binding.graphicOverlay, face, imageProxy.image!!.cropRect, displayLabel)
                    binding.graphicOverlay.add(faceBox)
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

/*
    fun ImageProxy.toBitmap(): Bitmap {
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

    private fun cropFaceFromImage(bitmap: Bitmap, rect: Rect): Bitmap {
        val safeRect = Rect(
            rect.left.coerceAtLeast(0),
            rect.top.coerceAtLeast(0),
            rect.right.coerceAtMost(bitmap.width),
            rect.bottom.coerceAtMost(bitmap.height)
        )
        return Bitmap.createBitmap(bitmap, safeRect.left, safeRect.top, safeRect.width(), safeRect.height())
    }
*/

    fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        // NV21 format: Y + interleaved VU
        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)

        // Copy V and U in interleaved format (VU)
        val chromaRowStride = planes[1].rowStride
        val chromaPixelStride = planes[1].pixelStride
        var offset = ySize
        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {
                val vuIndex = row * chromaRowStride + col * chromaPixelStride
                nv21[offset++] = vBuffer.get(vuIndex)
                nv21[offset++] = uBuffer.get(vuIndex)
            }
        }

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun cropFaceFromImage(bitmap: Bitmap, rect: Rect): Bitmap {
        val safeRect = Rect(
            rect.left.coerceAtLeast(0),
            rect.top.coerceAtLeast(0),
            rect.right.coerceAtMost(bitmap.width),
            rect.bottom.coerceAtMost(bitmap.height)
        )

        return try {
            Bitmap.createBitmap(bitmap, safeRect.left, safeRect.top, safeRect.width(), safeRect.height())
        } catch (e: IllegalArgumentException) {
            Log.e("BitmapCrop", "Crop failed: ${e.message}")
            bitmap // Return original as fallback
        }
    }


    companion object {
        private val TAG = FaceDetectionActivity::class.simpleName
        fun startActivity(context: Context) {
            Intent(context, FaceDetectionActivity::class.java).also {
                context.startActivity(it)
            }
        }
    }
}