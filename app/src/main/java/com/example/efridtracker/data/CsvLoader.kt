package com.example.efridtracker.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CsvLoader(private val context: Context) {

    fun getItemsForLocation(location: String): List<InventoryItem> =
        loadAll().filter { it.location.equals(location.trim(), ignoreCase = true) }

    private fun loadAll(): List<InventoryItem> {
        val items = mutableListOf<InventoryItem>()
        val reader = cachedFile().takeIf { it.exists() }?.bufferedReader()
            ?: context.assets.open("csvfolder/inventory.csv").bufferedReader()
        reader.useLines { lines ->
            lines.drop(1).forEach { line ->
                val parts = line.split(",")
                if (parts.size >= 4) {
                    items.add(
                        InventoryItem(
                            location = parts[0].trim(),
                            item = parts[1].trim(),
                            upc = parts[2].trim(),
                            quantity = parts[3].trim().toIntOrNull() ?: 0
                        )
                    )
                }
            }
        }
        return items
    }

    private fun cachedFile(): File = File(context.filesDir, CACHE_FILE)

    companion object {
        private const val TAG = "CsvLoader"
        private const val CACHE_FILE = "inventory.csv"
        private const val DRIVE_FILE_ID = "1YVXruhpY8WyLUBHluasMZat5Dn-27oUL"

        /**
         * Downloads inventory.csv from Google Drive via service account and saves to internal cache.
         * Throws on failure — caller is responsible for fallback handling.
         */
        suspend fun syncFromDrive(context: Context) = withContext(Dispatchers.IO) {
            Log.d(TAG, "Syncing inventory from Drive...")
            val text = DriveUploader.download(context, DRIVE_FILE_ID)
            File(context.filesDir, CACHE_FILE).writeText(text)
            Log.d(TAG, "Drive sync complete (${text.lines().size} lines)")
        }
    }
}
