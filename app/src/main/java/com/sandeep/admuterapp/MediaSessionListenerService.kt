package com.sandeep.admuterapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.media.MediaBrowserServiceCompat



class MediaSessionListenerService : MediaBrowserServiceCompat() {

    private lateinit var ticker: String
    private var lastTrackId: String? = null
    private lateinit var repository: AdRepository
    private var wasAdDetected = false
    private var mediaBrowser: MediaBrowserCompat? = null
    private var mediaController: MediaControllerCompat? = null

    private val spotifyPackageName = "com.spotify.music"
    private val spotifyMediaPlaybackService = "com.spotify.mobile.android.service.media.MediaPlaybackService"

    private val CHANNEL_ID = "AdMuter_Foreground_Service"
    private val NOTIFICATION_ID = 101


    companion object {
        var isServiceRunning = false
    }

    // --- OnCreate ---
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        repository = AdRepository(this)

        val spotifyComponentName = ComponentName(spotifyPackageName, spotifyMediaPlaybackService)
        mediaBrowser = MediaBrowserCompat(this, spotifyComponentName, connectionCallbacks, null)
        mediaBrowser?.connect()



    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startServiceInForeground()
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun startServiceInForeground() {
        createNotificationChannel()
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("AdMuter App")
            .setContentText("Monitoring Spotify for Ads")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setTicker("Service Running")
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "AdMuter App",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel
            )
        }
    }

    // --- MediaSession Connection Callbacks ---
    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {

            val token = mediaBrowser?.sessionToken ?: return
            mediaController = MediaControllerCompat(this@MediaSessionListenerService, token)
            mediaController?.registerCallback(playbackCallback)
            Log.d("MediaSession", "Connected and Registered Callback")
        }

        override fun onConnectionSuspended() {
            mediaController?.unregisterCallback(playbackCallback)
            mediaController = null
        }

        override fun onConnectionFailed() {

            Log.e("MediaSession", "Connection to Spotify failed")
        }
    }

    // --- The Core Logic: MediaSession Callback ---
    private val playbackCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {

            processMetadata(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {


        }
    }

    // --- Ad Detection and Mute Logic ---
    private fun processMetadata(metadata: MediaMetadataCompat?) {
        val title = metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE) ?: ""
        val artist = metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST) ?: ""

        Log.d("TrackCounter", "Raw Metadata -> Title: '$title', Artist: '$artist'")

        val currentTrackId = "$title-$artist"

        if (isAd(title, artist)) {
            if (!wasAdDetected) {
                repository.muteAd()
                wasAdDetected = true
                Log.d("TrackCounter", "❌ Ad detected: $currentTrackId")
            }
        } else {
            repository.unmuteAd()

            // 🔑 Count increase logic
            if (currentTrackId != lastTrackId) {
                repository.incrementSongCounter()
                lastTrackId = currentTrackId
                Log.d("TrackCounter", "✅ New Song: $currentTrackId")
            }
            wasAdDetected = false
        }
    }
    private fun isAd(title: String, artist: String): Boolean {

        val lowerTitle = title.lowercase()
        val lowerArtist = artist.lowercase()


        val isExplicitAd = lowerTitle.contains("advertisement") || lowerArtist.contains("advertisement")

        val isGenericAd = lowerTitle.isBlank() && lowerArtist.isBlank()

        return isExplicitAd || isGenericAd
    }
    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {

        return if (clientPackageName == spotifyPackageName || clientPackageName == applicationContext.packageName) {
            BrowserRoot("media_root_id", null)
        } else {
            null
        }
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        mediaController?.unregisterCallback(playbackCallback)
        mediaBrowser?.disconnect()
        stopForeground(true)
    }
}