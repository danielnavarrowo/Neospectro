package com.dnavarro.neospectro.ui

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.dnavarro.neospectro.R
import com.dnavarro.neospectro.services.LWPService
import com.dnavarro.neospectro.ui.mainScreen.MainScreen
import com.dnavarro.neospectro.utils.onBack

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3AdaptiveApi::class
)
@Composable
fun AppScreen(
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var isLwpSet by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val wallpaperManager = WallpaperManager.getInstance(context)
                val info = wallpaperManager.wallpaperInfo
                isLwpSet = info != null && info.component == ComponentName(context, LWPService::class.java)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val backStack = rememberNavBackStack(Screen.Main)
    val motionScheme = motionScheme
    val cutoutInsets = WindowInsets.displayCutout.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    val systemBarsInsets = WindowInsets.systemBars.asPaddingValues()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceDim,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.displaySmall,
                        textAlign = TextAlign.Center
                    )
                },
                subtitle = {},
                titleHorizontalAlignment = CenterHorizontally,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceDim)


            )
        },
        bottomBar = {
            AnimatedVisibility(
                true,
                enter = slideInVertically(motionScheme.slowSpatialSpec()) { it },
                exit = slideOutVertically(motionScheme.slowSpatialSpec()) { it }
            ) {
                val wide = remember(windowSizeClass) {
                    windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = cutoutInsets.calculateStartPadding(layoutDirection),
                            end = cutoutInsets.calculateEndPadding(layoutDirection)
                        ),
                    Alignment.Center
                ) {
                    HorizontalFloatingToolbar(
                        expanded = true,
                        modifier = Modifier
                            .padding(
                                top = ScreenOffset,
                                bottom = systemBarsInsets.calculateBottomPadding()
                                        + ScreenOffset
                            )
                            .zIndex(1f)

                    ) {
                        mainScreens.fastForEach { item ->
                            val selected by remember {
                                derivedStateOf { backStack.lastOrNull() == item.route }
                            }
                            TooltipBox(
                                positionProvider =
                                    TooltipDefaults.rememberTooltipPositionProvider(
                                        TooltipAnchorPosition.Above
                                    ),
                                tooltip = { PlainTooltip { Text(stringResource(item.label)) } },
                                state = rememberTooltipState(),
                            )
                            {
                                ToggleButton(
                                    checked = selected,
                                    onCheckedChange = if (item.route != Screen.Main) {
                                        {
                                            if (backStack.size < 2) backStack.add(item.route)
                                            else backStack[1] = item.route
                                        }
                                    } else {
                                        { if (backStack.size > 1) backStack.removeAt(1) }
                                    },
                                    shapes = ToggleButtonDefaults.shapes(
                                        CircleShape,
                                        CircleShape,
                                        CircleShape
                                    ),
                                    modifier = Modifier.height(56.dp)
                                    
                                    
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Crossfade(selected) {
                                            if (it) Icon(
                                                painterResource(item.selectedIcon),
                                                stringResource(item.label)
                                            )
                                            else Icon(
                                                painterResource(item.unselectedIcon),
                                                stringResource(item.label)
                                            )
                                        }
                                        AnimatedVisibility(
                                            visible = selected || wide,
                                            enter = expandHorizontally(motionScheme.defaultSpatialSpec()),
                                            exit = shrinkHorizontally(motionScheme.defaultSpatialSpec())
                                        ) {
                                            Text(
                                                text = stringResource(item.label),
                                                fontSize = 16.sp,
                                                lineHeight = 24.sp,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Clip,
                                                modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton =
            {
                if (!isLwpSet) {
                    MediumExtendedFloatingActionButton(
                        onClick = {
                            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                            intent.putExtra(
                                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                ComponentName(context, LWPService::class.java)
                            )
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.apply_outlined),
                            contentDescription = "Apply",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Text(stringResource(R.string.apply),
                            style = MaterialTheme.typography.titleMedium)
                    }
                }
            },

    ){
        contentPadding ->
        SharedTransitionLayout {
            NavDisplay(
                backStack = backStack,
                onBack = backStack::onBack,
                transitionSpec = {
                    fadeIn(motionScheme.defaultEffectsSpec())
                        .togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
                },
                popTransitionSpec = {
                    fadeIn(motionScheme.defaultEffectsSpec())
                        .togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
                },
                predictivePopTransitionSpec = {
                    fadeIn(motionScheme.defaultEffectsSpec())
                        .togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
                },
                entryProvider = entryProvider {
                    entry<Screen.Main> {
                        MainScreen(
                            contentPadding = contentPadding
                        )
                    }

                    entry <Screen.Info> {

                    }
                }
            )
        }
    }


}