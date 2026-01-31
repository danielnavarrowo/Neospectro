package com.dnavarro.neospectro.ui.mainScreen

import android.content.Context
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.dnavarro.neospectro.Constants
import com.dnavarro.neospectro.ui.mainScreen.components.SelectThemeListItem
import com.dnavarro.neospectro.ui.theme.NeospectroTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SharedTransitionScope.MainScreen (contentPadding: PaddingValues) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(Constants.PRENS_NAME, Context.MODE_PRIVATE)
    }
    var selectedTheme by remember {
        mutableStateOf(prefs.getString(Constants.PREF_THEME, Constants.THEME_ICE))
    }


    Scaffold(
        topBar = { Spacer(Modifier.height(contentPadding.calculateTopPadding())) },
        bottomBar = { Spacer(Modifier.height(contentPadding.calculateBottomPadding())) },

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
    NeospectroTheme() {
        Surface {
            SharedTransitionLayout {
                MainScreen(
                    contentPadding = PaddingValues(),
                )
            }
        }
    }
}



