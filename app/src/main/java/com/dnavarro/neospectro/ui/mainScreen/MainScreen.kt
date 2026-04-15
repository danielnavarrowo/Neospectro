package com.dnavarro.neospectro.ui.mainScreen

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dnavarro.neospectro.R
import com.dnavarro.neospectro.ui.mainScreen.components.SelectThemeListItem
import com.dnavarro.neospectro.ui.theme.CustomColors.listItemColors
import com.dnavarro.neospectro.ui.theme.CustomColors.switchColors
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults.bottomListItemShape
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults.topListItemShape
import com.dnavarro.neospectro.ui.theme.NeospectroTheme
import java.io.File
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen (
    contentPadding: PaddingValues,
    viewModel: MainViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val reverseColors by viewModel.reverseColors.collectAsState()
    val audioVizEnabled by viewModel.audioVizEnabled.collectAsState()
    val hasBgImage by viewModel.hasBgImage.collectAsState()
    val bgImageTrigger by viewModel.bgImageTrigger.collectAsState()

    var showBgSheet by remember { mutableStateOf(false) }

    val bgImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.handleBgImagePicked(uri, context)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // If permission granted, enable the feature
        viewModel.updateAudioVizEnabled(isGranted)
    }

    Scaffold(
        topBar = { Spacer(Modifier.height(contentPadding.calculateTopPadding())) },
        bottomBar = { Spacer(Modifier.height(contentPadding.calculateBottomPadding())) },
        containerColor = MaterialTheme.colorScheme.surfaceDim,

    ) {
        innerPadding ->

        if (showBgSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showBgSheet = false },
                sheetState = sheetState
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.background_image), style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))

                    val bgFile = File(context.filesDir, "bg_image.jpg")
                    var bottomSheetBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

                    androidx.compose.runtime.LaunchedEffect(hasBgImage, bgImageTrigger) {
                        bottomSheetBitmap = if (bgFile.exists()) {
                            BitmapFactory.decodeFile(bgFile.absolutePath)?.asImageBitmap()
                        } else {
                            null
                        }
                    }

                    if (bottomSheetBitmap != null) {
                        Image(
                            bitmap = bottomSheetBitmap!!,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth(.6f)
                                .height(400.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
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
                        Text(stringResource(R.string.choose_image
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        





                        ))
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        LazyColumn(contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)) {
            item {
                SelectThemeListItem(
                    selectedTheme = selectedTheme,
                    onThemeSelected = { theme ->
                        viewModel.updateTheme(theme)
                    }
                )
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
            }

            item {
                val item = SettingsSwitch(
                    checked = audioVizEnabled,
                    icon = R.drawable.graphic_eq,
                    label = R.string.enable_audio_visualization,
                    description = R.string.enable_audio_visualization_desc,
                    onClick = {
                        if (it) {
                            // Trying to enable
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
                            // Disabling
                            viewModel.updateAudioVizEnabled(false)
                        }
                    }
                )
                ListItem(
                    leadingContent = {
                        Icon(painterResource(item.icon), contentDescription = null)
                    },
                    headlineContent = { Text(stringResource(item.label), style = MaterialTheme.typography.titleMedium) },
                    supportingContent = { Text(stringResource(item.description))},
                    trailingContent = {
                        Switch(
                            checked = item.checked,
                            onCheckedChange = { item.onClick(it)},
                            thumbContent = {
                                if (
                                    item.checked
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.clear),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            },
                            colors = switchColors
                        )
                    },
                    colors = listItemColors,
                    modifier = Modifier.clip(topListItemShape)
                )
            }
            item {
                val item = SettingsSwitch(
                    checked = hasBgImage,
                    icon = R.drawable.palette_outlined,
                    label = R.string.background_image,
                    description = R.string.background_image_desc,
                    onClick = {
                        viewModel.updateHasBgImage(it)
                    }
                )
                ListItem(
                    leadingContent = {
                        Icon(painterResource(item.icon), contentDescription = null)
                    },
                    headlineContent = { Text(stringResource(item.label), style = MaterialTheme.typography.titleMedium) },
                    supportingContent = { Text(stringResource(item.description))},
                    trailingContent = {
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

                            Switch(
                                checked = item.checked,
                                onCheckedChange = { item.onClick(it) },
                                thumbContent = {
                                    if (item.checked) {
                                        Icon(
                                            painter = painterResource(R.drawable.check),
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(R.drawable.clear),
                                            contentDescription = null,
                                            modifier = Modifier.size(SwitchDefaults.IconSize),
                                        )
                                    }
                                },
                                colors = switchColors
                            )
                        }
                    },
                    colors = listItemColors,
                    modifier = Modifier.clickable { showBgSheet = true }
                )
            }
            item {
                val item = SettingsSwitch(
                    checked = reverseColors,
                    icon = R.drawable.swap,
                    label = R.string.reverse_color_order,
                    description = R.string.reverse_color_order_desc,
                    onClick = {
                        viewModel.updateReverseColors(it)
                    }
                )
                ListItem(
                    leadingContent = {
                        Icon(painterResource(item.icon), contentDescription = null)
                    },
                    headlineContent = { Text(stringResource(item.label), style = MaterialTheme.typography.titleMedium) },
                    supportingContent = { Text(stringResource(item.description))},
                    trailingContent = {
                        Switch(
                            checked = item.checked,
                            onCheckedChange = { item.onClick(it)},
                            thumbContent = {
                                if (
                                    item.checked
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.clear),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            },
                            colors = switchColors
                        )
                    },
                    colors = listItemColors,
                    modifier = Modifier.clip(bottomListItemShape)
                )
            }
            item {
                Spacer(Modifier.height(128.dp))
            }
        }
    }
    }

@Preview(
    showSystemUi = true,
    device = Devices.PIXEL_9_PRO
)
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
