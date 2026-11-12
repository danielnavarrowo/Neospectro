package com.dnavarro.neospectro.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import com.dnavarro.neospectro.Constants
import com.dnavarro.neospectro.ui.mainScreen.MainUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    private val _uiState = MutableStateFlow(
        MainUiState(
            selectedTheme = prefs.getString(Constants.PREF_THEME, Constants.THEME_ICE) ?: Constants.THEME_ICE,
            reverseColors = prefs.getBoolean(Constants.PREF_REVERSE_COLORS, false),
            audioVizEnabled = prefs.getBoolean(Constants.PREF_AUDIO_VIZ, false),
            hasBgImage = prefs.getBoolean(Constants.PREF_HAS_BG_IMAGE, false),
            bgImageTrigger = 0,
            isCustomThemeEnabled = prefs.getBoolean(Constants.PREF_CUSTOM_THEME_ENABLED, false),
            customEdgeColor = prefs.getInt(Constants.PREF_CUSTOM_EDGE_COLOR, 0xFF0303FF.toInt()),
            customMiddleColor = prefs.getInt(Constants.PREF_CUSTOM_MIDDLE_COLOR, 0xFF7B7BFF.toInt()),
            customCenterColor = prefs.getInt(Constants.PREF_CUSTOM_CENTER_COLOR, 0xFFF1F1FF.toInt())
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _selectedTheme = MutableStateFlow(_uiState.value.selectedTheme)
    val selectedTheme: StateFlow<String> = _selectedTheme.asStateFlow()

    private val _reverseColors = MutableStateFlow(_uiState.value.reverseColors)
    val reverseColors: StateFlow<Boolean> = _reverseColors.asStateFlow()

    private val _audioVizEnabled = MutableStateFlow(_uiState.value.audioVizEnabled)
    val audioVizEnabled: StateFlow<Boolean> = _audioVizEnabled.asStateFlow()

    private val _hasBgImage = MutableStateFlow(_uiState.value.hasBgImage)

    private val _bgImageTrigger = MutableStateFlow(_uiState.value.bgImageTrigger)
    val bgImageTrigger: StateFlow<Int> = _bgImageTrigger.asStateFlow()

    private val _isCustomThemeEnabled = MutableStateFlow(_uiState.value.isCustomThemeEnabled)
    val isCustomThemeEnabled: StateFlow<Boolean> = _isCustomThemeEnabled.asStateFlow()

    private val _customEdgeColor = MutableStateFlow(_uiState.value.customEdgeColor)
    val customEdgeColor: StateFlow<Int> = _customEdgeColor.asStateFlow()

    private val _customMiddleColor = MutableStateFlow(_uiState.value.customMiddleColor)
    val customMiddleColor: StateFlow<Int> = _customMiddleColor.asStateFlow()

    private val _customCenterColor = MutableStateFlow(_uiState.value.customCenterColor)
    val customCenterColor: StateFlow<Int> = _customCenterColor.asStateFlow()

    fun getTheme(): String = _uiState.value.selectedTheme

    fun getActiveWaveTheme(): WaveTheme {
        val state = _uiState.value
        return if (state.isCustomThemeEnabled) {
            WaveTheme(
                id = Constants.THEME_CUSTOM,
                edgeColor = state.customEdgeColor,
                middleColor = state.customMiddleColor,
                centerColor = state.customCenterColor
            )
        } else {
            ThemeRepository.getTheme(state.selectedTheme)
        }
    }

    fun isReverseColors(): Boolean = _uiState.value.reverseColors

    fun isAudioVizEnabled(): Boolean = _uiState.value.audioVizEnabled

    fun hasBgImage(): Boolean = _uiState.value.hasBgImage

    fun getBackgroundImageFile(): File {
        return File(appContext.filesDir, "bg_image.jpg")
    }

    fun updateCustomThemeEnabled(enabled: Boolean) {
        _isCustomThemeEnabled.value = enabled
        _uiState.update { it.copy(isCustomThemeEnabled = enabled) }
        prefs.edit { putBoolean(Constants.PREF_CUSTOM_THEME_ENABLED, enabled) }
    }

    fun updateCustomColors(edge: Int, middle: Int, center: Int) {
        _customEdgeColor.value = edge
        _customMiddleColor.value = middle
        _customCenterColor.value = center
        _uiState.update {
            it.copy(
                customEdgeColor = edge,
                customMiddleColor = middle,
                customCenterColor = center
            )
        }
        prefs.edit {
            putInt(Constants.PREF_CUSTOM_EDGE_COLOR, edge)
            putInt(Constants.PREF_CUSTOM_MIDDLE_COLOR, middle)
            putInt(Constants.PREF_CUSTOM_CENTER_COLOR, center)
        }
    }

    fun updateTheme(theme: String) {
        _selectedTheme.value = theme
        _uiState.update { it.copy(selectedTheme = theme) }
        prefs.edit { putString(Constants.PREF_THEME, theme) }
    }

    fun updateReverseColors(reverse: Boolean) {
        _reverseColors.value = reverse
        _uiState.update { it.copy(reverseColors = reverse) }
        prefs.edit { putBoolean(Constants.PREF_REVERSE_COLORS, reverse) }
    }

    fun updateAudioVizEnabled(enabled: Boolean) {
        _audioVizEnabled.value = enabled
        _uiState.update { it.copy(audioVizEnabled = enabled) }
        prefs.edit { putBoolean(Constants.PREF_AUDIO_VIZ, enabled) }
    }

    fun updateHasBgImage(hasImage: Boolean) {
        _hasBgImage.value = hasImage
        _uiState.update { it.copy(hasBgImage = hasImage) }
        prefs.edit { putBoolean(Constants.PREF_HAS_BG_IMAGE, hasImage) }
    }

    suspend fun saveBackgroundImage(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = getBackgroundImageFile()
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext false

            prefs.edit { putBoolean(Constants.PREF_HAS_BG_IMAGE, true) }
            _hasBgImage.value = true
            _bgImageTrigger.update { it + 1 }
            _uiState.update {
                it.copy(
                    hasBgImage = true,
                    bgImageTrigger = it.bgImageTrigger + 1
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
