package com.sandeep.admuterapp

import android.app.Notification
import android.content.ContentValues.TAG
import android.content.Intent
import android.media.AudioManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

//manifest.xml
//android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"

//<intent-filter>
//     <action android:name="android.service.notification.NotificationListenerService" />
//</intent-filter>

class NotificationListener : NotificationListenerService() {
    private var lastTrackId: String? = null
    private lateinit var repository: AdRepository
    private var wasAdDetected = false

    override fun onCreate() {
        super.onCreate()
        repository = AdRepository(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return
        val extras = notification.extras
        val packageName = sbn.packageName

        if (packageName == "com.spotify.music") {                                //com.spotify.music

            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            //Log.d("SpotifyListener", "TITLE: $title | TEXT: $text")


            val currentTrackId = "$title-$text"

            if (isAd(text)) {
                repository.muteAd()
                //Log.d("TrackCounter", "❌ Ad: $currentTrackId")
                wasAdDetected = true
            } else {
                repository.unmuteAd()

                // 🔑 Count increase only if new track detected
                if (currentTrackId != lastTrackId) {
                    repository.incrementSongCounter()
                    lastTrackId = currentTrackId
                    Log.d("TrackCounter", "✅ New Song: $currentTrackId")
                } else {

                }
                wasAdDetected = false
            }
        }
    }

    private fun sendCounterUpdate(type: String) {
        //Log.d("NotificationListener", "Sending counter update: $type")
        val intent = Intent("COUNTER_CHANGED")
        intent.putExtra("TYPE", type)
        //sendBroadcast(intent)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun isAd(text: String): Boolean {
        val lowerText = text.lowercase()
        return lowerText.contains("advertisement")
    }

//    private fun muteVolume() {
//        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
//        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
//        //Log.d("SpotifyListener", "🔇 Muted")
//        sendCounterUpdate("Ads")
//    }
//
//    private fun unmuteVolume() {
//        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
//        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
//        //Log.d("SpotifyListener", "🔊 Unmuted")
//        sendCounterUpdate("Songs")
//
//    }
}

