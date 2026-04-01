package com.example.efridtracker.ui.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

private val ScreenBackground        = Color(0xFF1E2A38)
private val ScreenOnBackground      = Color(0xFFECEFF1)
private val ScreenOnBackgroundVariant = Color(0xFFB0BEC5)
private val ScanButtonColor         = Color(0xFF1565C0)
private val ScanConfirmColor        = Color(0xFF43A047)

/** DataWedge intent action — configure the same value in your DataWedge profile. */
private const val DW_ACTION   = "com.example.efridtracker.SCAN"
/** DataWedge extra key that carries the decoded barcode string. */
private const val DW_DATA_KEY = "com.symbol.datawedge.data_string"

@Composable
fun LocationScreen(
    viewModel: LocationViewModel = viewModel(),
    onNavigateToScan: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Register DataWedge broadcast receiver for the lifetime of this screen.
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val data = intent.getStringExtra(DW_DATA_KEY) ?: return
                viewModel.onBarcodeReceived(data)
            }
        }
        context.registerReceiver(receiver, IntentFilter(DW_ACTION))
        onDispose { context.unregisterReceiver(receiver) }
    }

    fun onSearch() {
        if (viewModel.validateLocation()) {
            onNavigateToScan(uiState.locationInput.trim())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "EF RFID Tracker",
            style = MaterialTheme.typography.headlineMedium,
            color = ScreenOnBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter a location to load inventory",
            style = MaterialTheme.typography.bodyMedium,
            color = ScreenOnBackgroundVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Drive sync status
        AnimatedVisibility(visible = uiState.isSyncing, enter = fadeIn(), exit = fadeOut()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = ScreenOnBackgroundVariant
                )
                Text(
                    text = "Syncing inventory...",
                    style = MaterialTheme.typography.bodySmall,
                    color = ScreenOnBackgroundVariant
                )
            }
        }

        if (uiState.syncError != null) {
            Text(
                text = "⚠ ${uiState.syncError}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFFB74D)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = uiState.locationInput,
            onValueChange = viewModel::onLocationChanged,
            label = { Text("Location/Zone", color = ScreenOnBackgroundVariant) },
            placeholder = { Text("e.g. A1-01", color = ScreenOnBackgroundVariant) },
            isError = uiState.error != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFF2C3E50),
                focusedContainerColor = Color(0xFF34495E),
                unfocusedTextColor = ScreenOnBackground,
                focusedTextColor = ScreenOnBackground,
                unfocusedBorderColor = Color(0xFF546E7A),
                focusedBorderColor = Color(0xFF90CAF9),
                cursorColor = Color(0xFF90CAF9)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Scan barcode button
        OutlinedButton(
            onClick = { /* hardware trigger or DataWedge soft-scan handled by receiver */ },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ScreenOnBackground),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⬛  Scan Barcode", color = ScreenOnBackground)
            }
        }

        // Confirmation badge — fades in for 1.5 s after a successful scan
        AnimatedVisibility(
            visible = uiState.barcodeScanned,
            enter = fadeIn(),
            exit  = fadeOut()
        ) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = ScanConfirmColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Barcode scanned",
                    style = MaterialTheme.typography.bodySmall,
                    color = ScanConfirmColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onSearch() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search Location")
        }
    }
}
