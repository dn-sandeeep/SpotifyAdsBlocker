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
        android.util.Log.d("AdMuter_Log", "[Repo] muteAd requested by: $source")
        synchronized(activeMuteSources) {
            if (activeMuteSources.isEmpty()) {
                android.util.Log.i("AdMuter_Log", ">>> MUTING AUDIO (First source: $source)")
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
                _events.tryEmit(AdEvent.AdDetected)
            }
            activeMuteSources.add(source)
            android.util.Log.d("AdMuter_Log", "[Repo] Active sources: $activeMuteSources")
        }
    }

    fun unmuteAd(source: String) {
        android.util.Log.d("AdMuter_Log", "[Repo] unmuteAd requested by: $source")
        synchronized(activeMuteSources) {
            activeMuteSources.remove(source)
            android.util.Log.d("AdMuter_Log", "[Repo] Remaining sources: $activeMuteSources")
            if (activeMuteSources.isEmpty()) {
                android.util.Log.i("AdMuter_Log", "<<< UNMUTING AUDIO (All sources cleared)")
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
