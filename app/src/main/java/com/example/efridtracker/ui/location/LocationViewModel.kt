package com.example.efridtracker.ui.location

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.efridtracker.data.CsvLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LocationUiState(
    val locationInput: String = "",
    val error: String? = null
)

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val csvLoader = CsvLoader(application)

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    fun onLocationChanged(value: String) {
        _uiState.update { it.copy(locationInput = value, error = null) }
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
