package com.example.litelens.presentation.home

import android.graphics.Rect
import android.util.Log
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.litelens.R
import com.example.litelens.domain.model.Detection
import com.example.litelens.presentation.common.SwitchIconsButton
import com.example.litelens.presentation.home.components.CameraOverlay
import com.example.litelens.presentation.home.components.CameraPreview
import com.example.litelens.presentation.home.components.CameraTextRecognitionOverlay
import com.example.litelens.presentation.home.components.RequestPermission

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val viewModel: HomeViewModel = hiltViewModel()

    // Requesting permissions
    RequestPermission()

    // Observing the state for whether an image is saved
    val isImageSavedStateFlow = viewModel.isImageSavedStateFlow.collectAsState()

    val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels
    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels

    var detectionResults by remember { mutableStateOf<List<Detection>>(emptyList()) }

    val isImageDetectionChecked by viewModel.isImageDetectionChecked.collectAsState()
    var translatedText by remember { mutableStateOf<String?>(null) }

    var cameraController by remember { mutableStateOf<LifecycleCameraController?>(null) }

    var previewSize by remember { mutableStateOf(IntSize(0, 0)) }

    val visualSearchResults by viewModel.visualSearchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    LaunchedEffect(Unit) {
        cameraController = viewModel.initializeCameraController(
            context = context,
            isImageDetectionChecked = isImageDetectionChecked,
            onTextRecognized = { translatedText = it },
            onObjectDetectionResult = { detectionResults = it },
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )
    }

    LaunchedEffect(isImageDetectionChecked) {
        viewModel.updateCameraAnalyzer(
            context = context,
            isImageDetectionChecked = isImageDetectionChecked,
            onTextRecognized = { translatedText = it },
            onObjectDetectionResult = { detectionResults = it },
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        CameraContent(
            cameraController = cameraController,
            isImageDetectionChecked = isImageDetectionChecked,
            translatedText = translatedText,
            detectionResults = detectionResults,
            onSwitchModeClicked = viewModel::toggleImageDetection,
            previewSize = previewSize,
            onPreviewSizeChanged = { newSize -> previewSize = newSize }
        )

        if (isSearching) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = colorResource(id = R.color.white)
            )
        }

    }

}

@Composable
private fun CameraContent(
    cameraController: LifecycleCameraController?,
    isImageDetectionChecked: Boolean,
    translatedText: String?,
    detectionResults: List<Detection>,
    onSwitchModeClicked: () -> Unit,
    previewSize: IntSize,
    onPreviewSizeChanged: (IntSize) -> Unit
) {
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
            CameraPreview(
                cameraController = cameraController,
                onPreviewSizeChanged = onPreviewSizeChanged
            )

            if (!isImageDetectionChecked) {
                TextRecognitionOverlay(translatedText)
            }else{
                ObjectRecognitionOverlay(
                    detectionResults = detectionResults
                )
            }



            SwitchModeButton(
                isChecked = isImageDetectionChecked,
                onCheckedChange = onSwitchModeClicked
            )
        }
    }
}

@Composable
private fun CameraPreview(
    cameraController: LifecycleCameraController?,
    onPreviewSizeChanged: (IntSize) -> Unit
) {
    cameraController?.let {
        CameraPreview(
            controller = it,
            modifier = Modifier.fillMaxSize(),
            onPreviewSizeChanged = onPreviewSizeChanged
        )
    }
}

@Composable
private fun TextRecognitionOverlay(translatedText: String?) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraTextRecognitionOverlay(
            boxWidthPercentage = 0.8f,
            boxHeightPercentage = 0.2f,
            modifier = Modifier.fillMaxSize(),
            text = "Center text inside the box"
        )
        translatedText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.headlineMedium,
                color = colorResource(id = R.color.white),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun ObjectRecognitionOverlay(detectionResults: List<Detection>) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraTextRecognitionOverlay(
            boxWidthPercentage = 0.8f,
            boxHeightPercentage = 0.5f,
            modifier = Modifier.fillMaxSize(),
            text = "Center object inside the box"
        )
        CameraOverlay(
            detections = detectionResults
        )
    }
}



@Composable
private fun SwitchModeButton(
    isChecked: Boolean,
    onCheckedChange: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        SwitchIconsButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-24).dp, y = (-40).dp)
                .padding(16.dp),
            checked = isChecked,
            onCheckedChange = {
                Log.d("APP_LENS", "Switching mode")
                onCheckedChange()
            }
        )
    }
}