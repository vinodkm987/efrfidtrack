package com.example.efridtracker.rfid

/**
 * Decodes GS1 SGTIN-96 EPC strings to UPC-A.
 *
 * Verified against: EPC 30340C23BC01BA974876E8B1 → UPC 198895017705
 */
object EpcDecoder {

    // Partition → Pair(companyPrefixBits to cpDigits, itemRefBits to irDigits)
    private data class PartitionEntry(
        val cpBits: Int, val cpDigits: Int,
        val irBits: Int, val irDigits: Int
    )

    private val partitionTable = mapOf(
        0 to PartitionEntry(40, 12, 4, 1),
        1 to PartitionEntry(37, 11, 7, 2),
        2 to PartitionEntry(34, 10, 10, 3),
        3 to PartitionEntry(30, 9, 14, 4),
        4 to PartitionEntry(27, 8, 17, 5),
        5 to PartitionEntry(24, 7, 20, 6),
        6 to PartitionEntry(20, 6, 24, 7)
    )

    /**
     * Decodes an EPC hex string to UPC-A (12 digits).
     * Returns null if the EPC is not a valid SGTIN-96 or cannot be decoded.
     */
    fun decodeToUpc(epc: String): String? {
        val clean = epc.replace(" ", "").uppercase()
        if (clean.length < 24) return null

        val bytes = try {
            clean.chunked(2).map { it.toInt(16) }
        } catch (e: NumberFormatException) {
            return null
        }

        // Header 0x30 = SGTIN-96
        if (bytes[0] != 0x30) return null

        val bits = bytes.joinToString("") { it.toString(2).padStart(8, '0') }

        val partition = bits.substring(11, 14).toInt(2)
        val entry = partitionTable[partition] ?: return null

        val cpStart = 14
        val irStart = cpStart + entry.cpBits

        val companyPrefix = bits.substring(cpStart, cpStart + entry.cpBits).toLong(2)
        val itemReference = bits.substring(irStart, irStart + entry.irBits).toLong(2)

        val cpStr = companyPrefix.toString().padStart(entry.cpDigits, '0')
        val irStr = itemReference.toString().padStart(entry.irDigits, '0')

        // GTIN-14 body = indicator (irStr[0]) + companyPrefix + itemRef remainder
        val gtin13Body = "${irStr[0]}$cpStr${irStr.substring(1)}"
        val check = calculateCheckDigit(gtin13Body)
        val gtin14 = "$gtin13Body$check"

        return when {
            gtin14.startsWith("00") -> gtin14.substring(2)  // UPC-A (12 digits)
            gtin14.startsWith("0") -> gtin14.substring(1)   // EAN-13 (13 digits)
            else -> gtin14
        }
    }

    private fun calculateCheckDigit(body: String): Int {
        val sum = body.reversed().mapIndexed { idx, c ->
            val d = c.digitToInt()
            if (idx % 2 == 0) d * 3 else d
        }.sum()
        return (10 - sum % 10) % 10
    }
}
