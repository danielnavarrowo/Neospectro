package com.dnavarro.neospectro.services
import android.Manifest
import android.app.WallpaperColors
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import com.dnavarro.neospectro.Constants
import com.dnavarro.neospectro.data.ThemeRepository
import com.dnavarro.neospectro.renderer.GLES20Renderer
import java.io.File

class LWPService : OpenGLES2WallpaperService() {
    override fun onCreateEngine(): Engine {
        return NeospectroEngine()
    }
    inner class NeospectroEngine : GLEngine(), SharedPreferences.OnSharedPreferenceChangeListener {
        private var renderer: GLES20Renderer? = null
        private lateinit var prefs: SharedPreferences
        private var currentTheme: String = Constants.THEME_ICE
        private var reverseColors: Boolean = false

        override fun onCreate(surfaceHolder: android.view.SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            prefs = applicationContext.getSharedPreferences(Constants.PRENS_NAME, MODE_PRIVATE)
            prefs.registerOnSharedPreferenceChangeListener(this)

            renderer = GLES20Renderer(this@LWPService)

            // Set initial texture
            currentTheme = prefs.getString(Constants.PREF_THEME, Constants.THEME_ICE) ?: Constants.THEME_ICE
            reverseColors = prefs.getBoolean(Constants.PREF_REVERSE_COLORS, false)
            val theme = ThemeRepository.getTheme(currentTheme)

            val edge = if (reverseColors) theme.centerColor else theme.edgeColor
            val center = if (reverseColors) theme.edgeColor else theme.centerColor

            renderer!!.mEdgeColor = edge
            renderer!!.mMiddleColor = theme.middleColor
            renderer!!.mCenterColor = center

            setRenderer(renderer!!)
            // Initial check for audio
            checkAudioPermission()
            checkAndUpdateBgImage()
        }

        override fun onDestroy() {
            super.onDestroy()
            prefs.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            when (key) {
                Constants.PREF_THEME, Constants.PREF_REVERSE_COLORS -> {
                    checkAndUpdateTheme()
                }
                Constants.PREF_AUDIO_VIZ -> {
                    checkAudioPermission()
                }
                Constants.PREF_HAS_BG_IMAGE -> {
                    checkAndUpdateBgImage()
                }
            }
        }

        override fun onComputeColors(): WallpaperColors {
            val theme = ThemeRepository.getTheme(currentTheme)
            val edge = if (reverseColors) theme.centerColor else theme.edgeColor
            val center = if (reverseColors) theme.edgeColor else theme.centerColor
            return WallpaperColors(
                Color.valueOf(edge),
                Color.valueOf(theme.middleColor),
                Color.valueOf(center)
            )
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            renderer?.setVisible(visible)
            if (visible) {
                 checkAudioPermission()
                 checkAndUpdateTheme()
                 checkAndUpdateBgImage()
            }
        }

        private fun checkAndUpdateBgImage() {
            val hasBg = prefs.getBoolean(Constants.PREF_HAS_BG_IMAGE, false)
            if (hasBg) {
                val file = File(applicationContext.filesDir, "bg_image.jpg")
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    queueEvent {
                        renderer?.setBackgroundImage(bitmap)
                    }
                } else {
                    queueEvent {
                        renderer?.setBackgroundImage(null)
                    }
                }
            } else {
                queueEvent {
                    renderer?.setBackgroundImage(null)
                }
            }
        }

        private fun checkAndUpdateTheme() {
            val newTheme = prefs.getString(Constants.PREF_THEME, Constants.THEME_ICE) ?: Constants.THEME_ICE
            val newReverse = prefs.getBoolean(Constants.PREF_REVERSE_COLORS, false)

            if (newTheme != currentTheme || newReverse != reverseColors) {
                currentTheme = newTheme
                reverseColors = newReverse
                val theme = ThemeRepository.getTheme(newTheme)

                val edge = if (reverseColors) theme.centerColor else theme.edgeColor
                val center = if (reverseColors) theme.edgeColor else theme.centerColor

                queueEvent {
                    renderer?.updateTextureColor(edge, theme.middleColor, center)
                }
                notifyColorsChanged()
            }
        }

        private fun checkAudioPermission() {
             var hasPermission = false
             val isEnabledInSettings = prefs.getBoolean(Constants.PREF_AUDIO_VIZ, false)

             if (isEnabledInSettings && checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                 hasPermission = true
             }
             renderer?.setAudioEnabled(hasPermission)
        }
    }
}
