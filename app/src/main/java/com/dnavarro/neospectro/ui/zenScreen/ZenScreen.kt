package com.dnavarro.neospectro.ui.zenScreen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dnavarro.neospectro.Constants
import com.dnavarro.neospectro.data.ThemeRepository
import com.dnavarro.neospectro.renderer.GLES20Renderer
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ZenScreen(
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = remember(context) { context as? Activity }

    val prefs = remember { context.getSharedPreferences(Constants.PRENS_NAME, Context.MODE_PRIVATE) }
    var showHint by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3000.milliseconds)
        showHint = false
    }

    val currentTheme = remember { prefs.getString(Constants.PREF_THEME, Constants.THEME_ICE) ?: Constants.THEME_ICE }
    val reverseColors = remember { prefs.getBoolean(Constants.PREF_REVERSE_COLORS, false) }
    val audioVizEnabled = remember { prefs.getBoolean(Constants.PREF_AUDIO_VIZ, false) }

    val theme = remember(currentTheme) { ThemeRepository.getTheme(currentTheme) }
    val edge = remember(theme, reverseColors) { if (reverseColors) theme.centerColor else theme.edgeColor }
    val center = remember(theme, reverseColors) { if (reverseColors) theme.edgeColor else theme.centerColor }

    val renderer = remember { GLES20Renderer(context) }

    val glSurfaceView = remember {
        GLSurfaceView(context).apply {
            setEGLContextClientVersion(2)
            keepScreenOn = true

            // Set renderer parameters
            renderer.mEdgeColor = edge
            renderer.mMiddleColor = theme.middleColor
            renderer.mCenterColor = center

            val hasAudioPermission = audioVizEnabled && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            renderer.setAudioEnabled(hasAudioPermission)

            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
    }

    // Handle Immersive Fullscreen (hide status/navigation bar)
    DisposableEffect(activity) {
        val window = activity?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            val window = activity?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Lifecycle handling for GLSurfaceView & GLES20Renderer
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    glSurfaceView.onResume()
                    renderer.setVisible(true)
                }
                Lifecycle.Event.ON_PAUSE -> {
                    renderer.setVisible(false)
                    glSurfaceView.onPause()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            renderer.setVisible(false)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                onClick = onExit,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        AndroidView(
            factory = { glSurfaceView },
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = showHint,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = "Tap anywhere to exit Zen mode",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
