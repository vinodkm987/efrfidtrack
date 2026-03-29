package com.example.efridtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.efridtracker.navigation.AppNavigation
import com.example.efridtracker.rfid.RfidManager
import com.example.efridtracker.ui.theme.EFRidTrackerTheme

class MainActivity : ComponentActivity() {

    private lateinit var rfidManager: RfidManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TC27 uses the Zebra RFID host service (e-Connectix) — no runtime BT permissions required.
        rfidManager = RfidManager(applicationContext)

        enableEdgeToEdge()
        setContent {
            EFRidTrackerTheme {
                AppNavigation(rfidManager = rfidManager)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rfidManager.disconnect()
    }
}