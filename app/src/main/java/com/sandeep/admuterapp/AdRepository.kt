package com.sandeep.admuterapp

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class AdRepository(private val context: Context) {

    private lateinit var repository: AdRepository



    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun muteAd() {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
        sendBroadcast("Ads")
    }

    fun unmuteAd() {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
        //sendBroadcast("Songs")
    }

    fun incrementSongCounter() {
        sendBroadcast("Songs")
    }

    private fun sendBroadcast(type: String) {
        val intent = Intent("COUNTER_CHANGED").apply {
            putExtra("TYPE", type)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}
