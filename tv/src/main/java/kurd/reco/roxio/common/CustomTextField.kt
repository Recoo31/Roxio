package kurd.reco.roxio.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ShapeDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import kurd.reco.roxio.ui.theme.RoxioTheme

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    placeholderStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    focusRequester: FocusRequester = remember { FocusRequester() },
    surfaceContainerColor: Color = MaterialTheme.colorScheme.surface,
    leadingIcon: @Composable (() -> Unit)? = null,
    onFocusChange: ((Boolean) -> Unit)? = null,
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape = ShapeDefaults.ExtraSmall),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f),
        tonalElevation = 2.dp,
        onClick = { focusRequester.requestFocus() },
        colors = ClickableSurfaceDefaults.colors(
            containerColor = surfaceContainerColor,
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.invoke()

            Box {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { isFocused = it.hasFocus },
                    enabled = enabled,
                    readOnly = readOnly,
                    textStyle = textStyle.copy(
                        color = if (enabled)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    ),
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    singleLine = singleLine,
                    maxLines = maxLines,
                )

                if (value.isEmpty() && !isFocused) {
                    Text(
                        text = placeholder,
                        style = placeholderStyle,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CustomTextFieldPreview() {
    RoxioTheme {
        CustomTextField(value = "", onValueChange = {}, placeholder = "Placeholder", leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.padding(end = 16.dp)) })
    }
}