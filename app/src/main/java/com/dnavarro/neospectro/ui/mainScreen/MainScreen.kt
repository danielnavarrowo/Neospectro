package com.dnavarro.neospectro.ui.mainScreen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dnavarro.neospectro.R
import com.dnavarro.neospectro.ui.FormFactorPreviews
import com.dnavarro.neospectro.ui.mainScreen.components.CustomColorPickerBottomSheet
import com.dnavarro.neospectro.ui.mainScreen.components.ThemeSelectorStrip
import com.dnavarro.neospectro.ui.theme.GlassDefaults
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults.bottomListItemShape
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults.cardShape
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults.topListItemShape
import com.dnavarro.neospectro.ui.theme.NeospectroTheme
import com.dnavarro.neospectro.ui.theme.frostedGlass
import com.dnavarro.neospectro.utils.decodeSampledBitmapFromFile
import dev.chrisbanes.haze.HazeState
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val BottomSheetPreviewShape = RoundedCornerShape(12.dp)
private val MaxControlsWidth = 560.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    contentPadding: PaddingValues,
    hazeState: HazeState? = null,
    onEnterZenMode: () -> Unit = {},
    viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory)
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()
    val selectedTheme = uiState.selectedTheme
    val reverseColors = uiState.reverseColors
    val audioVizEnabled = uiState.audioVizEnabled
    val hasBgImage = uiState.hasBgImage
    val bgImageTrigger = uiState.bgImageTrigger
    val isCustomThemeEnabled = uiState.isCustomThemeEnabled
    val customEdgeColor = uiState.customEdgeColor
    val customMiddleColor = uiState.customMiddleColor
    val customCenterColor = uiState.customCenterColor

    var showBgSheet by remember { mutableStateOf(false) }
    var showColorPickerSheet by remember { mutableStateOf(false) }
    var showZenHint by remember { mutableStateOf(false) }
    var zenHintTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(showZenHint, zenHintTrigger) {
        if (showZenHint) {
            delay(3000.milliseconds)
            showZenHint = false
        }
    }

    val bgImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.handleBgImagePicked(uri)
            showBgSheet = false
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        viewModel.updateAudioVizEnabled(isGranted)
    }

    if (showBgSheet) {
        val sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden
        )
        ModalBottomSheet(
            onDismissRequest = { showBgSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.background_image),
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                val bottomSheetBitmap by produceState<ImageBitmap?>(
                    initialValue = null,
                    hasBgImage,
                    bgImageTrigger
                ) {
                    value = if (!hasBgImage) {
                        null
                    } else {
                        withContext(Dispatchers.IO) {
                            val bgFile = File(context.filesDir, "bg_image.jpg")
                            decodeSampledBitmapFromFile(
                                file = bgFile,
                                reqWidth = 600,
                                reqHeight = 800
                            )?.asImageBitmap()
                        }
                    }
                }

                val currentBitmap = bottomSheetBitmap
                if (currentBitmap != null) {
                    Image(
                        bitmap = currentBitmap,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth(.6f)
                            .height(400.dp)
                            .clip(BottomSheetPreviewShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                BottomSheetPreviewShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.background_image))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { bgImageLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.choose_image))
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showColorPickerSheet) {
        CustomColorPickerBottomSheet(
            initialEdgeColor = customEdgeColor,
            initialMiddleColor = customMiddleColor,
            initialCenterColor = customCenterColor,
            onDismissRequest = { showColorPickerSheet = false },
            onColorsSelected = { edge, middle, center ->
                viewModel.updateCustomColors(edge, middle, center)
                viewModel.updateCustomThemeEnabled(true)
            }
        )
    }

    val onAudioVizClick: (Boolean) -> Unit = { enabled ->
        if (enabled) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                viewModel.updateAudioVizEnabled(true)
            } else {
                launcher.launch(Manifest.permission.RECORD_AUDIO)
            }
        } else {
            viewModel.updateAudioVizEnabled(false)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    showZenHint = true
                    zenHintTrigger++
                },
                onLongClick = {
                    showZenHint = false
                    onEnterZenMode()
                }
            )
            .padding(contentPadding)
    ) {
        val minContentHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minContentHeight)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = MaxControlsWidth)
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ThemeSelectorStrip(
                    selectedTheme = selectedTheme,
                    isCustomThemeEnabled = isCustomThemeEnabled,
                    reverseColors = reverseColors,
                    customEdgeColor = customEdgeColor,
                    customMiddleColor = customMiddleColor,
                    customCenterColor = customCenterColor,
                    hazeState = hazeState,
                    onPresetSelected = { theme ->
                        viewModel.updateCustomThemeEnabled(false)
                        viewModel.updateTheme(theme)
                    },
                    onCustomClick = { showColorPickerSheet = true },
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(onTap = {}, onLongPress = {})
                    }
                )

                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SettingsSwitchItem(
                        icon = R.drawable.graphic_eq,
                        label = R.string.enable_audio_visualization,
                        description = R.string.enable_audio_visualization_desc,
                        checked = audioVizEnabled,
                        shape = cardShape,
                        hazeState = hazeState,
                        onCheckedChange = onAudioVizClick
                    )
                }
            }

            // Keep the center open so the live wave remains unobstructed
            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = MaxControlsWidth)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = showZenHint,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.tap_and_hold_zen_hint),
                        color = Color.White.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.85f),
                                blurRadius = 8f
                            )
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                BottomSettingsItemsSection(
                    hasBgImage = hasBgImage,
                    reverseColors = reverseColors,
                    hazeState = hazeState,
                    onHasBgImageClick = { viewModel.updateHasBgImage(it) },
                    onReverseColorsClick = { viewModel.updateReverseColors(it) },
                    onShowBgSheet = { showBgSheet = true }
                )
            }
        }
    }
}

