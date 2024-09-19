package com.example.litelens.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.example.litelens.domain.model.Detection
import com.example.litelens.domain.repository.objectDetection.ObjectDetectionManager
import javax.inject.Inject
import kotlin.math.roundToInt

class CameraFrameAnalyzer @Inject constructor(
    private val objectDetectionManager: ObjectDetectionManager,
    private val onObjectDetectionResults: (List<Detection>) -> Unit,
    private val onInitiateVisualSearch: (Bitmap) -> Unit,
    private val isSearching: () -> Boolean,
    private val isBottomSheetVisible: () -> Boolean,
    private val confidenceScoreState: Float = Constants.INITIAL_CONFIDENCE_SCORE,
    private val screenWidth: Int,
    private val screenHeight: Int
) : ImageAnalysis.Analyzer {

    private var frameSkipCounter = 0
    private val boxWidthPercentage = 0.8f
    private val boxHeightPercentage = 0.5f

    /**
     * Analyzes each frame from the camera feed.
     * Skips frames when searching or when the bottom sheet is visible.
     * Processes every 15th frame to reduce computational load.
     */
    override fun analyze(imageProxy: ImageProxy) {

        if (isSearching() || isBottomSheetVisible()) {
            imageProxy.close()
            return
        }

        frameSkipCounter++
        if (frameSkipCounter % 15 == 0) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val bitmapImage: Bitmap = imageProxy.toBitmap()
            val rotatedBitmap = bitmapImage.rotateIfRequired(rotationDegrees)
            val croppedBitmap = cropBitmapToOverlay(rotatedBitmap, screenWidth, screenHeight)
            val preprocessedBitmap = preprocessImage(croppedBitmap, imageProxy.imageInfo.rotationDegrees)
            processImage(preprocessedBitmap, croppedBitmap, imageProxy)
        }
        imageProxy.close()
    }

    /**
     * Processes the image for object detection and initiates visual search if objects are detected.
     * @param bitmapImage The preprocessed bitmap image
     * @param croppedBitmap The original cropped bitmap
     * @param proxy The ImageProxy containing metadata about the image
     */
    private fun processImage(bitmapImage: Bitmap, croppedBitmap: Bitmap, proxy: ImageProxy) {
        Log.d("APP_LENS", "Processing image")
        objectDetectionManager.detectObjectsInCurrentFrame(
            bitmap = bitmapImage,
            originalBitmap = croppedBitmap,
            rotation = proxy.imageInfo.rotationDegrees,
            confidenceThreshold = confidenceScoreState,
            onSuccess = { detectedObjects: List<Detection> ->
                Log.d("APP_LENS", "Detected objects: ${detectedObjects.size}")
                onObjectDetectionResults(detectedObjects)
                if ( detectedObjects.isNotEmpty() ){
                    onInitiateVisualSearch(croppedBitmap)
                }
            },
            onError = { error ->
                Log.e("APP_LENS", error)
            }
        )
    }

    /**
     * Preprocesses the image for better object detection performance.
     * Resizes the image, applies enhancements, and handles rotation.
     * @param bitmap The original bitmap image
     * @param rotation The rotation of the image in degrees
     * @return A preprocessed bitmap image
     */
    private fun preprocessImage(bitmap: Bitmap, rotation: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())

        // Resize the image to a standard size for better detection performance
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)

        // Apply image enhancements
        val enhancedBitmap = enhanceImage(scaledBitmap)

        return Bitmap.createBitmap(enhancedBitmap, 0, 0, enhancedBitmap.width, enhancedBitmap.height, matrix, true)
    }

    /**
     * Applies multiple image enhancement techniques to improve object detection.
     * @param original The original bitmap image
     * @return An enhanced bitmap image
     */
    private fun enhanceImage(original: Bitmap): Bitmap {
        val width = original.width
        val height = original.height
        val enhanced = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(enhanced)
        val paint = Paint()

        // Apply multiple enhancements
        val enhancedBitmap = original
            .let { increaseContrast(it, 1.5f) }
            .let { adjustBrightness(it, 1.2f) }
            .let { enhanceEdges(it) }

        canvas.drawBitmap(enhancedBitmap, 0f, 0f, paint)
        return enhanced
    }

    /**
     * Increases the contrast of the image.
     * @param src The source bitmap
     * @param factor The contrast adjustment factor
     * @return A bitmap with increased contrast
     */
    private fun increaseContrast(src: Bitmap, factor: Float): Bitmap {
        val contrast = factor.coerceIn(0f, 2f)
        val brightness = 0f
        val colorMatrix = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, brightness,
            0f, contrast, 0f, 0f, brightness,
            0f, 0f, contrast, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        ))
        return applyColorMatrix(src, colorMatrix)
    }

    /**
     * Adjusts the brightness of the image.
     * @param src The source bitmap
     * @param factor The brightness adjustment factor
     * @return A bitmap with adjusted brightness
     */
    private fun adjustBrightness(src: Bitmap, factor: Float): Bitmap {
        val colorMatrix = ColorMatrix().apply { setScale(factor, factor, factor, 1f) }
        return applyColorMatrix(src, colorMatrix)
    }

    /**
     * Enhances the edges in the image to improve object detection.
     * @param src The source bitmap
     * @return A bitmap with enhanced edges
     */
    private fun enhanceEdges(src: Bitmap): Bitmap {
        val sharpness = 0.5f
        val kernel = floatArrayOf(
            0f, -sharpness, 0f,
            -sharpness, 1 + 4 * sharpness, -sharpness,
            0f, -sharpness, 0f
        )
        val convolution = ConvolutionMatrix(3)
        convolution.applyConfig(kernel)
        convolution.factor = 1f
        convolution.offset = 0f
        return convolution.computeConvolution3x3(src)
    }

    /**
     * Applies a color matrix to the bitmap for various image adjustments.
     * @param src The source bitmap
     * @param colorMatrix The color matrix to apply
     * @return A bitmap with the color matrix applied
     */
    private fun applyColorMatrix(src: Bitmap, colorMatrix: ColorMatrix): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, src.config)
        val canvas = Canvas(result)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return result
    }

    /**
     * Crops the bitmap to match the overlay dimensions on the screen.
     * @param sourceBitmap The original bitmap
     * @param screenWidth The width of the screen
     * @param screenHeight The height of the screen
     * @return A cropped bitmap matching the overlay dimensions
     */
    private fun cropBitmapToOverlay(
        sourceBitmap: Bitmap,
        screenWidth: Int,
        screenHeight: Int
    ): Bitmap {
        val bitmapWidth = sourceBitmap.width
        val bitmapHeight = sourceBitmap.height

        val overlayAspectRatio = (boxWidthPercentage * screenWidth) / (boxHeightPercentage * screenHeight)

        var cropBitmapWidth = (bitmapWidth * boxWidthPercentage).roundToInt()
        var cropBitmapHeight = (bitmapHeight * boxHeightPercentage).roundToInt()

        if (cropBitmapWidth / cropBitmapHeight > overlayAspectRatio) {
            cropBitmapWidth = (cropBitmapHeight * overlayAspectRatio).roundToInt()
        } else {
            cropBitmapHeight = (cropBitmapWidth / overlayAspectRatio).roundToInt()
        }

        val startX = (bitmapWidth - cropBitmapWidth) / 2
        val startY = (bitmapHeight - cropBitmapHeight) / 2

        val safeStartX = startX.coerceIn(0, bitmapWidth - cropBitmapWidth)
        val safeStartY = startY.coerceIn(0, bitmapHeight - cropBitmapHeight)

        return Bitmap.createBitmap(
            sourceBitmap,
            safeStartX,
            safeStartY,
            cropBitmapWidth,
            cropBitmapHeight
        )
    }

    /**
     * Rotates the bitmap if required based on the rotation degrees.
     * @param rotationDegrees The rotation in degrees
     * @return A rotated bitmap if rotation is needed, otherwise the original bitmap
     */
    private fun Bitmap.rotateIfRequired(rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return this
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    companion object {
        private const val INPUT_SIZE = 300 // or whatever size your model expects
    }
}

