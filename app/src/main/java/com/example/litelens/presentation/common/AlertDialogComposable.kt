package com.example.litelens.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.litelens.domain.model.VisualSearchResult

@Composable
fun AlertDialogComposable(
    title: String,
    message: String,
    firstButtonText: String,
    icon: ImageVector,
    result: VisualSearchResult?,
    onDismissRequest: () -> Unit,
    onSaveImage: (VisualSearchResult) -> Unit,
    showThirdButton: Boolean = false,
    onThirdButtonClick: (VisualSearchResult) -> Unit,
    isLoading: Boolean
) {
    if (result == null) return

    AlertDialog(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = "Hearth icon",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        },
        title = {
            Text(text = title)
        },
        text = {
            Column {
                Text(text = result.title ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = message)

            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {


            Row {
                TextButton(
                    onClick = {
                        onSaveImage(result)
                    },
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text(firstButtonText)
                    }

                }
                if (showThirdButton) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            // Add your custom action here
                            onThirdButtonClick(result)
                        }
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Text("Save only in app")
                        }
                    }
                }
            }
        },
        dismissButton = {
                TextButton(
                    onClick = onDismissRequest
                ) {
                    Text("Cancel")
                }

        }
    )
}