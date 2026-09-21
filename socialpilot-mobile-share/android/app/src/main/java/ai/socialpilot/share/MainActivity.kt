package ai.socialpilot.share

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {
    private lateinit var web: WebView
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null
    private val fileChooserRequestCode = 4101
    private val homeUrl = "https://socialpilot-ai-yvo2.hatchable.site/?native=1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CookieManager.getInstance().setAcceptCookie(true)

        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportMultipleWindows(false)
            settings.loadsImagesAutomatically = true
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean = false

                @Suppress("DEPRECATION")
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean = false
            }

            webChromeClient = object : WebChromeClient() {
                override fun onShowFileChooser(
                    webView: WebView,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams
                ): Boolean {
                    fileChooserCallback?.onReceiveValue(null)
                    fileChooserCallback = filePathCallback

                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                    }

                    return try {
                        startActivityForResult(intent, fileChooserRequestCode)
                        true
                    } catch (_: Exception) {
                        fileChooserCallback?.onReceiveValue(null)
                        fileChooserCallback = null
                        false
                    }
                }
            }
        }

        setContentView(web)
        if (savedInstanceState == null) web.loadUrl(homeUrl) else web.restoreState(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != fileChooserRequestCode) return
        val results = if (resultCode == RESULT_OK && data != null) {
            data.clipData?.let { clip ->
                Array(clip.itemCount) { i -> clip.getItemAt(i).uri }
            } ?: data.data?.let { arrayOf(it) }
        } else null
        fileChooserCallback?.onReceiveValue(results)
        fileChooserCallback = null
    }

    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        web.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        fileChooserCallback?.onReceiveValue(null)
        fileChooserCallback = null
        web.destroy()
        super.onDestroy()
    }
}