class ConvolutionMatrix(private val size: Int) {
    var matrix: FloatArray = FloatArray(size * size)
    var factor = 1.0f
    var offset = 1.0f

    fun computeConvolution3x3(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val result = Bitmap.createBitmap(width, height, src.config)

        val pixelsIn = IntArray(width * height)
        val pixelsOut = IntArray(width * height)
        src.getPixels(pixelsIn, 0, width, 0, 0, width, height)

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var red = 0f
                var green = 0f
                var blue = 0f
                var alpha = 0f

                for (convY in 0 until 3) {
                    for (convX in 0 until 3) {
                        val pixelIndex = (y + convY - 1) * width + (x + convX - 1)
                        val weight = matrix[convY * 3 + convX]
                        val pixel = pixelsIn[pixelIndex]

                        red += pixel.red * weight
                        green += pixel.green * weight
                        blue += pixel.blue * weight
                        alpha += pixel.alpha * weight
                    }
                }

                val finalRed = (red * factor + offset).toInt().coerceIn(0, 255)
                val finalGreen = (green * factor + offset).toInt().coerceIn(0, 255)
                val finalBlue = (blue * factor + offset).toInt().coerceIn(0, 255)
                val finalAlpha = alpha.toInt().coerceIn(0, 255)

                pixelsOut[y * width + x] = Color.argb(finalAlpha, finalRed, finalGreen, finalBlue)
            }
        }

        result.setPixels(pixelsOut, 0, width, 0, 0, width, height)
        return result
    }

    fun applyConfig(config: FloatArray) {
        if (config.size != size * size) {
            throw IllegalArgumentException("Kernel size does not match ConvolutionMatrix size")
        }
        this.matrix = config
    }
}