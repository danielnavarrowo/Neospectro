package com.dnavarro.neospectro.ui

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.window.core.layout.WindowSizeClass
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.dnavarro.neospectro.R
import com.dnavarro.neospectro.services.LWPService
import com.dnavarro.neospectro.ui.infoScreen.InfoScreen
import com.dnavarro.neospectro.ui.mainScreen.MainScreen
import com.dnavarro.neospectro.ui.zenScreen.ZenScreen
import com.dnavarro.neospectro.utils.onBack

@Composable
fun AppScreen(
) {
    val context = LocalContext.current
    var isLwpSet by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LifecycleResumeEffect(Unit) {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val info = wallpaperManager.wallpaperInfo
        isLwpSet =
            info != null && info.component == ComponentName(context, LWPService::class.java)
        onPauseOrDispose { }
    }

    val backStack = rememberNavBackStack(Screen.Main)
    val isZenMode by remember {
        derivedStateOf { backStack.lastOrNull() == Screen.Zen }
    }
    val motionScheme = motionScheme

    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val isExpanded = adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(
        WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
    )
    val isLarge = adaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(
        WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND
    )
    val navLayoutType = when {
        isZenMode -> NavigationSuiteType.None
        isLarge -> NavigationSuiteType.WideNavigationRailExpanded
        isExpanded -> NavigationSuiteType.WideNavigationRailCollapsed
        else -> NavigationSuiteType.None
    }

    val onNavigateTo: (Screen) -> Unit = { targetRoute ->
        if (targetRoute == Screen.Main) {
            if (backStack.size > 1) {
                backStack.removeAt(1)
            }
        } else {
            if (backStack.size < 2) {
                backStack.add(targetRoute)
            } else {
                backStack[1] = targetRoute
            }
        }
    }

    val currentRoute = backStack.lastOrNull()
    LaunchedEffect(currentRoute) {
        scrollBehavior.state.heightOffset = 0f
        scrollBehavior.state.contentOffset = 0f
    }

    NavigationSuiteScaffold(
        layoutType = navLayoutType,
        navigationSuiteItems = {
            mainScreens.fastForEach { item ->
                item(
                    selected = backStack.lastOrNull() == item.route,
                    onClick = { onNavigateTo(item.route) },
                    icon = {
                        Icon(
                            painterResource(item.selectedIcon),
                            stringResource(item.label)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(item.label),
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 12.sp,
                                maxFontSize = 16.sp
                            ),
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                )
            }
        },
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.surfaceDim,
            topBar = {
                if (!isZenMode) {
                    TopAppBar(
                        scrollBehavior = scrollBehavior,
                        title = {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.displaySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 24.dp)
                            )
                        },
                        subtitle = {},
                        titleHorizontalAlignment = CenterHorizontally,
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceDim
                        )
                    )
                }
            },
            bottomBar = {
                if (!isExpanded) {
                    AnimatedVisibility(
                        visible = !isZenMode,
                        enter = slideInVertically(motionScheme.slowSpatialSpec()) { it },
                        exit = slideOutVertically(motionScheme.slowSpatialSpec()) { it }
                    ) {
                        FloatingNavigationToolbar(
                            currentRoute = backStack.lastOrNull(),
                            onNavigate = onNavigateTo
                        )
                    }
                }
            },
            floatingActionButton =
                {
                    if (!isLwpSet && !isZenMode) {
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
                            Text(
                                stringResource(R.string.apply),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                },

            ) { contentPadding ->
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

                    entry<Screen.Zen> {
                        ZenScreen(
                            onExit = {
                                if (backStack.size > 1) backStack.removeAt(1)
                            }
                        )
                    }

                    entry<Screen.Info> {
                        InfoScreen(
                            contentPadding = contentPadding
                        )
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloatingNavigationToolbar(
    currentRoute: Any?,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val motionScheme = motionScheme
    val cutoutInsets = WindowInsets.displayCutout.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    val systemBarsInsets = WindowInsets.systemBars.asPaddingValues()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = cutoutInsets.calculateStartPadding(layoutDirection),
                end = cutoutInsets.calculateEndPadding(layoutDirection)
            ),
        contentAlignment = Alignment.Center
    ) {
        HorizontalFloatingToolbar(
            expanded = true,
            modifier = Modifier
                .padding(
                    top = ScreenOffset,
                    bottom = systemBarsInsets.calculateBottomPadding() + ScreenOffset
                )
                .zIndex(1f)
        ) {
            mainScreens.fastForEach { item ->
                val selected = currentRoute == item.route
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Above
                    ),
                    tooltip = { PlainTooltip { Text(stringResource(item.label)) } },
                    state = rememberTooltipState(),
                ) {
                    ToggleButton(
                        checked = selected,
                        onCheckedChange = { onNavigate(item.route) },
                        shapes = ToggleButtonShapes(
                            CircleShape,
                            CircleShape,
                            CircleShape
                        ),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Crossfade(selected) { isSelected ->
                                if (isSelected) {
                                    Icon(
                                        painterResource(item.selectedIcon),
                                        stringResource(item.label)
                                    )
                                } else {
                                    Icon(
                                        painterResource(item.unselectedIcon),
                                        stringResource(item.label)
                                    )
                                }
                            }
                            AnimatedVisibility(
                                visible = selected,
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