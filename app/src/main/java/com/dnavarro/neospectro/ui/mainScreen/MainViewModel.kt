package com.dnavarro.neospectro.ui.mainScreen

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dnavarro.neospectro.data.SettingsRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel @JvmOverloads constructor(
    application: Application,
    private val settingsRepository: SettingsRepository = SettingsRepository(application)
) : AndroidViewModel(application) {

    val selectedTheme: StateFlow<String> = settingsRepository.selectedTheme
    val reverseColors: StateFlow<Boolean> = settingsRepository.reverseColors
    val audioVizEnabled: StateFlow<Boolean> = settingsRepository.audioVizEnabled
    val hasBgImage: StateFlow<Boolean> = settingsRepository.hasBgImage
    val bgImageTrigger: StateFlow<Int> = settingsRepository.bgImageTrigger

    fun updateTheme(theme: String) {
        settingsRepository.updateTheme(theme)
    }

    fun updateReverseColors(reverse: Boolean) {
        settingsRepository.updateReverseColors(reverse)
    }

    fun updateAudioVizEnabled(enabled: Boolean) {
        settingsRepository.updateAudioVizEnabled(enabled)
    }

    fun updateHasBgImage(hasImage: Boolean) {
        settingsRepository.updateHasBgImage(hasImage)
    }

    fun handleBgImagePicked(uri: Uri?) {
        if (uri != null) {
            viewModelScope.launch {
                settingsRepository.saveBackgroundImage(uri)
            }
        }
    }
}
