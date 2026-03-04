package com.sandeep.admuterapp.data

import android.content.Context
import android.media.AudioManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AdRepository private constructor(context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val activeMuteSources = mutableSetOf<String>()
    private var lastTrackId: String? = null
    
    private val _events = MutableSharedFlow<AdEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<AdEvent> = _events.asSharedFlow()

    sealed class AdEvent {
        object AdDetected : AdEvent()
        object SongDetected : AdEvent()
    }

    fun muteAd(source: String) {
        synchronized(activeMuteSources) {
            if (activeMuteSources.isEmpty()) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
                _events.tryEmit(AdEvent.AdDetected)
            }
            activeMuteSources.add(source)
        }
    }

    fun unmuteAd(source: String) {
        synchronized(activeMuteSources) {
            activeMuteSources.remove(source)
            if (activeMuteSources.isEmpty()) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
            }
        }
    }

    fun incrementSongCounter(trackId: String) {
        synchronized(this) {
            if (trackId != lastTrackId) {
                _events.tryEmit(AdEvent.SongDetected)
                lastTrackId = trackId
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AdRepository? = null

        fun getInstance(context: Context): AdRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
