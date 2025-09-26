package com.sandeep.admuterapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
class AdMuterViewModel : ViewModel() {

    private val _adsCount = MutableStateFlow(0)
    val adsCount: StateFlow<Int> = _adsCount.asStateFlow()
    private val _songsCount = MutableStateFlow(1)
    val songsCount: StateFlow<Int> = _songsCount.asStateFlow()


    fun incrementAds() {
        _adsCount.value += 1
        Log.d("AdMuterViewModel", "Ads counter incremented: ${_adsCount.value}")
    }
    fun incrementSongs() {
        _songsCount.value += 1
        Log.d("AdMuterViewModel", "Songs counter incremented: ${_songsCount.value}")
    }
}
