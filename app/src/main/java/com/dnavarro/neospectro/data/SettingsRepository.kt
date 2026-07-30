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

class SettingsRepository private constructor(private val context: Context) {

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
        context.getSharedPreferences(Constants.PRENS_NAME, Context.MODE_PRIVATE)

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

    fun getTheme(): String {
        return prefs.getString(Constants.PREF_THEME, Constants.THEME_ICE) ?: Constants.THEME_ICE
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
        return File(context.filesDir, "bg_image.jpg")
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
            val inputStream = context.contentResolver.openInputStream(uri)
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
