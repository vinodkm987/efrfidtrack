package com.example.efridtracker.rfid

import android.content.Context
import android.util.Log
import com.zebra.rfid.api3.ENUM_TRANSPORT
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE
import com.zebra.rfid.api3.InvalidUsageException
import com.zebra.rfid.api3.OperationFailureException
import com.zebra.rfid.api3.RFIDReader
import com.zebra.rfid.api3.ReaderDevice
import com.zebra.rfid.api3.Readers
import com.zebra.rfid.api3.RfidEventsListener
import com.zebra.rfid.api3.RfidReadEvents
import com.zebra.rfid.api3.RfidStatusEvents
import com.zebra.rfid.api3.STATUS_EVENT_TYPE
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class RfidManager(private val context: Context) {

    enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, ERROR }

    private companion object {
        const val TAG = "RfidManager"
    }

    private var readers: Readers? = null
    private var rfidReader: RFIDReader? = null

    // Emits (epc, upc) for every decoded tag read. Deduplication is the ViewModel's responsibility.
    private val _scannedTag = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 128)
    val scannedTag: SharedFlow<Pair<String, String>> = _scannedTag.asSharedFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val eventListener = object : RfidEventsListener {
        override fun eventReadNotify(e: RfidReadEvents) {
            val tags = rfidReader?.Actions?.getReadTags(100) ?: return
            for (tag in tags) {
                try {
                    val epc = tag?.getTagID() ?: continue
                    if (epc.isBlank()) continue
                    val upc = EpcDecoder.decodeToUpc(epc) ?: continue
                    _scannedTag.tryEmit(epc to upc)
                } catch (ex: Exception) {
                    Log.w(TAG, "Skipping tag due to error: ${ex.message}")
                }
            }
        }

        override fun eventStatusNotify(e: RfidStatusEvents) {
            when (e.StatusEventData.getStatusEventType()) {
                STATUS_EVENT_TYPE.DISCONNECTION_EVENT -> {
                    Log.d(TAG, "Reader disconnected")
                    _connectionState.value = ConnectionState.DISCONNECTED
                    _isScanning.value = false
                }
                STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT -> {
                    when (e.StatusEventData.HandheldTriggerEventData.getHandheldEvent()) {
                        HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED -> startScan()
                        HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED -> stopScan()
                        else -> {}
                    }
                }
                else -> {}
            }
        }
    }

    fun connect() {
        if (_connectionState.value == ConnectionState.CONNECTED) return
        _connectionState.value = ConnectionState.CONNECTING
        try {
            // TC27 + RFD4030 via e-Connectix: use the Zebra RFID host service transport.
            // SERVICE_USB is the Zebra inter-process RFID service, not literal USB.
            readers = Readers(context, ENUM_TRANSPORT.SERVICE_USB)
            val available = readers?.GetAvailableRFIDReaderList()

            if (available.isNullOrEmpty()) {
                Log.e(TAG, "No RFID readers found via Zebra e-Connectix service")
                _connectionState.value = ConnectionState.ERROR
                return
            }

            val device: ReaderDevice = available[0]
            rfidReader = device.getRFIDReader()
            rfidReader?.connect()

            rfidReader?.Events?.apply {
                setHandheldEvent(true)
                setTagReadEvent(true)
                setAttachTagDataWithReadEvent(false)
                addEventsListener(eventListener)
            }

            rfidReader?.Config?.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true)

            _connectionState.value = ConnectionState.CONNECTED
            Log.d(TAG, "Connected to ${device.getName()} via e-Connectix")
        } catch (e: InvalidUsageException) {
            Log.e(TAG, "InvalidUsageException on connect", e)
            _connectionState.value = ConnectionState.ERROR
        } catch (e: OperationFailureException) {
            Log.e(TAG, "OperationFailureException on connect", e)
            _connectionState.value = ConnectionState.ERROR
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error on connect", e)
            _connectionState.value = ConnectionState.ERROR
        }
    }

    fun startScan() {
        if (_isScanning.value) return
        try {
            rfidReader?.Actions?.Inventory?.perform()
            _isScanning.value = true
            Log.d(TAG, "Scan started")
        } catch (e: InvalidUsageException) {
            Log.e(TAG, "InvalidUsageException on startScan", e)
        } catch (e: OperationFailureException) {
            Log.e(TAG, "OperationFailureException on startScan", e)
        }
    }

    fun stopScan() {
        if (!_isScanning.value) return
        try {
            rfidReader?.Actions?.Inventory?.stop()
            _isScanning.value = false
            Log.d(TAG, "Scan stopped")
        } catch (e: InvalidUsageException) {
            Log.e(TAG, "InvalidUsageException on stopScan", e)
        } catch (e: OperationFailureException) {
            Log.e(TAG, "OperationFailureException on stopScan", e)
        }
    }

    fun disconnect() {
        stopScan()
        try {
            rfidReader?.Events?.removeEventsListener(eventListener)
            rfidReader?.disconnect()
            readers?.Dispose()
        } catch (e: Exception) {
            Log.e(TAG, "Error on disconnect", e)
        } finally {
            rfidReader = null
            readers = null
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }
}
