package com.sandeep.admuterapp.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.sandeep.admuterapp.service.MediaSessionListenerService
import com.sandeep.admuterapp.ui.theme.AdMuterAppTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AdMuterViewModel by viewModels()
    private var isServiceActive by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.d("AdMuter_Log", "Notification permission granted: $isGranted")
        if (isGranted) {
            checkBatteryOptimizationAndStart()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        updateServiceStatus()

        setContent {
            AdMuterAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AdMuterApp(
                        modifier = Modifier.padding(innerPadding),
                        isServiceRunning = isServiceActive,
                        viewModel = viewModel,
                        onStartServiceClick = { 
                            Log.d("AdMuter_Log", "Start Service Button Clicked")
                            checkPermissionsAndStart() 
                        },
                        onOnePlusFixClick = { openOnePlusAutoLaunchSettings() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }

    private fun updateServiceStatus() {
        isServiceActive = isServiceRunning() && isNotificationServiceEnabled()
        Log.d("AdMuter_Log", "Service status updated: $isServiceActive (Running: ${isServiceRunning()}, NotifEnabled: ${isNotificationServiceEnabled()})")
    }

    private fun checkPermissionsAndStart() {
        if (!isNotificationServiceEnabled()) {
            Log.d("AdMuter_Log", "Notification Listener not enabled, opening settings")
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("AdMuter_Log", "Requesting POST_NOTIFICATIONS permission")
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                checkBatteryOptimizationAndStart()
            }
        } else {
            checkBatteryOptimizationAndStart()
        }
    }

    private fun checkBatteryOptimizationAndStart() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val packageName = packageName
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                Log.d("AdMuter_Log", "Requesting ignore battery optimizations")
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
        
        startMediaSessionService()
    }

    private fun isServiceRunning(): Boolean {
        return MediaSessionListenerService.isServiceRunning
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (!flat.isNullOrEmpty()) {
            val names = flat.split(":")
            for (name in names) {
                val cn = ComponentName.unflattenFromString(name)
                if (cn != null && cn.packageName == pkgName) {
                    return true
                }
            }
        }
        return false
    }

    private fun startMediaSessionService() {
        Log.i("AdMuter_Log", "Starting Foreground Service...")
        val serviceIntent = Intent(this, MediaSessionListenerService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        // Set local state to true immediately for better UX
        isServiceActive = true 
    }

    private fun openOnePlusAutoLaunchSettings() {
        Log.d("AdMuter_Log", "Opening OnePlus Auto-launch settings")
        try {
            val intent = Intent()
            intent.component = ComponentName(
                "com.oneplus.security",
                "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
            )
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent()
                intent.component = ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.startupapp.StartupAppListActivity"
                )
                startActivity(intent)
            } catch (e2: Exception) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }
}

@Composable
fun AdMuterApp(
    modifier: Modifier,
    isServiceRunning: Boolean,
    viewModel: AdMuterViewModel,
    onStartServiceClick: () -> Unit,
    onOnePlusFixClick: () -> Unit
) {
    val adsCount by viewModel.adsCount.collectAsState()
    val songsCount by viewModel.songsCount.collectAsState()
    val isOnePlus = Build.MANUFACTURER.lowercase().contains("oneplus") || 
                    Build.BRAND.lowercase().contains("oneplus") ||
                    Build.MANUFACTURER.lowercase().contains("oppo")

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Spotify Ad Muter",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = if (isServiceRunning) "Service Active" else "Service Inactive",
                    color = if (isServiceRunning) Color(0xFF4CAF50) else Color.Red,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onStartServiceClick,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(if (isServiceRunning) "Restart Service" else "Start Service")
                }

                if (isOnePlus) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onOnePlusFixClick,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("OnePlus Fix (Auto-launch)")
                    }
                    Text(
                        text = "OnePlus devices require 'Auto-launch' to be enabled.",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
                StatisticsDashboard(adsCount = adsCount, songsCount = songsCount)
            }
        }
    }
}

@Composable
fun StatisticsDashboard(adsCount: Int, songsCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatCard(
            title = "Ads Muted",
            count = adsCount,
            modifier = Modifier.weight(1f),
            color = Color(0xFFE57373)
        )

        Spacer(modifier = Modifier.width(16.dp))

        StatCard(
            title = "Songs Played",
            count = songsCount,
            modifier = Modifier.weight(1f),
            color = Color(0xFF1d6ef0)
        )
    }
}

@Composable
fun StatCard(title: String, count: Int, modifier: Modifier, color: Color) {
    Card(
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
            Text(
                text = count.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}
