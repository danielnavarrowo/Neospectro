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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.dnavarro.neospectro.R
import com.dnavarro.neospectro.services.LWPService
import com.dnavarro.neospectro.ui.components.LiveWaveBackground
import com.dnavarro.neospectro.ui.infoScreen.InfoScreen
import com.dnavarro.neospectro.ui.mainScreen.MainScreen
import com.dnavarro.neospectro.ui.mainScreen.MainViewModel
import com.dnavarro.neospectro.ui.theme.GlassDefaults
import com.dnavarro.neospectro.ui.theme.frostedGlass
import com.dnavarro.neospectro.ui.zenScreen.ZenScreen
import com.dnavarro.neospectro.utils.onBack
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

private val FloatingToolbarButtonShapes = ToggleButtonShapes(
    CircleShape,
    CircleShape,
    CircleShape
)

private val ApplyButtonShape = RoundedCornerShape(20.dp)

@Composable
fun AppScreen(
    mainViewModel: MainViewModel = viewModel(factory = MainViewModel.Factory)
) {
    val context = LocalContext.current
    var isLwpSet by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val info = wallpaperManager.wallpaperInfo
        isLwpSet =
            info != null && info.component == ComponentName(context, LWPService::class.java)
        onPauseOrDispose { }
    }

    val uiState by mainViewModel.uiState.collectAsState()
    val backStack = rememberNavBackStack(Screen.Main)
    val currentRoute = backStack.lastOrNull()
    val isZenMode = currentRoute == Screen.Zen
    val isInfoScreen = currentRoute == Screen.Info
    val motionScheme = motionScheme
    val hazeState = rememberHazeState()

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

    val onApplyWallpaper: () -> Unit = {
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
        intent.putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(context, LWPService::class.java)
        )
        context.startActivity(intent)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        LiveWaveBackground(
            uiState = uiState,
            isActive = !isInfoScreen,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState, zIndex = 0f)
        )

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                AnimatedVisibility(
                    visible = !isZenMode,
                    enter = slideInVertically(motionScheme.slowSpatialSpec()) { it },
                    exit = slideOutVertically(motionScheme.slowSpatialSpec()) { it }
                ) {
                    FloatingNavigationToolbar(
                        currentRoute = currentRoute,
                        showApplyButton = !isLwpSet,
                        hazeState = hazeState,
                        onNavigate = onNavigateTo,
                        onApplyClick = onApplyWallpaper
                    )
                }
            },
        ) { contentPadding ->
            NavDisplay(
                backStack = backStack,
                onBack = {
                    backStack.onBack()
                },
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
                            contentPadding = contentPadding,
                            hazeState = hazeState,
                            onEnterZenMode = { onNavigateTo(Screen.Zen) },
                            viewModel = mainViewModel
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
                            contentPadding = contentPadding,
                            modifier = Modifier.hazeSource(hazeState, zIndex = 1f)
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
    showApplyButton: Boolean,
    hazeState: HazeState?,
    onNavigate: (Screen) -> Unit,
    onApplyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val motionScheme = motionScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
                .padding(horizontal = 16.dp, vertical = ScreenOffset)
                .zIndex(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalFloatingToolbar(
                expanded = true,
                modifier = Modifier.frostedGlass(
                    hazeState = hazeState,
                    shape = CircleShape
                ),
                colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                    toolbarContainerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
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
                            shapes = FloatingToolbarButtonShapes,
                            colors = ToggleButtonDefaults.colors(
                                containerColor = Color.Transparent,
                                contentColor = GlassDefaults.SecondaryContentColor,
                                checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                                        fontSize = 15.sp,
                                        lineHeight = 20.sp,
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

            AnimatedVisibility(visible = showApplyButton) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Above
                    ),
                    tooltip = { PlainTooltip { Text(stringResource(R.string.apply)) } },
                    state = rememberTooltipState(),
                ) {
                    FloatingActionButton(
                        onClick = onApplyClick,
                        shape = ApplyButtonShape,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 2.dp
                        ),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.apply_outlined),
                            contentDescription = stringResource(R.string.apply),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
