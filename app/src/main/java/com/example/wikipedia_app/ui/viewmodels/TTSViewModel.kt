package com.example.wikipedia_app.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wikipedia_app.accessibility.TextToSpeechService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TTSViewModel(context: Context) : ViewModel() {
    private val ttsService = TextToSpeechService(context)
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        viewModelScope.launch {
            ttsService.isSpeaking.collect { speaking ->
                _isSpeaking.value = speaking
            }
        }
    }

    fun speak(text: String) {
        ttsService.speak(text)
    }

    fun stop() {
        ttsService.stop()
    }

    override fun onCleared() {
        super.onCleared()
        ttsService.shutdown()
    }
} 