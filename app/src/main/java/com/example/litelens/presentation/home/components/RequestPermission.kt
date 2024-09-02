package com.example.litelens.presentation.home.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestPermission() {
    // Camera permission request
    val cameraPermissionState = rememberPermissionState(permission =
        android.Manifest.permission.CAMERA
    )

    // External storage write permission request
    val storagePermissionState = rememberPermissionState(permission =
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    // Check the grant status of permissions
    val isCameraGranted = cameraPermissionState.status.isGranted
    val isStorageGranted = storagePermissionState.status.isGranted

    if (!isCameraGranted || !isStorageGranted) {
        // Launch permission request
        LaunchedEffect(cameraPermissionState, storagePermissionState) {
            cameraPermissionState.launchPermissionRequest()
            storagePermissionState.launchPermissionRequest()

        }
    }
}