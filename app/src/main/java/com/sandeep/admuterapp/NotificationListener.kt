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
    private val actionDetector = AdActionDetector()
    private var lastUpdateTime = 0L

    override fun onCreate() {
        super.onCreate()
        repository = AdRepository(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return
        val extras = notification.extras
        val packageName = sbn.packageName

        if (packageName == "com.spotify.music") {

            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            Log.d("NotificationListener", "Active Notification -> Title: $title | Text: $text")

            actionDetector.isAdBasedOnActions(sbn)

            val currentTrackId = "$title-$text"
            
            // Hybrid Check: Text-based OR Action-based
            val isTextAd = isAd(title, text)
            val isActionAd = actionDetector.isAdBasedOnActions(sbn)
            
            if (isTextAd || isActionAd) {
                if (!wasAdDetected) {
                    repository.muteAd()
                    wasAdDetected = true
                    Log.d("TrackCounter", "❌ Ad detected (Notif): $currentTrackId")
                }
            } else {
                repository.unmuteAd()

                if (currentTrackId != lastTrackId) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastUpdateTime > 2000) { // Increased to 2000ms debounce
                        repository.incrementSongCounter()
                        lastTrackId = currentTrackId
                        lastUpdateTime = currentTime
                        Log.d("TrackCounter", "✅ New Song (Notif): $currentTrackId")
                    }
                }
                wasAdDetected = false
            }
        }
    }

    private fun isAd(title: String, text: String): Boolean {
        val lowerTitle = title.lowercase()
        val lowerText = text.lowercase()
        return lowerTitle.contains("advertisement") || lowerText.contains("advertisement") || (lowerTitle.isBlank() && lowerText.isBlank())
    }
}
