package com.sandeep.admuterapp

import android.app.Notification
import android.service.notification.StatusBarNotification

class AdActionDetector {

    fun isAdBasedOnActions(sbn: StatusBarNotification): Boolean {
        val notification = sbn.notification ?: return false
        val actions = notification.actions ?: return false

        // Start with assumption that it's an ad (restricted)
        var hasNext = false
        var hasPrev = false

        // Check active actions in the notification
        // Note: Spotify might not remove actions, but change their icon/intent 
        // or availability. 
        // However, checking for "Skip to Next" action title or checking if actions are empty is a start.
        
        for (action in actions) {
            val title = action.title?.toString()?.lowercase() ?: "null"
            
            if (title.contains("next") || title.contains("skip")) {
                hasNext = true
            }
            if (title.contains("previous") || title.contains("prev")) {
                hasPrev = true
            }
        }
        val isLikelyAd = !hasNext && actions.size <= 2
        
        return isLikelyAd
    }
}
