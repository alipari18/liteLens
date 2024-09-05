package com.example.litelens.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.litelens.R
import com.example.litelens.theme.Shapes

@Composable
fun SwitchIconsButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    checkedIcon: Int = R.drawable.frame_inspect_icon,
    uncheckedIcon: Int = R.drawable.translate_icon,
) {

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.scale(1.2f),
        thumbContent = if (checked) {
            {
                Icon(
                    painter = painterResource(id = checkedIcon),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            {
                Icon(
                    painter = painterResource(id = uncheckedIcon),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }

        }
    )

}


@Preview(showBackground = true, widthDp = 360, heightDp = 640, backgroundColor = 0xFFFFFFFF)
@Composable
fun SwitchIconsButtonPreview() {
    SwitchIconsButton(
        checked = false,
        onCheckedChange = { }
    )
}
