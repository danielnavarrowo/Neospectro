package com.dnavarro.neospectro.ui.mainScreen

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dnavarro.neospectro.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel @JvmOverloads constructor(
    application: Application,
    private val settingsRepository: SettingsRepository = SettingsRepository(application)
) : AndroidViewModel(application) {

    val uiState: StateFlow<MainUiState> = combine(
        settingsRepository.selectedTheme,
        settingsRepository.reverseColors,
        settingsRepository.audioVizEnabled,
        settingsRepository.hasBgImage,
        settingsRepository.bgImageTrigger
    ) { theme, reverse, audioViz, hasBg, bgTrigger ->
        MainUiState(
            selectedTheme = theme,
            reverseColors = reverse,
            audioVizEnabled = audioViz,
            hasBgImage = hasBg,
            bgImageTrigger = bgTrigger
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState(
            selectedTheme = settingsRepository.getTheme(),
            reverseColors = settingsRepository.isReverseColors(),
            audioVizEnabled = settingsRepository.isAudioVizEnabled(),
            hasBgImage = settingsRepository.hasBgImage()
        )
    )

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
