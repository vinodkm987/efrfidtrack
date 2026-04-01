package com.example.efridtracker.ui.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.efridtracker.rfid.RfidManager

private val CardDefault      = Color(0xFF2E2E2E)
private val CardDefaultText  = Color(0xFFECECEC)
private val CardComplete     = Color(0xFF2E7D32)   // deep green
private val CardCompleteText = Color(0xFFFFFFFF)
private val GreenComplete    = Color(0xFF43A047)   // connection status dot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.connect()
    }

    val view = LocalView.current
    LaunchedEffect(uiState.isScanning) {
        view.keepScreenOn = uiState.isScanning
    }

    // Show export result in Snackbar
    LaunchedEffect(uiState.exportMessage) {
        uiState.exportMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearExportMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan — ${uiState.items.firstOrNull()?.location ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
            HorizontalDivider()
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Reader: ${uiState.connectionState.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (uiState.connectionState == RfidManager.ConnectionState.CONNECTED)
                        GreenComplete else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = viewModel::onStartScan,
                        enabled = uiState.connectionState == RfidManager.ConnectionState.CONNECTED && !uiState.isScanning,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Start Scan")
                    }
                    Button(
                        onClick = viewModel::onStopScan,
                        enabled = uiState.isScanning,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Stop Scan")
                    }
                }
            }
            } // Surface
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                items(uiState.items.filter { !it.isComplete }) { item ->
                    ItemCard(item)
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
            }

            // Export button — sits above the page navigation
            OutlinedButton(
                onClick = { viewModel.exportCsv(context) },
                enabled = uiState.exportEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text("Export to CSV")
            }

        }
    }
}

@Composable
private fun ItemCard(item: ScanItem) {
    val bg   = if (item.isComplete) CardComplete  else CardDefault
    val text = if (item.isComplete) CardCompleteText else CardDefaultText

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.location,
                style = MaterialTheme.typography.labelSmall,
                color = text.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.item,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = text
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "UPC: ${item.upc}",
                style = MaterialTheme.typography.bodySmall,
                color = text.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "On Hand: ${item.expectedQuantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = text
                )
                Text(
                    text = "Scanned: ${item.scannedCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = text
                )
            }
        }
    }
}
