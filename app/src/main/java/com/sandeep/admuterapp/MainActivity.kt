package com.sandeep.admuterapp

import android.R.attr.type
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.sandeep.admuterapp.ui.theme.AdMuterAppTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AdMuterViewModel by viewModels()
    private val isListenerEnabled: Boolean
        get() {
            val enabledListeners =
                Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            return enabledListeners?.contains(packageName) == true
        }
    private val counterReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            //Log.d("MainActivity", "Counter broadcast received: $type")
            when (intent?.getStringExtra("TYPE")) {
                "Ads" -> {
                    viewModel.incrementAds()
                    Log.d("MainActivity", "Ads incremented")
                }
                "Songs" -> {
                    viewModel.incrementSongs()
                    Log.d("MainActivity", "Songs incremented")
                }
            }
        }
    }
//    override fun onResume() {
//        super.onResume()
//        registerReceiver(counterReceiver, IntentFilter("COUNTER_CHANGED"))
//    }
//
//    override fun onPause() {
//        super.onPause()
//        unregisterReceiver(counterReceiver)
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(counterReceiver, IntentFilter("COUNTER_CHANGED"))

        setContent {
            AdMuterAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AdMuterApp(
                        modifier = Modifier
                            .padding(innerPadding),
                        isListenerEnabled = isListenerEnabled,
                        viewModel = viewModel
                    ) {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // --- Unregister Broadcast ---
        LocalBroadcastManager.getInstance(this).unregisterReceiver(counterReceiver)
    }
}


@Composable
fun AdMuterApp(
    modifier: Modifier,
    isListenerEnabled: Boolean,
    viewModel: AdMuterViewModel,
    onEnabledClick: () -> Unit) {
    var isActive by remember {
        mutableStateOf(isListenerEnabled)

    }
    val context = LocalContext.current
    val adsCount by viewModel.adsCount.collectAsState()
    val songsCount by viewModel.songsCount.collectAsState()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .size(width = 400.dp, height = 400.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Spotify Ad Muter",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    if (isActive) {
                        "Notification Access Granted"
                    } else {
                        "Notification Access Not Granted"
                    },
                    color = if (isActive) Color.Green else Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onEnabledClick) {
                    Text("Open Notification Access Settings")
                }
                Spacer(modifier = Modifier.height(40.dp))
                // --- Display Counters ---
                Text("Ads muted: $adsCount")
                Text("Songs played: $songsCount")

            }
        }

    }

}