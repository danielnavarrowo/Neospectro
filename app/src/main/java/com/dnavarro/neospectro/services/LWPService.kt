package com.dnavarro.neospectro.services
import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import com.dnavarro.neospectro.Constants
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

            // Use applicationContext to share prefs correctly with Activity
            prefs = applicationContext.getSharedPreferences(Constants.PRENS_NAME, MODE_PRIVATE)
            prefs.registerOnSharedPreferenceChangeListener(this)

            renderer = GLES20Renderer(this@LWPService)

            // Set initial texture
            currentTheme = prefs.getString(Constants.PREF_THEME, Constants.THEME_ICE) ?: Constants.THEME_ICE
            val (edgeColor, centerColor) = getColorsForTheme(currentTheme)
            renderer!!.mEdgeColor = edgeColor
            renderer!!.mCenterColor = centerColor

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
                val (edgeColor, centerColor) = getColorsForTheme(newTheme)
                queueEvent {
                    renderer?.updateTextureColor(edgeColor, centerColor)
                }
            }
        }

        private fun getColorsForTheme(theme: String): Pair<Int, Int> {
            return when (theme) {
                Constants.THEME_FIRE -> Pair(Color.RED, Color.YELLOW)
                Constants.THEME_ACID -> Pair(Color.GREEN, Color.WHITE)
                else -> Pair(Color.rgb(3, 3, 255), Color.WHITE) // Ice
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