@Composable
private fun BottomSettingsItemsSection(
    hasBgImage: Boolean,
    reverseColors: Boolean,
    hazeState: HazeState?,
    onHasBgImageClick: (Boolean) -> Unit,
    onReverseColorsClick: (Boolean) -> Unit,
    onShowBgSheet: () -> Unit,
) {
    SettingsSwitchItem(
        icon = R.drawable.palette_outlined,
        label = R.string.background_image,
        description = R.string.background_image_desc,
        checked = hasBgImage,
        shape = topListItemShape,
        hazeState = hazeState,
        onCheckedChange = onHasBgImageClick,
        onRowClick = onShowBgSheet
    )
    SettingsSwitchItem(
        icon = R.drawable.swap,
        label = R.string.reverse_color_order,
        description = R.string.reverse_color_order_desc,
        checked = reverseColors,
        shape = bottomListItemShape,
        hazeState = hazeState,
        onCheckedChange = onReverseColorsClick
    )
}

@Composable
private fun SettingsSwitchItem(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    @StringRes description: Int,
    checked: Boolean,
    shape: Shape,
    hazeState: HazeState?,
    onCheckedChange: (Boolean) -> Unit,
    onRowClick: (() -> Unit)? = null
) {
    val itemModifier = Modifier
        .fillMaxWidth()
        .frostedGlass(hazeState = hazeState, shape = shape)
        .let { base ->
            if (onRowClick != null) {
                base.combinedClickable(onClick = onRowClick, onLongClick = {})
            } else {
                base.pointerInput(Unit) {
                    detectTapGestures(onTap = {}, onLongPress = {})
                }
            }
        }

    ListItem(
        modifier = itemModifier,
        leadingContent = {
            Icon(painterResource(icon), contentDescription = null)
        },
        trailingContent = {
            if (onRowClick != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(painterResource(R.drawable.chevron), contentDescription = null)

                    Spacer(
                        modifier = Modifier
                            .height(32.dp)
                            .width(1.dp)
                            .background(GlassDefaults.DividerColor)
                    )

                    SettingsThumbSwitch(
                        checked = checked,
                        onCheckedChange = onCheckedChange
                    )
                }
            } else {
                SettingsThumbSwitch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            }
        },
        supportingContent = { Text(stringResource(description)) },
        colors = GlassDefaults.glassListItemColors,
        content = {
            Text(
                stringResource(label),
                style = MaterialTheme.typography.titleMedium
            )
        },
    )
}

@Composable
private fun SettingsThumbSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        thumbContent = {
            Icon(
                painter = painterResource(if (checked) R.drawable.check else R.drawable.clear),
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize),
            )
        },
        colors = GlassDefaults.glassSwitchColors
    )
}

@FormFactorPreviews
@Composable
fun MainScreenPreview() {
    NeospectroTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            MainScreen(
                contentPadding = PaddingValues(),
            )
        }
    }
}
