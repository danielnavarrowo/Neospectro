package com.dnavarro.neospectro.services
import android.Manifest
import android.app.WallpaperColors
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import com.dnavarro.neospectro.Constants
import com.dnavarro.neospectro.data.ThemeRepository
import com.dnavarro.neospectro.renderer.GLES20Renderer

class LWPService : OpenGLES2WallpaperService() {
    override fun onCreateEngine(): Engine {
        return NeospectroEngine()
    }
    inner class NeospectroEngine : GLEngine(), SharedPreferences.OnSharedPreferenceChangeListener {
        private var renderer: GLES20Renderer? = null
        private lateinit var prefs: SharedPreferences
        private var currentTheme: String = Constants.THEME_ICE

        override fun onCreate(surfaceHolder: android.view.SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            prefs = applicationContext.getSharedPreferences(Constants.PRENS_NAME, MODE_PRIVATE)
            prefs.registerOnSharedPreferenceChangeListener(this)

            renderer = GLES20Renderer(this@LWPService)

            // Set initial texture
            currentTheme = prefs.getString(Constants.PREF_THEME, Constants.THEME_ICE) ?: Constants.THEME_ICE
            val theme = ThemeRepository.getTheme(currentTheme)
            renderer!!.mEdgeColor = theme.edgeColor
            renderer!!.mMiddleColor = theme.middleColor
            renderer!!.mCenterColor = theme.centerColor

            setRenderer(renderer!!)
            // Initial check for audio
            checkAudioPermission()
        }

        override fun onDestroy() {
            super.onDestroy()
            prefs.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            if (key == Constants.PREF_THEME) {
                checkAndUpdateTheme()
            }
        }

        override fun onComputeColors(): WallpaperColors {
            val theme = ThemeRepository.getTheme(currentTheme)
            return WallpaperColors(
                Color.valueOf(theme.edgeColor),
                Color.valueOf(theme.middleColor),
                Color.valueOf(theme.centerColor)
            )
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            renderer?.setVisible(visible)
            if (visible) {
                 checkAudioPermission()
                 checkAndUpdateTheme()
            }
        }

        private fun checkAndUpdateTheme() {
            val newTheme = prefs.getString(Constants.PREF_THEME, Constants.THEME_ICE) ?: Constants.THEME_ICE
            if (newTheme != currentTheme) {
                currentTheme = newTheme
                val theme = ThemeRepository.getTheme(newTheme)
                queueEvent {
                    renderer?.updateTextureColor(theme.edgeColor, theme.middleColor, theme.centerColor)
                }
                notifyColorsChanged()
            }
        }

        private fun checkAudioPermission() {
             var hasPermission = false
             if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                 hasPermission = true
             }
             renderer?.setAudioEnabled(hasPermission)
        }
    }
}
