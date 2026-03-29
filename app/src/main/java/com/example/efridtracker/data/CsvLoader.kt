package com.example.efridtracker.data

import android.content.Context

class CsvLoader(private val context: Context) {

    fun getItemsForLocation(location: String): List<InventoryItem> =
        loadAll().filter { it.location.equals(location.trim(), ignoreCase = true) }

    private fun loadAll(): List<InventoryItem> {
        val items = mutableListOf<InventoryItem>()
        context.assets.open("csvfolder/inventory.csv").bufferedReader().useLines { lines ->
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
}
