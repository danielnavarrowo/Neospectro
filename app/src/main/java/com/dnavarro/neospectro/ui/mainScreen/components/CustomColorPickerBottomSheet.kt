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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberSliderState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import com.dnavarro.neospectro.R
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults
import kotlin.math.roundToInt

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomColorPickerBottomSheet(
    initialEdgeColor: Int,
    initialMiddleColor: Int,
    initialCenterColor: Int,
    onDismissRequest: () -> Unit,
    onColorsSelected: (edge: Int, middle: Int, center: Int) -> Unit
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

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

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.custom_theming),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                    val filtered =
                        newValue.uppercase().filter { it in '0'..'9' || it in 'A'..'F' }.take(6)
                    hexText = filtered
                    if (filtered.length == 6) {
                        try {
                            val parsedInt = filtered.toInt(16)
                            val newColor = (0xFF shl 24) or parsedInt
                            updateCurrentColor(newColor)
                        } catch (_: Exception) {
                        }
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
                ColorChannelSlider(
                    label = stringResource(R.string.color_red, red),
                    channelValue = red,
                    accentColor = Color.Red,
                    onChannelChange = { newR ->
                        updateCurrentColor((0xFF shl 24) or (newR shl 16) or (green shl 8) or blue)
                    },
                )
                ColorChannelSlider(
                    label = stringResource(R.string.color_green, green),
                    channelValue = green,
                    accentColor = Color.Green,
                    onChannelChange = { newG ->
                        updateCurrentColor((0xFF shl 24) or (red shl 16) or (newG shl 8) or blue)
                    },
                )
                ColorChannelSlider(
                    label = stringResource(R.string.color_blue, blue),
                    channelValue = blue,
                    accentColor = Color(0xFF3D5AFE),
                    onChannelChange = { newB ->
                        updateCurrentColor((0xFF shl 24) or (red shl 16) or (green shl 8) or newB)
                    },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = {
                        onColorsSelected(edgeColor, middleColor, centerColor)
                        onDismissRequest()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.save_colors))
                }
            }
        }
    }
}

/**
 * Renders one 0..255 RGB channel slider using [SliderState], keeping the thumb synced when the
 * channel value updates externally via tab switches, presets, or hex edits.
 */
@Composable
private fun ColorChannelSlider(
    label: String,
    channelValue: Int,
    accentColor: Color,
    onChannelChange: (Int) -> Unit,
) {
    val latestChannelValue by rememberUpdatedState(channelValue)
    val latestOnChannelChange by rememberUpdatedState(onChannelChange)
    val sliderState = rememberSliderState(
        value = channelValue.toFloat(),
        trackRange = 0f..255f,
    )

    LaunchedEffect(channelValue, sliderState.isDragging) {
        if (!sliderState.isDragging && sliderState.value.roundToInt() != channelValue) {
            sliderState.value = channelValue.toFloat()
        }
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
    )
    Slider(
        state = sliderState,
        onValueChange = { raw ->
            sliderState.value = raw
            val updated = raw.roundToInt().coerceIn(0, 255)
            if (updated != latestChannelValue) {
                latestOnChannelChange(updated)
            }
        },
        colors = SliderDefaults.colors(
            thumbColor = accentColor,
            activeTrackColor = accentColor.copy(alpha = 0.7f),
        ),
    )
}
