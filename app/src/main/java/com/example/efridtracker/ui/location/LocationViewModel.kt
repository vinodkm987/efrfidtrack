package com.example.efridtracker.ui.location

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.efridtracker.data.CsvLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LocationUiState(
    val locationInput: String = "",
    val error: String? = null,
    val barcodeScanned: Boolean = false,   // brief visual feedback after a scan
    val isSyncing: Boolean = false,
    val syncError: String? = null
)

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val csvLoader = CsvLoader(application)

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    init {
        syncInventory()
    }

    private fun syncInventory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncError = null) }
            try {
                CsvLoader.syncFromDrive(getApplication())
            } catch (e: Exception) {
                _uiState.update { it.copy(syncError = "Could not refresh — using cached data") }
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun onLocationChanged(value: String) {
        _uiState.update { it.copy(locationInput = value, error = null) }
    }

    /** Called when DataWedge delivers a scanned barcode. */
    fun onBarcodeReceived(value: String) {
        _uiState.update { it.copy(locationInput = value.trim(), error = null, barcodeScanned = true) }
        viewModelScope.launch {
            delay(1500)
            _uiState.update { it.copy(barcodeScanned = false) }
        }
    }

    /** Returns true if the location is non-empty and has items in the CSV. */
    fun validateLocation(): Boolean {
        val location = _uiState.value.locationInput.trim()
        if (location.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter a location") }
            return false
        }
        if (csvLoader.getItemsForLocation(location).isEmpty()) {
            _uiState.update { it.copy(error = "No items found for \"$location\"") }
            return false
        }
        return true
    }
}
