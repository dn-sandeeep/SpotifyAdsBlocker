package com.sandeep.admuterapp.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.sandeep.admuterapp.data.AdRepository
import com.sandeep.admuterapp.util.AdActionDetector

class NotificationListener : NotificationListenerService() {
    private var lastTrackId: String? = null
    private lateinit var repository: AdRepository
    private var wasAdDetected = false
    private val actionDetector = AdActionDetector()
    private var lastUpdateTime = 0L

    override fun onCreate() {
        super.onCreate()
        repository = AdRepository.getInstance(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return
        val extras = notification.extras
        val packageName = sbn.packageName

        if (packageName == "com.spotify.music") {

            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

            actionDetector.isAdBasedOnActions(sbn)

            val currentTrackId = "$title-$text"
            
            // Hybrid Check: Text-based OR Action-based
            val isTextAd = isAd(title, text)
            val isActionAd = actionDetector.isAdBasedOnActions(sbn)
            
            if (isTextAd || isActionAd) {
                repository.muteAd("NotificationListener")
                wasAdDetected = true
            } else {
                repository.unmuteAd("NotificationListener")

                if (currentTrackId != lastTrackId) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastUpdateTime > 2000) { // Increased to 2000ms debounce
                        repository.incrementSongCounter(currentTrackId)
                        lastTrackId = currentTrackId
                        lastUpdateTime = currentTime
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
