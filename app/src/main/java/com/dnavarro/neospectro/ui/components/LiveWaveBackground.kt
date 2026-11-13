package com.dnavarro.neospectro.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dnavarro.neospectro.Constants
import com.dnavarro.neospectro.data.SettingsRepository
import com.dnavarro.neospectro.data.ThemeRepository
import com.dnavarro.neospectro.data.WaveTheme
import com.dnavarro.neospectro.renderer.GLES20Renderer
import com.dnavarro.neospectro.renderer.GLTextureView
import com.dnavarro.neospectro.ui.mainScreen.MainUiState
import com.dnavarro.neospectro.utils.decodeSampledBitmapFromFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Renders the live OpenGL ES 2.0 wave inside a [TextureView]-backed surface so Compose
 * backdrop effects (Haze) can capture and blur it in real time.
 */
@Composable
fun LiveWaveBackground(
    uiState: MainUiState,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val settingsRepository = remember(context) { SettingsRepository.getInstance(context) }

    val theme = remember(
        uiState.selectedTheme,
        uiState.isCustomThemeEnabled,
        uiState.customEdgeColor,
        uiState.customMiddleColor,
        uiState.customCenterColor
    ) {
        if (uiState.isCustomThemeEnabled) {
            WaveTheme(
                id = Constants.THEME_CUSTOM,
                edgeColor = uiState.customEdgeColor,
                middleColor = uiState.customMiddleColor,
                centerColor = uiState.customCenterColor
            )
        } else {
            ThemeRepository.getTheme(uiState.selectedTheme)
        }
    }
    val edge = if (uiState.reverseColors) theme.centerColor else theme.edgeColor
    val center = if (uiState.reverseColors) theme.edgeColor else theme.centerColor

    val hasAudioPermission = uiState.audioVizEnabled && ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    var frameTick by remember { mutableLongStateOf(0L) }
    val renderer = remember(context) { GLES20Renderer(context) }
    val glTextureView = remember(context) {
        GLTextureView(context).apply {
            renderer.mEdgeColor = edge
            renderer.mMiddleColor = theme.middleColor
            renderer.mCenterColor = center
            setRenderer(renderer)
            onFrameUpdated = { frameTick++ }
        }
    }

    var isResumed by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> isResumed = true
                Lifecycle.Event.ON_PAUSE -> isResumed = false
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val shouldRender = isResumed && isActive

    LaunchedEffect(glTextureView, shouldRender) {
        if (shouldRender) {
            glTextureView.onResume()
            glTextureView.queueEvent {
                renderer.setVisible(true)
            }
        } else {
            glTextureView.queueEvent {
                renderer.setVisible(false)
            }
            glTextureView.onPause()
        }
    }

    DisposableEffect(glTextureView) {
        onDispose {
            glTextureView.queueEvent {
                renderer.setVisible(false)
            }
            glTextureView.onPause()
        }
    }

    // Push theme/color/audio updates immediately to the renderer
    LaunchedEffect(edge, theme.middleColor, center, hasAudioPermission) {
        glTextureView.queueEvent {
            renderer.updateTextureColor(edge, theme.middleColor, center)
            renderer.setAudioEnabled(hasAudioPermission)
        }
    }

    // Push background image toggle & newly picked images immediately to the renderer
    var hasSyncedInitialBg by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.hasBgImage, uiState.bgImageTrigger) {
        if (!hasSyncedInitialBg) {
            hasSyncedInitialBg = true
            return@LaunchedEffect
        }
        val bitmap = if (uiState.hasBgImage) {
            withContext(Dispatchers.IO) {
                val file = settingsRepository.getBackgroundImageFile()
                if (file.exists()) {
                    val metrics = resources.displayMetrics
                    val reqWidth = metrics.widthPixels.coerceAtLeast(1080)
                    val reqHeight = metrics.heightPixels.coerceAtLeast(1920)
                    decodeSampledBitmapFromFile(file, reqWidth, reqHeight)
                } else {
                    null
                }
            }
        } else {
            null
        }
        glTextureView.queueEvent {
            renderer.setBackgroundImage(bitmap)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithContent {
                // Read frameTick in the draw phase so Haze re-captures each live GL frame
                frameTick
                drawContent()
            }
    ) {
        AndroidView(
            factory = { glTextureView },
            modifier = Modifier.fillMaxSize()
        )
    }
}
