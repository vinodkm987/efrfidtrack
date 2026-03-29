package com.example.efridtracker.ui.scan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.efridtracker.data.CsvLoader
import com.example.efridtracker.rfid.RfidManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanItem(
    val location: String,
    val item: String,
    val upc: String,
    val expectedQuantity: Int,
    val scannedCount: Int = 0
) {
    val isComplete: Boolean get() = scannedCount >= expectedQuantity
}

data class ScanUiState(
    val items: List<ScanItem> = emptyList(),
    val isScanning: Boolean = false,
    val connectionState: RfidManager.ConnectionState = RfidManager.ConnectionState.DISCONNECTED
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
            rfidManager.scannedUpc.collect { upc ->
                _uiState.update { state ->
                    state.copy(
                        items = state.items.map { item ->
                            if (item.upc == upc) item.copy(scannedCount = item.scannedCount + 1)
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

    fun onStartStopScan() {
        if (_uiState.value.isScanning) rfidManager.stopScan()
        else rfidManager.startScan()
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
