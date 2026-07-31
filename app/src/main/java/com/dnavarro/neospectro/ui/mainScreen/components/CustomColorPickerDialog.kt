package com.dnavarro.neospectro.ui.mainScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.dnavarro.neospectro.R
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomColorPickerDialog(
    initialEdgeColor: Int,
    initialMiddleColor: Int,
    initialCenterColor: Int,
    onDismissRequest: () -> Unit,
    onColorsSelected: (edge: Int, middle: Int, center: Int) -> Unit
) {
    var edgeColor by remember { mutableIntStateOf(initialEdgeColor) }
    var middleColor by remember { mutableIntStateOf(initialMiddleColor) }
    var centerColor by remember { mutableIntStateOf(initialCenterColor) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Edge, 1: Middle, 2: Center

    val currentColor = when (selectedTab) {
        0 -> edgeColor
        1 -> middleColor
        else -> centerColor
    }

    val updateCurrentColor: (Int) -> Unit = { newColor ->
        when (selectedTab) {
            0 -> edgeColor = newColor
            1 -> middleColor = newColor
            2 -> centerColor = newColor
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = {
                    onColorsSelected(edgeColor, middleColor, centerColor)
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.save_colors))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.custom_theming),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.current_color),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(NeospectroShapeDefaults.cardShape)
                                .background(
                                    Color(currentColor)
                                )
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.gradient_preview),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(NeospectroShapeDefaults.cardShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(edgeColor),
                                            Color(middleColor),
                                            Color(centerColor),
                                            Color(middleColor),
                                            Color(edgeColor)
                                        )
                                    )
                                )
                        )
                    }

                }



                // Connected ToggleButtons for Edge / Middle / Center
                val tabOptions = listOf(
                    0 to stringResource(R.string.edge),
                    1 to stringResource(R.string.middle),
                    2 to stringResource(R.string.center)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabOptions.forEachIndexed { index, option ->
                        val isSelected = selectedTab == option.first
                        ToggleButton(
                            checked = isSelected,
                            onCheckedChange = { selectedTab = option.first },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { role = Role.RadioButton },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                tabOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                        ) {
                            Text(
                                option.second,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Preset Swatches
                Text(
                    text = stringResource(R.string.quick_presets),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val presetColors = listOf(
                    Color.Red, Color(0xFFFF8000), Color.Yellow, Color.Green,
                    Color.Cyan, Color(0xFF0303FF), Color(0xFF8000FF), Color.Magenta,
                    Color.White, Color.Black
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetColors.forEach { color ->
                        item {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(MaterialShapes.Cookie12Sided.toShape())
                                    .background(color)
                                    .border(
                                        if (currentColor == color.toArgb()) 2.dp else 0.dp,
                                        if (currentColor == color.toArgb()) MaterialTheme.colorScheme.secondary else color,
                                        MaterialShapes.Cookie12Sided.toShape()
                                    )
                                    .clickable { updateCurrentColor(color.toArgb()) }
                            )
                        }
                    }
                }

                // HEX Color TextField
                var hexText by remember(currentColor) {
                    mutableStateOf(String.format("%06X", 0xFFFFFF and currentColor))
                }

                OutlinedTextField(
                    value = hexText,
                    onValueChange = { newValue ->
                        val filtered = newValue.uppercase().filter { it in '0'..'9' || it in 'A'..'F' }.take(6)
                        hexText = filtered
                        if (filtered.length == 6) {
                            try {
                                val parsedInt = filtered.toInt(16)
                                val newColor = (0xFF shl 24) or parsedInt
                                updateCurrentColor(newColor)
                            } catch (_: Exception) {}
                        }
                    },
                    label = { Text(stringResource(R.string.hex_color)) },
                    prefix = { Text("#") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // RGB Sliders
                val red = (currentColor shr 16) and 0xFF
                val green = (currentColor shr 8) and 0xFF
                val blue = currentColor and 0xFF

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.color_red, red),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Slider(
                        value = red.toFloat(),
                        onValueChange = { r ->
                            val updated = (0xFF shl 24) or (r.toInt() shl 16) or (green shl 8) or blue
                            updateCurrentColor(updated)
                        },
                        valueRange = 0f..255f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Red,
                            activeTrackColor = Color.Red.copy(alpha = 0.7f)
                        )
                    )

                    Text(
                        text = stringResource(R.string.color_green, green),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Slider(
                        value = green.toFloat(),
                        onValueChange = { g ->
                            val updated = (0xFF shl 24) or (red shl 16) or (g.toInt() shl 8) or blue
                            updateCurrentColor(updated)
                        },
                        valueRange = 0f..255f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Green,
                            activeTrackColor = Color.Green.copy(alpha = 0.7f)
                        )
                    )

                    Text(
                        text = stringResource(R.string.color_blue, blue),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Slider(
                        value = blue.toFloat(),
                        onValueChange = { b ->
                            val updated = (0xFF shl 24) or (red shl 16) or (green shl 8) or b.toInt()
                            updateCurrentColor(updated)
                        },
                        valueRange = 0f..255f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF3D5AFE),
                            activeTrackColor = Color(0xFF3D5AFE).copy(alpha = 0.7f)
                        )
                    )
                }
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(16.dp)
    )
}
