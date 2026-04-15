package com.dnavarro.neospectro.ui.mainScreen

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dnavarro.neospectro.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences(Constants.PRENS_NAME, Context.MODE_PRIVATE)

    private val _selectedTheme = MutableStateFlow(prefs.getString(Constants.PREF_THEME, Constants.THEME_ICE) ?: Constants.THEME_ICE)
    val selectedTheme: StateFlow<String> = _selectedTheme.asStateFlow()

    private val _reverseColors = MutableStateFlow(prefs.getBoolean(Constants.PREF_REVERSE_COLORS, false))
    val reverseColors: StateFlow<Boolean> = _reverseColors.asStateFlow()

    private val _audioVizEnabled = MutableStateFlow(prefs.getBoolean(Constants.PREF_AUDIO_VIZ, false))
    val audioVizEnabled: StateFlow<Boolean> = _audioVizEnabled.asStateFlow()

    private val _hasBgImage = MutableStateFlow(prefs.getBoolean(Constants.PREF_HAS_BG_IMAGE, false))
    val hasBgImage: StateFlow<Boolean> = _hasBgImage.asStateFlow()

    private val _bgImageTrigger = MutableStateFlow(0)
    val bgImageTrigger: StateFlow<Int> = _bgImageTrigger.asStateFlow()

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

    fun handleBgImagePicked(uri: Uri?, context: Context) {
        if (uri != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val file = File(context.filesDir, "bg_image.jpg")
                    val outputStream = FileOutputStream(file)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    launch(Dispatchers.Main) {
                        updateHasBgImage(true)
                        _bgImageTrigger.value++
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
