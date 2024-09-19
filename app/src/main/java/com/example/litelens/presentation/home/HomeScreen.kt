package com.example.litelens.presentation.home

import android.util.Log
import android.widget.Toast
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.litelens.R
import com.example.litelens.domain.model.Detection
import com.example.litelens.domain.model.VisualSearchResult
import com.example.litelens.presentation.common.SwitchIconsButton
import com.example.litelens.presentation.home.components.CameraPreview
import com.example.litelens.presentation.home.components.CameraTextRecognitionOverlay
import com.example.litelens.presentation.home.components.ExpandableResultCard
import com.example.litelens.presentation.home.components.ExpandableTranslationResultCard
import com.example.litelens.presentation.home.components.LanguageSelectionBar
import com.example.litelens.presentation.home.components.RequestPermission

@Composable
fun HomeScreen(
    onNavigateToSavedSearches: () -> Unit
) {
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
    var translatedText by remember { mutableStateOf<List<VisualSearchResult>>(emptyList()) }

    var cameraController by remember { mutableStateOf<LifecycleCameraController?>(null) }

    var previewSize by remember { mutableStateOf(IntSize(0, 0)) }

    val visualSearchResults by viewModel.visualSearchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    val showBottomSheet by viewModel.showBottomSheet.collectAsState()

    val targetLanguage by viewModel.targetLanguage.collectAsState()

    LaunchedEffect(Unit) {
        cameraController = viewModel.initializeCameraController(
            context = context,
            isImageDetectionChecked = isImageDetectionChecked,
            onTextRecognized = {
                if(it.isEmpty()){
                    Toast.makeText(context, "No text detected", Toast.LENGTH_SHORT).show()
                }else{
                    viewModel.toggleBottomSheetText(true)
                    Toast.makeText(context, "Text translated successfully", Toast.LENGTH_SHORT).show()
                }
                translatedText = it },
            onObjectDetectionResult = { detectionResults = it },
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )
    }

    LaunchedEffect(isImageDetectionChecked, targetLanguage) {
        viewModel.updateCameraAnalyzer(
            context = context,
            isImageDetectionChecked = isImageDetectionChecked,
            onTextRecognized = {
                if(it.isEmpty()){
                    Toast.makeText(context, "No text detected", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context, "Text translated successfully", Toast.LENGTH_SHORT).show()
                }
                translatedText = it },
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
            visualSearchResults = visualSearchResults,
            isSearching = isSearching,
            viewModel = viewModel,
            onPreviewSizeChanged = { newSize -> previewSize = newSize },
            onNavigateToSavedSearches = onNavigateToSavedSearches,
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
    translatedText: List<VisualSearchResult>,
    detectionResults: List<Detection>,
    onSwitchModeClicked: () -> Unit,
    visualSearchResults: List<VisualSearchResult>,
    isSearching: Boolean,
    viewModel: HomeViewModel,
    onPreviewSizeChanged: (IntSize) -> Unit,
    onNavigateToSavedSearches: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background

    val showBottomSheet by viewModel.showBottomSheet.collectAsState()

    val showBottomSheetText by viewModel.showBottomSheetText.collectAsState()

    val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels.toFloat()
    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels.toFloat()

    val context = LocalContext.current

    val isLoadingSaving = viewModel.isLoadingSaving.collectAsState()

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
                TextRecognitionOverlay(translatedText, viewModel)

                LanguageSelectionBar(detectedLanguage = "Auto", targetLanguage = viewModel.targetLanguage.collectAsState().value, onTargetLanguageChange = viewModel::setTargetLanguage)

                CaptureButton(
                    onClick = viewModel::triggerFrameAnalysis,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 64.dp)
                )

                ExpandableTranslationResultCard(
                    translationResult = translatedText.firstOrNull(),
                    isLoading = viewModel.shouldAnalyzeFrame.collectAsState().value,
                    showBottomSheet = showBottomSheetText,
                    updateBottomSheet = { viewModel.toggleBottomSheetText(it)},
                    onDismiss = { viewModel.toggleBottomSheetText(false)},
                    onSaveTranslation = {viewModel.saveSearch(context,
                        { translatedText.first() }, detectionResults)}
                )

            }else{
                ObjectRecognitionOverlay(
                    detectionResults = detectionResults
                )

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ExpandableResultCard(
                        results = visualSearchResults,
                        isLoading = isSearching,
                        onDismiss = { viewModel.toggleBottomSheet(false) },
                        showBottomSheet = showBottomSheet,
                        updateBottomSheet = { viewModel.toggleBottomSheet(it)},
                        onSaveImage = {
                                viewModel.capturePhoto(
                                    context = context,
                                    detections = detectionResults,
                                    savedSearchResult = {it}
                                )
                        },
                        onSaveOnlySearch = {
                            viewModel.saveSearch(context, {it}, detectionResults)
                        },
                        isLoadingSaving = isLoadingSaving.value,
                        onViewClick = { viewModel.openUrlInBrowser(context, it) }
                    )
                }
            }
            ControlButtons(
                isChecked = isImageDetectionChecked,
                onCheckedChange = onSwitchModeClicked,
                onBookmarkClick = { onNavigateToSavedSearches() }
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
private fun TextRecognitionOverlay(translatedText: List<VisualSearchResult>, viewModel: HomeViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraTextRecognitionOverlay(
            boxWidthPercentage = 0.8f,
            boxHeightPercentage = 0.2f,
            modifier = Modifier.fillMaxSize(),
            text = "Center text inside the box",
            isLoading = viewModel.shouldAnalyzeFrame.collectAsState().value
        )
    }
}

@Composable
private fun ObjectRecognitionOverlay(detectionResults: List<Detection>) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraTextRecognitionOverlay(
            boxWidthPercentage = 0.8f,
            boxHeightPercentage = 0.5f,
            modifier = Modifier.fillMaxSize(),
            text = "Center object inside the box",
            isLoading = false
        )
        /*
        Uncomment to draw bounding boxes around detected objects
            CameraOverlay(
                detections = detectionResults
            )
         */
    }
}



@Composable
private fun ControlButtons(
    isChecked: Boolean,
    onCheckedChange: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, end = 48.dp, start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.weight(0.8f)) // Push buttons to the right

        BookmarkButton(
            onClick = onBookmarkClick,
            modifier = Modifier.size(40.dp)
        )

        SwitchIconsButton(
            checked = isChecked,
            onCheckedChange = {
                Log.d("APP_LENS", "Switching mode")
                onCheckedChange()
            },
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun BookmarkButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.bookmarks),
            contentDescription = "Bookmarks",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun CaptureButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.8f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}