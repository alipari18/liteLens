package com.example.litelens.presentation.common

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.litelens.R

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
