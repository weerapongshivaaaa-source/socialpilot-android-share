package ai.socialpilot.share

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.widget.Toast
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class ShareReceiverActivity : Activity() {
    private val endpoint = "https://socialpilot-ai-yvo2.hatchable.site/api/share/ingest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CookieManager.getInstance().setAcceptCookie(true)

        val uris = when (intent.action) {
            Intent.ACTION_SEND_MULTIPLE ->
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.toList() ?: emptyList()
            Intent.ACTION_SEND ->
                listOfNotNull(intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
            else -> emptyList()
        }

        if (uris.isEmpty()) {
            Toast.makeText(this, "No photo or video was received.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        Thread {
            try {
                val result = uploadOnce(uris.take(10))
                runOnUiThread {
                    Toast.makeText(this, "SocialPilot: $result", Toast.LENGTH_LONG).show()
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "SocialPilot: \${e.message ?: "Upload failed"}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }.start()
    }

    private fun uploadOnce(uris: List<Uri>): String {
        val boundary = "SocialPilot-" + UUID.randomUUID()
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = 30000
        connection.readTimeout = 120000
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

        DataOutputStream(connection.outputStream).use { out ->
            var sent = 0
            for ((i, uri) in uris.withIndex()) {
                val mime = contentResolver.getType(uri) ?: continue
                if (!mime.startsWith("image/") && !mime.startsWith("video/")) continue
                out.writeBytes("--$boundary\r\n")
                out.writeBytes("Content-Disposition: form-data; name=\"media\"; filename=\"shared-\${i + 1}\"\r\n")
                out.writeBytes("Content-Type: $mime\r\n\r\n")
                contentResolver.openInputStream(uri)?.use { input ->
                    input.copyTo(out)
                    sent++
                }
                out.writeBytes("\r\n")
            }
            out.writeBytes("--$boundary--\r\n")
            out.flush()
            if (sent == 0) throw IllegalStateException("No supported image/video files")
        }

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
        connection.disconnect()

        if (code == 413) throw IllegalStateException("This media file is too large. Please use a smaller file.")
        if (code == 401) throw IllegalStateException("Please open SocialPilot and sign in first.")
        if (code !in 200..299) throw IllegalStateException("Upload failed (HTTP $code)")
        return "posted successfully"
    }
}
