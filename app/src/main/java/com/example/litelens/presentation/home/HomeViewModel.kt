package com.example.litelens.presentation.home

import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.translation.TranslationManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.litelens.domain.model.Detection
import com.example.litelens.domain.repository.languageIdentification.LanguageIdentificationManager
import com.example.litelens.domain.repository.objectDetection.ObjectDetectionManager
import com.example.litelens.domain.repository.textRecognition.TextRecognitionManager
import com.example.litelens.domain.repository.textTranslation.TextTranslationManager
import com.example.litelens.utils.CameraFrameAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class HomeViewModel @Inject constructor(
    val objectDetectionManager: ObjectDetectionManager,
    val textRecognitionManager: TextRecognitionManager,
    val languageIdentificationManager: LanguageIdentificationManager,
    val textTranslationManager: TextTranslationManager
): ViewModel() {

    companion object {
        private val TAG: String? = HomeViewModel::class.simpleName
    }

    private val _isImageSavedStateFlow = MutableStateFlow(true)
    val isImageSavedStateFlow = _isImageSavedStateFlow.asStateFlow()

    private val labelColorMap = mutableMapOf<String, Int>()

    private val _detectionResults = MutableStateFlow<List<Detection>>(emptyList())
    val detectionResults = _detectionResults.asStateFlow()

    private val _translationResults = MutableStateFlow("")
    val translationResults = _translationResults.asStateFlow()

    fun updateDetectionResults(detections: List<Detection>) {
        _detectionResults.value = detections
    }

    fun updateTranslationResults(text: String) {
        _translationResults.value = text
    }

    /**
     * Initializes and returns a `LifecycleCameraController` instance with the specified use cases.
     * This function sets up the camera controller for image analysis and image capture.
     *
     * @param context context used for managing the lifecycle of the camera controller.
     * @return Returns a fully initialized `LifecycleCameraController` instance.
     */
    fun prepareCameraController(
        context: Context,
        cameraFrameAnalyzer: CameraFrameAnalyzer
    ): LifecycleCameraController{
        Log.d(TAG, "prepareCameraController: Preparing Camera Controller")
        return LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_ANALYSIS or CameraController.IMAGE_CAPTURE
            )
            setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context),
                cameraFrameAnalyzer
            )
        }
    }

    /**
     * Retrieves the selected camera (front or back) based on the current camera selection of the provided camera controller.
     *
     * @param cameraController The controller managing the camera operations.
     * @return Returns [CameraSelector.DEFAULT_FRONT_CAMERA] if the current camera is the back camera,
     *         and [CameraSelector.DEFAULT_BACK_CAMERA] if it's the front camera.
     */
    fun getSelectedCamera(
        cameraController: LifecycleCameraController
    ): CameraSelector {
        return if(
            cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA
        ){
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    fun capturePhoto(
        context: Context,
        cameraController: LifecycleCameraController,
        screenWidth: Float,
        screenHeight: Float,
        detections: List<Detection>
    ){
        cameraController.takePicture(
            ContextCompat.getMainExecutor(context),
            object: ImageCapture.OnImageCapturedCallback() {

                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    Log.d(TAG, "onCaptureSuccess: Image Captured")

                    val rotatedImageMatrix: Matrix =
                        Matrix().apply {
                            postRotate(image.imageInfo.rotationDegrees.toFloat())
                        }

                    val rotatedBitmap: Bitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,
                        rotatedImageMatrix,
                        true
                    )

                    val combinedBitmap = overlayDetectionsOnBitmap(
                    rotatedBitmap,
                    detections,
                    screenWidth,
                    screenHeight
                    )

                    // Save the Image-Bitmap to Device
                    saveBitmapToDevice(
                        context = context,
                        capturedImageBitmap = combinedBitmap
                    )
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e(TAG, "onError() called for capturePhoto with: exception = $exception")
                    isPhotoSuccessfullySaved(false)
                }
            }
        )
    }

    private fun saveBitmapToDevice(
        context: Context,
        capturedImageBitmap: Bitmap
    ){
        viewModelScope.launch(Dispatchers.IO) {
            try{
                Log.d(TAG, "saveBitmapToDevice: Saving image to device with version = ${Build.VERSION.SDK_INT}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, generateImageName())
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }

                    val uri: Uri? = context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )

                    uri?.let {
                        context.contentResolver.openOutputStream(it).use { outputStream ->
                            if(outputStream != null) {
                                capturedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                            }
                            outputStream?.flush()
                            outputStream?.close()

                        }
                    }

                    isPhotoSuccessfullySaved(true)
                }
            }catch (e: Exception){
                Log.e(TAG, "saveBitmapToDevice: Error saving image to device", e)
                isPhotoSuccessfullySaved(false)
            }
        }
    }

    /**
     * Returns the current system time in a formatted string, prepended with "IMG_".
     * The returned string is in the format "IMG_YYYYMMDD_HHMMSS", which represents the current system time.
     *
     * @return A formatted string representing the current system time, prepended with "IMG_".
     */
    private fun generateImageName(): String {
        Log.d(TAG, "generateImageName() called")
        val currentDateTime = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDateTime)
        return "IMG_$formattedDate"
    }

    fun overlayDetectionsOnBitmap(
        bitmap: Bitmap,
        detections: List<Detection>,
        screenWidth: Float,
        screenHeight: Float
    ): Bitmap {
        val overlayBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        val canvas = Canvas(overlayBitmap)

        // Draw the captured image bitmap
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        // Draw detections on the canvas (i.e., on top of the captured image)
        detections.forEach { detection ->
            drawDetectionBox(
                detection = detection,
                originalBitmap = overlayBitmap,
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )
        }
        return overlayBitmap
    }

    private fun drawDetectionBox(
        detection: Detection,
        originalBitmap: Bitmap,
        screenWidth: Float,
        screenHeight: Float
    ): Bitmap{
        val canvas = Canvas(originalBitmap)
        val paint = Paint().apply {
            color = getColorForLabel(detection.detectedObjectName)
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }

        // Scaling adaptively depending on DPI of the device
        val adaptiveScaleFactor: Float = getDeviceDensityValue()

        val scaledBox = RectF(
            detection.boundingBox.left * adaptiveScaleFactor,
            detection.boundingBox.top * adaptiveScaleFactor,
            detection.boundingBox.right * adaptiveScaleFactor,
            detection.boundingBox.bottom * adaptiveScaleFactor
        ).also {
            it.left = it.left.coerceAtLeast(0f)
            it.top = it.top.coerceAtLeast(0f)
            it.right = it.right.coerceAtMost(screenWidth)
            it.bottom = it.bottom.coerceAtMost(screenHeight)
        }

        val text = "${detection.detectedObjectName} ${(detection.confidenceScore * 100).toInt()}%"
        val textPaint = Paint().apply {
            color = paint.color
            textSize = 20f
        }

        // Draw the text on the canvas
        canvas.drawText(text, scaledBox.left, scaledBox.top - 10, textPaint)

        return originalBitmap
    }

    /**
     * Updates the state flow with the status of whether the photo has been successfully saved or not.
     *
     * @param isSaved Boolean flag indicating if the photo was successfully saved.
     */
    private fun isPhotoSuccessfullySaved(isSaved: Boolean) {
        Log.d(TAG, "isPhotoSuccessfullySaved() called with: isSaved Flag = $isSaved")
        _isImageSavedStateFlow.value = isSaved
    }

    /**
     * Retrieves the device's screen density factor as a float value.
     * This value is used to scale pixel dimensions to match the current screen density.
     *
     * @return A float representing the density factor of the display (e.g., 0.75 for low, 1.0 for medium, etc.).
     * The default return value is 1.0f, corresponding to the baseline screen density (mdpi).
     */
    private fun getDeviceDensityValue(): Float {
        return when (Resources.getSystem().displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> 0.75f
            DisplayMetrics.DENSITY_MEDIUM -> 1.0f
            DisplayMetrics.DENSITY_HIGH -> 1.5f
            DisplayMetrics.DENSITY_XHIGH -> 2.0f
            DisplayMetrics.DENSITY_XXHIGH -> 3.0f
            DisplayMetrics.DENSITY_XXXHIGH -> 4.0f
            else -> 1.0f
        }
    }


    /**
     * Gets a color associated with a particular label. If a color is not already assigned,
     * it generates a random color and associates it with the label for consistent coloring.
     *
     * @param label The label for which a color is required.
     * @return The color associated with the given label.
     */
    private fun getColorForLabel(label: String): Int {
        return labelColorMap.getOrPut(label) {
            // Generates a random color for the label if it doesn't exist in the map.
            Random.nextInt()
        }
    }






}