package com.dnavarro.neospectro.ui.mainScreen

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import com.dnavarro.neospectro.R
import com.dnavarro.neospectro.ui.FormFactorPreviews
import com.dnavarro.neospectro.ui.mainScreen.components.CustomColorPickerDialog
import com.dnavarro.neospectro.ui.mainScreen.components.CustomThemeListItem
import com.dnavarro.neospectro.ui.mainScreen.components.SelectThemeListItem
import com.dnavarro.neospectro.ui.theme.CustomColors.listItemColors
import com.dnavarro.neospectro.ui.theme.CustomColors.switchColors
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults.bottomListItemShape
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults.topListItemShape
import com.dnavarro.neospectro.ui.theme.NeospectroTheme
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val BottomSheetPreviewShape = RoundedCornerShape(12.dp)

private fun decodeSampledBitmapFromFile(
    filePath: String,
    reqWidth: Int,
    reqHeight: Int
): Bitmap? {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeFile(filePath, options)

    val [height: Int, width: Int] = options.run { outHeight to outWidth }
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }

    options.inSampleSize = inSampleSize
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeFile(filePath, options)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    contentPadding: PaddingValues,
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
    var showColorPickerDialog by remember { mutableStateOf(false) }

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
        // If permission granted, enable the feature
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
                            if (bgFile.exists()) {
                                decodeSampledBitmapFromFile(
                                    filePath = bgFile.absolutePath,
                                    reqWidth = 600,
                                    reqHeight = 800
                                )?.asImageBitmap()
                            } else {
                                null
                            }
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

    if (showColorPickerDialog) {
        CustomColorPickerDialog(
            initialEdgeColor = customEdgeColor,
            initialMiddleColor = customMiddleColor,
            initialCenterColor = customCenterColor,
            onDismissRequest = { showColorPickerDialog = false },
            onColorsSelected = { edge, middle, center ->
                viewModel.updateCustomColors(edge, middle, center)
            }
        )
    }

    val isExpanded = currentWindowAdaptiveInfoV2()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

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

    if (isExpanded) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isCustomThemeEnabled) {
                CustomThemeListItem(
                    modifier = Modifier
                        .padding(contentPadding)
                        .weight(1f),
                    edgeColor = customEdgeColor,
                    middleColor = customMiddleColor,
                    centerColor = customCenterColor,
                    onEditColorsClick = { showColorPickerDialog = true }
                )
            } else {
                SelectThemeListItem(
                    modifier = Modifier
                        .padding(contentPadding)
                        .weight(1f),
                    selectedTheme = selectedTheme,
                    onThemeSelected = { theme ->
                        viewModel.updateTheme(theme)
                    }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SettingsItemsSection(
                    audioVizEnabled = audioVizEnabled,
                    hasBgImage = hasBgImage,
                    reverseColors = reverseColors,
                    onAudioVizClick = onAudioVizClick,
                    onHasBgImageClick = { viewModel.updateHasBgImage(it) },
                    onReverseColorsClick = { viewModel.updateReverseColors(it) },
                    onShowBgSheet = { showBgSheet = true }
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (isCustomThemeEnabled) {
                CustomThemeListItem(
                    edgeColor = customEdgeColor,
                    middleColor = customMiddleColor,
                    centerColor = customCenterColor,
                    onEditColorsClick = { showColorPickerDialog = true }
                )
            } else {
                SelectThemeListItem(
                    selectedTheme = selectedTheme,
                    onThemeSelected = { theme ->
                        viewModel.updateTheme(theme)
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            SettingsItemsSection(
                audioVizEnabled = audioVizEnabled,
                hasBgImage = hasBgImage,
                reverseColors = reverseColors,
                onAudioVizClick = onAudioVizClick,
                onHasBgImageClick = { viewModel.updateHasBgImage(it) },
                onReverseColorsClick = { viewModel.updateReverseColors(it) },
                onShowBgSheet = { showBgSheet = true }
            )

            Spacer(Modifier.height(128.dp))
        }
    }
}

@Composable
private fun SettingsItemsSection(
    audioVizEnabled: Boolean,
    hasBgImage: Boolean,
    reverseColors: Boolean,
    onAudioVizClick: (Boolean) -> Unit,
    onHasBgImageClick: (Boolean) -> Unit,
    onReverseColorsClick: (Boolean) -> Unit,
    onShowBgSheet: () -> Unit,
) {
    SettingsSwitchItem(
        icon = R.drawable.graphic_eq,
        label = R.string.enable_audio_visualization,
        description = R.string.enable_audio_visualization_desc,
        checked = audioVizEnabled,
        shape = topListItemShape,
        onCheckedChange = onAudioVizClick
    )
    SettingsSwitchItem(
        icon = R.drawable.palette_outlined,
        label = R.string.background_image,
        description = R.string.background_image_desc,
        checked = hasBgImage,
        shape = RectangleShape,
        onCheckedChange = onHasBgImageClick,
        onRowClick = onShowBgSheet
    )
    SettingsSwitchItem(
        icon = R.drawable.swap,
        label = R.string.reverse_color_order,
        description = R.string.reverse_color_order_desc,
        checked = reverseColors,
        shape = bottomListItemShape,
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
    onCheckedChange: (Boolean) -> Unit,
    onRowClick: (() -> Unit)? = null
) {
    val rowModifier = if (onRowClick != null) {
        Modifier.clickable(onClick = onRowClick)
    } else {
        Modifier.clip(shape)
    }
    ListItem(
        modifier = rowModifier,
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
                            .background(MaterialTheme.colorScheme.outlineVariant)
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
        colors = listItemColors,
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
        colors = switchColors
    )
}

@FormFactorPreviews
@Composable
fun MainScreenPreview() {
    NeospectroTheme {
        Surface {
            SharedTransitionLayout {
                MainScreen(
                    contentPadding = PaddingValues(),
                )
            }
        }
    }
}

