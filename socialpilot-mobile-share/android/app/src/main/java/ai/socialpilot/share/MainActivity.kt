package ai.socialpilot.share

import android.app.Activity
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var web: WebView
    private val homeUrl = "https://socialpilot-ai-yvo2.hatchable.site/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = true
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    if (request.url.scheme == "socialpilot") {
                        Toast.makeText(this@MainActivity, "Gallery sharing is ready. Return to Gallery and tap Share → SocialPilot AI.", Toast.LENGTH_LONG).show()
                        return true
                    }
                    return false
                }
            }
        }
        setContentView(web)
        if (savedInstanceState == null) web.loadUrl(homeUrl) else web.restoreState(savedInstanceState)
    }

    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        web.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        web.destroy()
        super.onDestroy()
    }
}
