package com.sandeep.admuterapp.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.sandeep.admuterapp.data.AdRepository
import com.sandeep.admuterapp.util.AdActionDetector

class NotificationListener : NotificationListenerService() {
    private var lastTrackId: String? = null
    private var lastRealSongTrackId: String? = null
    private lateinit var repository: AdRepository
    private var wasAdDetected = false
    private var lastAdDetectedTime = 0L
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
            val currentTrackId = "$title-$text"

            android.util.Log.d("AdMuter_Log", "[NotifListener] Notification Received: Title='$title', Text='$text'")

            // Hybrid Check: Text-based OR Action-based
            val isTextAd = isAd(title, text)
            val isActionAd = actionDetector.isAdBasedOnActions(sbn)
            val isCurrentAd = isTextAd || isActionAd
            
            android.util.Log.d("AdMuter_Log", "[NotifListener] Ad Check: isTextAd=$isTextAd, isActionAd=$isActionAd")

            if (isCurrentAd) {
                android.util.Log.i("AdMuter_Log", "[NotifListener] !! AD DETECTED !! Source: ${if (isTextAd) "Text" else "Actions"}")
                repository.muteAd("NotificationListener")
                wasAdDetected = true
                lastAdDetectedTime = System.currentTimeMillis()
            } else {
                // --- FLICKER PROTECTION LOGIC ---
                val currentTime = System.currentTimeMillis()
                val timeSinceLastAd = currentTime - lastAdDetectedTime
                
                // If we just saw an ad (< 1.5s ago) and this "song" is the same as the one BEFORE the ad, 
                // it's a flicker/glitch. Ignore it.
                if (timeSinceLastAd < 1500 && currentTrackId == lastRealSongTrackId) {
                    android.util.Log.w("AdMuter_Log", "[NotifListener] Flicker Detected! Ignoring old song metadata during ad transition.")
                    return 
                }

                android.util.Log.d("AdMuter_Log", "[NotifListener] Song Detected: Title='$title'")
                repository.unmuteAd("NotificationListener")
                lastRealSongTrackId = currentTrackId
                wasAdDetected = false

                if (currentTrackId != lastTrackId) {
                    if (currentTime - lastUpdateTime > 2000) { // Increased to 2000ms debounce
                        repository.incrementSongCounter(currentTrackId)
                        lastTrackId = currentTrackId
                        lastUpdateTime = currentTime
                    }
                }
            }
        }
    }

    private fun isAd(title: String, text: String): Boolean {
        val lowerTitle = title.lowercase()
        val lowerText = text.lowercase()
        val result = lowerTitle.contains("advertisement") || lowerText.contains("advertisement") || (lowerTitle.isBlank() && lowerText.isBlank())
        return result
    }
}
