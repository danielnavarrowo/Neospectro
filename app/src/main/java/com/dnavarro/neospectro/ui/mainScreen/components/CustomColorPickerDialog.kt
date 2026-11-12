package com.dnavarro.neospectro.ui.mainScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
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
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.DialogProperties
import com.dnavarro.neospectro.R
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults

private val PRESET_COLORS = listOf(
    Color.Red, Color(0xFFFF8000), Color.Yellow, Color.Green,
    Color.Cyan, Color(0xFF0303FF), Color(0xFF8000FF), Color.Magenta,
    Color.White, Color.Black
)

private val TAB_LABEL_RES_IDS = intArrayOf(
    R.string.edge,
    R.string.middle,
    R.string.center
)

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

    var hexText by remember {
        mutableStateOf(String.format("%06X", 0xFFFFFF and currentColor))
    }

    val updateCurrentColor: (Int) -> Unit = { newColor ->
        when (selectedTab) {
            0 -> edgeColor = newColor
            1 -> middleColor = newColor
            2 -> centerColor = newColor
        }
        hexText = String.format("%06X", 0xFFFFFF and newColor)
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
                                .drawBehind {
                                    drawRect(Color(currentColor))
                                }
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
                                .drawWithCache {
                                    val brush = Brush.verticalGradient(
                                        listOf(
                                            Color(edgeColor),
                                            Color(middleColor),
                                            Color(centerColor),
                                            Color(middleColor),
                                            Color(edgeColor)
                                        )
                                    )
                                    onDrawBehind {
                                        drawRect(brush)
                                    }
                                }
                        )
                    }

                }

                // Connected ToggleButtons for Edge / Middle / Center
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TAB_LABEL_RES_IDS.forEachIndexed { index, labelResId ->
                        val isSelected = selectedTab == index
                        ToggleButton(
                            checked = isSelected,
                            onCheckedChange = {
                                selectedTab = index
                                val tabColor = when (index) {
                                    0 -> edgeColor
                                    1 -> middleColor
                                    else -> centerColor
                                }
                                hexText = String.format("%06X", 0xFFFFFF and tabColor)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { role = Role.RadioButton },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                TAB_LABEL_RES_IDS.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                        ) {
                            Text(
                                stringResource(labelResId),
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
                val swatchShape = MaterialShapes.Cookie12Sided.toShape()
                val secondaryColor = MaterialTheme.colorScheme.secondary
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    PRESET_COLORS.fastForEach { color ->
                        val colorArgb = color.toArgb()
                        val isSelectedSwatch = currentColor == colorArgb
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(swatchShape)
                                .background(color)
                                .border(
                                    if (isSelectedSwatch) 2.dp else 0.dp,
                                    if (isSelectedSwatch) secondaryColor else color,
                                    swatchShape
                                )
                                .clickable { updateCurrentColor(colorArgb) }
                        )
                    }
                }

                // HEX Color TextField
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
                            val newR = r.toInt()
                            if (newR != red) {
                                val updated = (0xFF shl 24) or (newR shl 16) or (green shl 8) or blue
                                updateCurrentColor(updated)
                            }
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
                            val newG = g.toInt()
                            if (newG != green) {
                                val updated = (0xFF shl 24) or (red shl 16) or (newG shl 8) or blue
                                updateCurrentColor(updated)
                            }
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
                            val newB = b.toInt()
                            if (newB != blue) {
                                val updated = (0xFF shl 24) or (red shl 16) or (green shl 8) or newB
                                updateCurrentColor(updated)
                            }
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

