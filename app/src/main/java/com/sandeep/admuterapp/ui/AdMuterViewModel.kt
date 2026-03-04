package com.sandeep.admuterapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.admuterapp.data.AdRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdMuterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AdRepository.getInstance(application)

    private val _adsCount = MutableStateFlow(0)
    val adsCount: StateFlow<Int> = _adsCount.asStateFlow()
    
    private val _songsCount = MutableStateFlow(0)
    val songsCount: StateFlow<Int> = _songsCount.asStateFlow()

    init {
        viewModelScope.launch {
            repository.events.collect { event ->
                when (event) {
                    is AdRepository.AdEvent.AdDetected -> incrementAds()
                    is AdRepository.AdEvent.SongDetected -> incrementSongs()
                }
            }
        }
    }

    private fun incrementAds() {
        _adsCount.value += 1
    }
    
    private fun incrementSongs() {
        _songsCount.value += 1
    }
}
