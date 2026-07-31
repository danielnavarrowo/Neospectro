package com.dnavarro.neospectro.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import com.dnavarro.neospectro.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SettingsRepository private constructor(context: Context) {

    private val appContext: Context = context.applicationContext

    companion object {
        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(Constants.PRENS_NAME, Context.MODE_PRIVATE)

    private val _selectedTheme = MutableStateFlow(
        prefs.getString(Constants.PREF_THEME, Constants.THEME_ICE) ?: Constants.THEME_ICE
    )
    val selectedTheme: StateFlow<String> = _selectedTheme.asStateFlow()

    private val _reverseColors =
        MutableStateFlow(prefs.getBoolean(Constants.PREF_REVERSE_COLORS, false))
    val reverseColors: StateFlow<Boolean> = _reverseColors.asStateFlow()

    private val _audioVizEnabled =
        MutableStateFlow(prefs.getBoolean(Constants.PREF_AUDIO_VIZ, false))
    val audioVizEnabled: StateFlow<Boolean> = _audioVizEnabled.asStateFlow()

    private val _hasBgImage =
        MutableStateFlow(prefs.getBoolean(Constants.PREF_HAS_BG_IMAGE, false))
    val hasBgImage: StateFlow<Boolean> = _hasBgImage.asStateFlow()

    private val _bgImageTrigger = MutableStateFlow(0)
    val bgImageTrigger: StateFlow<Int> = _bgImageTrigger.asStateFlow()

    private val _isCustomThemeEnabled = MutableStateFlow(
        prefs.getBoolean(Constants.PREF_CUSTOM_THEME_ENABLED, false)
    )
    val isCustomThemeEnabled: StateFlow<Boolean> = _isCustomThemeEnabled.asStateFlow()

    private val _customEdgeColor = MutableStateFlow(
        prefs.getInt(Constants.PREF_CUSTOM_EDGE_COLOR, android.graphics.Color.rgb(3, 3, 255))
    )
    val customEdgeColor: StateFlow<Int> = _customEdgeColor.asStateFlow()

    private val _customMiddleColor = MutableStateFlow(
        prefs.getInt(Constants.PREF_CUSTOM_MIDDLE_COLOR, android.graphics.Color.rgb(123, 123, 255))
    )
    val customMiddleColor: StateFlow<Int> = _customMiddleColor.asStateFlow()

    private val _customCenterColor = MutableStateFlow(
        prefs.getInt(Constants.PREF_CUSTOM_CENTER_COLOR, android.graphics.Color.rgb(241, 241, 255))
    )
    val customCenterColor: StateFlow<Int> = _customCenterColor.asStateFlow()

    fun getTheme(): String {
        return prefs.getString(Constants.PREF_THEME, Constants.THEME_ICE) ?: Constants.THEME_ICE
    }

    fun isCustomThemeEnabled(): Boolean {
        return prefs.getBoolean(Constants.PREF_CUSTOM_THEME_ENABLED, false)
    }

    fun getCustomEdgeColor(): Int {
        return prefs.getInt(Constants.PREF_CUSTOM_EDGE_COLOR, android.graphics.Color.rgb(3, 3, 255))
    }

    fun getCustomMiddleColor(): Int {
        return prefs.getInt(Constants.PREF_CUSTOM_MIDDLE_COLOR, android.graphics.Color.rgb(123, 123, 255))
    }

    fun getCustomCenterColor(): Int {
        return prefs.getInt(Constants.PREF_CUSTOM_CENTER_COLOR, android.graphics.Color.rgb(241, 241, 255))
    }

    fun getActiveWaveTheme(): WaveTheme {
        return if (isCustomThemeEnabled()) {
            WaveTheme(
                id = Constants.THEME_CUSTOM,
                edgeColor = getCustomEdgeColor(),
                middleColor = getCustomMiddleColor(),
                centerColor = getCustomCenterColor()
            )
        } else {
            ThemeRepository.getTheme(getTheme())
        }
    }

    fun isReverseColors(): Boolean {
        return prefs.getBoolean(Constants.PREF_REVERSE_COLORS, false)
    }

    fun isAudioVizEnabled(): Boolean {
        return prefs.getBoolean(Constants.PREF_AUDIO_VIZ, false)
    }

    fun hasBgImage(): Boolean {
        return prefs.getBoolean(Constants.PREF_HAS_BG_IMAGE, false)
    }

    fun getBackgroundImageFile(): File {
        return File(appContext.filesDir, "bg_image.jpg")
    }

    fun updateCustomThemeEnabled(enabled: Boolean) {
        _isCustomThemeEnabled.value = enabled
        prefs.edit { putBoolean(Constants.PREF_CUSTOM_THEME_ENABLED, enabled) }
    }

    fun updateCustomColors(edge: Int, middle: Int, center: Int) {
        _customEdgeColor.value = edge
        _customMiddleColor.value = middle
        _customCenterColor.value = center
        prefs.edit {
            putInt(Constants.PREF_CUSTOM_EDGE_COLOR, edge)
            putInt(Constants.PREF_CUSTOM_MIDDLE_COLOR, middle)
            putInt(Constants.PREF_CUSTOM_CENTER_COLOR, center)
        }
    }

    fun updateTheme(theme: String) {
        _selectedTheme.value = theme
        prefs.edit { putString(Constants.PREF_THEME, theme) }
    }

    fun updateReverseColors(reverse: Boolean) {
        _reverseColors.value = reverse
        prefs.edit { putBoolean(Constants.PREF_REVERSE_COLORS, reverse) }
    }

    fun updateAudioVizEnabled(enabled: Boolean) {
        _audioVizEnabled.value = enabled
        prefs.edit { putBoolean(Constants.PREF_AUDIO_VIZ, enabled) }
    }

    fun updateHasBgImage(hasImage: Boolean) {
        _hasBgImage.value = hasImage
        prefs.edit { putBoolean(Constants.PREF_HAS_BG_IMAGE, hasImage) }
    }

    suspend fun saveBackgroundImage(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val inputStream = appContext.contentResolver.openInputStream(uri)
            val file = getBackgroundImageFile()
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            withContext(Dispatchers.Main) {
                updateHasBgImage(true)
                _bgImageTrigger.value++
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
