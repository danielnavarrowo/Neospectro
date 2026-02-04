package com.dnavarro.neospectro.ui.mainScreen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.dnavarro.neospectro.Constants
import com.dnavarro.neospectro.R
import com.dnavarro.neospectro.ui.mainScreen.components.SelectThemeListItem
import com.dnavarro.neospectro.ui.theme.CustomColors.listItemColors
import com.dnavarro.neospectro.ui.theme.CustomColors.switchColors
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults.bottomListItemShape
import com.dnavarro.neospectro.ui.theme.NeospectroShapeDefaults.topListItemShape
import com.dnavarro.neospectro.ui.theme.NeospectroTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen (contentPadding: PaddingValues) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(Constants.PRENS_NAME, Context.MODE_PRIVATE)
    }
    var selectedTheme by remember {
        mutableStateOf(prefs.getString(Constants.PREF_THEME, Constants.THEME_ICE))
    }
    var reverseColors by remember {
        mutableStateOf(prefs.getBoolean(Constants.PREF_REVERSE_COLORS, false))
    }
    var audioVizEnabled by remember {
        mutableStateOf(prefs.getBoolean(Constants.PREF_AUDIO_VIZ, false))
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // If permission granted, enable the feature
        if (isGranted) {
            audioVizEnabled = true
            prefs.edit { putBoolean(Constants.PREF_AUDIO_VIZ, true) }
        } else {
            // Permission denied, ensure switch is off
            audioVizEnabled = false
            prefs.edit { putBoolean(Constants.PREF_AUDIO_VIZ, false) }
        }
    }

    Scaffold(
        topBar = { Spacer(Modifier.height(contentPadding.calculateTopPadding())) },
        bottomBar = { Spacer(Modifier.height(contentPadding.calculateBottomPadding())) },
        containerColor = MaterialTheme.colorScheme.surfaceDim,

    ) {
        innerPadding ->

        LazyColumn(contentPadding = innerPadding,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)) {
            item {
                SelectThemeListItem(
                    selectedTheme = selectedTheme!!,
                    onThemeSelected = { theme ->
                        selectedTheme = theme
                        prefs.edit { putString(Constants.PREF_THEME, theme) }
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
                                audioVizEnabled = true
                                prefs.edit { putBoolean(Constants.PREF_AUDIO_VIZ, true) }
                            } else {
                                launcher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        } else {
                            // Disabling
                            audioVizEnabled = false
                            prefs.edit { putBoolean(Constants.PREF_AUDIO_VIZ, false) }
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
                    checked = reverseColors,
                    icon = R.drawable.swap,
                    label = R.string.reverse_color_order,
                    description = R.string.reverse_color_order_desc,
                    onClick = {
                        reverseColors = it
                        prefs.edit { putBoolean(Constants.PREF_REVERSE_COLORS, it) }
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
