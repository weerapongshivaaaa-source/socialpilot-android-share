package ai.socialpilot.share

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
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
                    val uri = request.url
                    if (uri.scheme == "socialpilot") {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, uri))
                        } catch (_: Exception) {
                            Toast.makeText(this@MainActivity, "Open SocialPilot pairing from the website.", Toast.LENGTH_LONG).show()
                        }
                        return true
                    }
                    return false
                }
            }
        }

        setContentView(web)

        if (savedInstanceState == null) {
            val data = intent?.data
            val pairToken = data?.getQueryParameter("token")
            if (data?.scheme == "socialpilot" && data.host == "pair" && !pairToken.isNullOrBlank()) {
                web.loadUrl("https://socialpilot-ai-yvo2.hatchable.site/mobile-share.html?native=1&token=" + Uri.encode(pairToken))
            } else {
                web.loadUrl(homeUrl)
            }
        } else {
            web.restoreState(savedInstanceState)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        val uri = intent?.data
        if (uri?.scheme == "socialpilot" && uri.host == "pair") {
            val token = uri.getQueryParameter("token").orEmpty()
            if (token.isNotBlank()) {
                web.loadUrl("https://socialpilot-ai-yvo2.hatchable.site/mobile-share.html?native=1&token=" + Uri.encode(token))
            }
        }
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
