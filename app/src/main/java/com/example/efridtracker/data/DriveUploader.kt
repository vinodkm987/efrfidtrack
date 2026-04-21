package com.example.efridtracker.data

import android.content.Context
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec

/**
 * Uploads a file to Google Drive using a service account.
 *
 * Prerequisites:
 *  1. Place your service account JSON key at: app/src/main/assets/quantum-yen-491918-p4-81a1c2527441.json
 *  2. Share the target Drive folder with the service account email as Editor.
 *  3. Share the inventory Google Sheet with the service account email as Viewer.
 *
 * The uploaded file appears in the Drive folder shared with the service account.
 */
object DriveUploader {

    private const val TAG = "DriveUploader"
    private const val TOKEN_URL = "https://oauth2.googleapis.com/token"
    private const val UPLOAD_URL =
        "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
    private const val SCOPE = "https://www.googleapis.com/auth/drive"
    private const val SA_ASSET = "quantum-yen-491918-p4-81a1c2527441.json"

    // Share the folder with scannerrfid81@quantum-yen-491918-p4.iam.gserviceaccount.com (Editor)
    private const val DRIVE_FOLDER_ID = "1KKKEUrjOagrH8vnOww8aJ02PRCS5cZmX"

    /**
     * Uploads [csvFile] to Google Drive.
     * Returns the Drive file ID on success, throws on failure.
     */
    suspend fun upload(context: Context, csvFile: File): String = withContext(Dispatchers.IO) {
        val sa = loadServiceAccount(context)
        val token = getAccessToken(sa)
        uploadFile(csvFile, token)
    }

    /**
     * Downloads a Google Sheet from Drive as CSV using service account auth.
     * The sheet must be shared with the service account email as at least Viewer.
     */
    suspend fun download(context: Context, fileId: String): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "download: fetching file $fileId via service account")
        val sa = loadServiceAccount(context)
        val token = getAccessToken(sa)
        val conn = URL("https://www.googleapis.com/drive/v3/files/$fileId/export?mimeType=text%2Fcsv")
            .openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.connectTimeout = 10_000
        conn.readTimeout = 15_000
        try {
            val code = conn.responseCode
            if (code != 200) {
                val err = conn.errorStream?.bufferedReader()?.readText() ?: "no body"
                Log.e(TAG, "download: FAILED ($code): $err")
                throw Exception("Drive download failed ($code): $err")
            }
            val text = conn.inputStream.bufferedReader().readText()
            Log.d(TAG, "download: success, ${text.lines().size} lines")
            text
        } finally {
            conn.disconnect()
        }
    }

    // ── Service account JSON ──────────────────────────────────────────────────

    private data class ServiceAccount(
        val clientEmail: String,
        val privateKeyPem: String,
        val tokenUri: String
    )

    private fun loadServiceAccount(context: Context): ServiceAccount {
        val json = context.assets.open(SA_ASSET).bufferedReader().readText()
        val obj = JSONObject(json)
        return ServiceAccount(
            clientEmail  = obj.getString("client_email"),
            privateKeyPem = obj.getString("private_key"),
            tokenUri     = obj.optString("token_uri", TOKEN_URL)
        )
    }

    // ── JWT + token exchange ──────────────────────────────────────────────────

    private fun getAccessToken(sa: ServiceAccount): String {
        val now = System.currentTimeMillis() / 1000
        val header = base64url("""{"alg":"RS256","typ":"JWT"}""")
        val claims = base64url(
            """{"iss":"${sa.clientEmail}","scope":"$SCOPE","aud":"${sa.tokenUri}","iat":$now,"exp":${now + 3600}}"""
        )
        val unsigned = "$header.$claims"
        val signature = signRs256(unsigned, sa.privateKeyPem)
        val jwt = "$unsigned.$signature"

        val body = "grant_type=${encode("urn:ietf:params:oauth:grant-type:jwt-bearer")}&assertion=${encode(jwt)}"
        val conn = URL(sa.tokenUri).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        try {
            conn.outputStream.write(body.toByteArray())
            val code = conn.responseCode
            val response = if (code in 200..299)
                conn.inputStream.bufferedReader().readText()
            else
                conn.errorStream?.bufferedReader()?.readText() ?: "no body"
            if (code !in 200..299) {
                Log.e(TAG, "getAccessToken: FAILED ($code): $response")
                throw Exception("OAuth token request failed ($code): $response")
            }
            return JSONObject(response).getString("access_token")
        } finally {
            conn.disconnect()
        }
    }

    private fun signRs256(data: String, pemKey: String): String {
        val stripped = pemKey
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\n", "")
            .replace("\n", "")
            .trim()
        val keyBytes = Base64.decode(stripped, Base64.DEFAULT)
        val privateKey = KeyFactory.getInstance("RSA")
            .generatePrivate(PKCS8EncodedKeySpec(keyBytes))
        val sig = java.security.Signature.getInstance("SHA256withRSA")
        sig.initSign(privateKey)
        sig.update(data.toByteArray())
        return Base64.encodeToString(sig.sign(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    // ── Multipart upload ──────────────────────────────────────────────────────

    private fun uploadFile(file: File, token: String): String {
        val boundary = "efridtracker_boundary_${System.currentTimeMillis()}"
        val metadata = if (DRIVE_FOLDER_ID.isNotBlank())
            """{"name":"${file.name}","parents":["$DRIVE_FOLDER_ID"]}"""
        else
            """{"name":"${file.name}"}"""
        val csvBytes = file.readBytes()

        val body = buildString {
            append("--$boundary\r\n")
            append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            append("$metadata\r\n")
            append("--$boundary\r\n")
            append("Content-Type: text/csv\r\n\r\n")
        }.toByteArray() + csvBytes + "\r\n--$boundary--\r\n".toByteArray()

        val conn = URL(UPLOAD_URL).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
        conn.setRequestProperty("Content-Length", body.size.toString())
        try {
            conn.outputStream.write(body)
            val responseCode = conn.responseCode
            val response = if (responseCode in 200..299)
                conn.inputStream.bufferedReader().readText()
            else
                conn.errorStream?.bufferedReader()?.readText() ?: "Unknown error"

            if (responseCode !in 200..299) {
                throw Exception("Drive upload failed ($responseCode): $response")
            }
            val fileId = JSONObject(response).getString("id")
            Log.d(TAG, "Uploaded to Drive: $fileId")
            return fileId
        } finally {
            conn.disconnect()
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun base64url(input: String): String =
        Base64.encodeToString(input.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

    private fun encode(value: String): String =
        java.net.URLEncoder.encode(value, "UTF-8")
}
