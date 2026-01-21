package com.sandeep.admuterapp

import android.app.Notification
import android.service.notification.StatusBarNotification
import android.util.Log

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
        
        Log.d("ActionDetector", "Analyzing ${actions.size} actions")
        
        for (action in actions) {
            val title = action.title?.toString()?.lowercase() ?: "null"
            Log.d("ActionDetector", "Action: $title")
            
            if (title.contains("next") || title.contains("skip")) {
                hasNext = true
            }
            if (title.contains("previous") || title.contains("prev")) {
                hasPrev = true
            }
        }
        
        // HEURISTIC:
        // 1. If "Next" action is present, it's definitely a Song (user can skip).
        // 2. If "Next" is missing, it MIGHT be an Ad.
        // 3. To be safe, if we have very few actions (<=2) AND no Next button, it's likely an Ad.
        //    (Ads usually only have Play/Pause, or nothing).
        
        val isLikelyAd = !hasNext && actions.size <= 2
        
        Log.d("ActionDetector", "Analysis Result -> HasNext: $hasNext, Count: ${actions.size}, IsAd: $isLikelyAd")
        
        return isLikelyAd
    }
}
