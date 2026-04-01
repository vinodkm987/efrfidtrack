package com.example.efridtracker.ui.scan

import android.app.Application
import android.content.Context
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.efridtracker.data.CsvLoader
import com.example.efridtracker.data.DriveUploader
import com.example.efridtracker.rfid.RfidManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class ScanItem(
    val location: String,
    val item: String,
    val upc: String,
    val expectedQuantity: Int,
    val scannedEpcs: Set<String> = emptySet()
) {
    val scannedCount: Int get() = scannedEpcs.size
    val isComplete: Boolean get() = scannedCount >= expectedQuantity
}

data class ScanUiState(
    val items: List<ScanItem> = emptyList(),
    val isScanning: Boolean = false,
    val connectionState: RfidManager.ConnectionState = RfidManager.ConnectionState.DISCONNECTED,
    val exportEnabled: Boolean = false,   // activates once Stop is pressed
    val exportMessage: String? = null
)

class ScanViewModel(
    application: Application,
    private val location: String,
    private val rfidManager: RfidManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    init {
        loadItems()
        observeRfidState()
    }

    private fun loadItems() {
        val items = CsvLoader(getApplication()).getItemsForLocation(location).map {
            ScanItem(it.location, it.item, it.upc, it.quantity)
        }
        _uiState.update { it.copy(items = items) }
    }

    private fun observeRfidState() {
        viewModelScope.launch {
            rfidManager.connectionState.collect { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
        }
        viewModelScope.launch {
            rfidManager.isScanning.collect { scanning ->
                _uiState.update { it.copy(isScanning = scanning) }
            }
        }
        viewModelScope.launch {
            rfidManager.scannedTag.collect { (epc, upc) ->
                _uiState.update { state ->
                    state.copy(
                        items = state.items.map { item ->
                            if (item.upc == upc && epc !in item.scannedEpcs)
                                item.copy(scannedEpcs = item.scannedEpcs + epc)
                            else item
                        }
                    )
                }
            }
        }
    }

    fun connect() {
        viewModelScope.launch(Dispatchers.IO) {
            rfidManager.connect()
        }
    }

    fun onStartScan() {
        // Reset all EPC sets and export state before each new scan session
        _uiState.update { state ->
            state.copy(
                items = state.items.map { it.copy(scannedEpcs = emptySet()) },
                exportEnabled = false
            )
        }
        rfidManager.startScan()
    }

    fun onStopScan() {
        rfidManager.stopScan()
        _uiState.update { it.copy(exportEnabled = true) }
    }

    fun exportCsv(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Write local copy
                val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                val file = File(dir, "inventory_scan.csv")
                file.bufferedWriter().use { writer ->
                    writer.write("Location,Item,UPC,On Hand,Scanned\n")
                    _uiState.value.items.forEach { item ->
                        writer.write("${item.location},\"${item.item}\",${item.upc},${item.expectedQuantity},${item.scannedCount}\n")
                    }
                }

                // Upload to Google Drive
                _uiState.update { it.copy(exportMessage = "Uploading to Drive...") }
                try {
                    DriveUploader.upload(context, file)
                    _uiState.update { it.copy(exportMessage = "Uploaded to Drive ✓") }
                } catch (uploadEx: Exception) {
                    _uiState.update { it.copy(exportMessage = "Saved locally (Drive upload failed: ${uploadEx.message})") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportMessage = "Export failed: ${e.message}") }
            }
        }
    }

    fun clearExportMessage() {
        _uiState.update { it.copy(exportMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        rfidManager.disconnect()
    }
}

class ScanViewModelFactory(
    private val application: Application,
    private val location: String,
    private val rfidManager: RfidManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        ScanViewModel(application, location, rfidManager) as T
}
