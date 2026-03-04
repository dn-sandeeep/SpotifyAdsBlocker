package com.sandeep.admuterapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
class AdMuterViewModel : ViewModel() {

    private val _adsCount = MutableStateFlow(0)
    val adsCount: StateFlow<Int> = _adsCount.asStateFlow()
    private val _songsCount = MutableStateFlow(0)
    val songsCount: StateFlow<Int> = _songsCount.asStateFlow()


    fun incrementAds() {
        _adsCount.value += 1
    }
    fun incrementSongs() {
        _songsCount.value += 1
    }
}
