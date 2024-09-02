package com.example.litelens.presentation.home

import android.graphics.RectF
import android.util.Log
import androidx.annotation.Dimension
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.litelens.R
import com.example.litelens.data.manager.languageIdentification.LanguageIdentificationManagerImpl
import com.example.litelens.data.manager.objectDetection.ObjectDetectionManagerImpl
import com.example.litelens.data.manager.textRecognition.TextRecognitionManagerImpl
import com.example.litelens.data.manager.textTranslation.TextTranslationManagerImpl
import com.example.litelens.domain.model.Detection
import com.example.litelens.presentation.home.components.CameraOverlay
import com.example.litelens.presentation.home.components.CameraPreview
import com.example.litelens.presentation.home.components.RequestPermission
import com.example.litelens.utils.CameraFrameAnalyzer
import com.example.litelens.utils.Constants
import com.example.litelens.utils.ImageScalingUnits

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val viewModel: HomeViewModel = hiltViewModel()

    // Requesting permissions
    RequestPermission()

    // Observing the state for whether an image is saved
    val isImageSavedStateFlow = viewModel.isImageSavedStateFlow.collectAsState()

    // State to keep track of the preview size of the camera feed
    val previewSizeState = remember { mutableStateOf(IntSize(0,0)) }

    // List to hold bounding box coordinates for the detected object
    val boundingBoxCoordinatesState = remember { mutableStateListOf<RectF>() }

    // State for the confidence score
    val confidenceScoreState = remember { mutableFloatStateOf(Constants.INITIAL_CONFIDENCE_SCORE)}

    val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels * 1f
    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels * 1f

    val detectionResults by viewModel.detectionResults.collectAsState()
    val translationResult by viewModel.translationResults.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        var detections by remember {
            mutableStateOf(emptyList<Detection>())
        }

        // re-invoke composable when state of detections changes
        LaunchedEffect(detections) {}

        // Preparing Image Analyzer
        val cameraFrameAnalyzer =  remember {
            CameraFrameAnalyzer(
                objectDetectionManager = viewModel.objectDetectionManager,  // Use ViewModel injected managers
                textRecognitionManager = viewModel.textRecognitionManager,
                languageIdentificationManager = viewModel.languageIdentificationManager,
                translationManager = viewModel.textTranslationManager,
                onObjectDetectionResults = {
                    detections = it

                    // Clear the previous RectFs and add all new ones
                    boundingBoxCoordinatesState.clear()
                    detections.forEach { detection ->
                        boundingBoxCoordinatesState.add(detection.boundingBox)
                    }
                },
                onTranslationResults = {
                    viewModel.updateTranslationResults(it)
                },
                confidenceScoreState = confidenceScoreState
            )
        }

        // Prepare Camera Controller
        val cameraController = remember {
            viewModel.prepareCameraController(
                context = context,
                cameraFrameAnalyzer = cameraFrameAnalyzer
            )
        }

        val backgroundColor = MaterialTheme.colorScheme.background

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.8f)
            ) {
                // Camera Preview
                CameraPreview(
                    controller = remember {
                        cameraController
                    },
                    modifier = Modifier.fillMaxSize(),
                    onPreviewSizeChanged = { newSize ->
                        previewSizeState.value = newSize

                        // Get Scale-Factors for bounding box coordinates
                        val scaleFactors = ImageScalingUnits.getScaleFactors(
                            newSize.width,
                            newSize.height
                        )

                        Log.d("HomeViewModel", "HomeScreen() called with: newSize = $scaleFactors")
                    }
                )

                // Camera Overlay that overalls on top of the camera preview
                CameraOverlay(
                    detections = detections,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.2f)
                    .padding(top = 8.dp)
            ){
                if (detections.isNotEmpty()) {
                    Text(text = "Object detected: ${detections[0].detectedObjectName}")
                }

                if (translationResult.isNotEmpty()) {
                    Text(text = "Translated text: $translationResult")
                }


            }


        }
    }

}