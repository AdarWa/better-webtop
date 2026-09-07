package net.adarw.bettersmartschool.auth

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginWebView(
    url: String,
    modifier: Modifier = Modifier,
    onLoginSuccess: (ParsedTokens) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                // Clears existing session data to enforce a clean login state
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
                WebStorage.getInstance().deleteAllData()
                clearCache(true)

                webViewClient = object : WebViewClient() {

                    private fun checkCookie(currentUrl: String?) {
                        if (currentUrl == null) return
                        val cookieManager = CookieManager.getInstance()
                        val cookies = cookieManager.getCookie(currentUrl) ?: return

                        parseTokens(cookies)?.let {
                            onLoginSuccess(it)
                        }
                    }

                    override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        checkCookie(url)
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, request: android.webkit.WebResourceRequest): Boolean {
                        checkCookie(request.url.toString())
                        return super.shouldOverrideUrlLoading(view, request)
                    }

                    override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
                        super.doUpdateVisitedHistory(view, url, isReload)
                        checkCookie(url)
                    }

                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        checkCookie(url)
                    }
                }
                loadUrl(url)
            }
        },
        update = { webView ->
            webView.loadUrl(url)
        }
    )
}

data class ParsedTokens(
    val uniqueId: String,
    val webToken: String
)

fun parseTokens(cookieString: String): ParsedTokens? {
    val cookieMap = cookieString.split(";")
        .associate {
            val parts = it.trim().split("=", limit = 2)
            val key = parts.firstOrNull() ?: ""
            val value = parts.getOrNull(1) ?: ""
            key to value
        }
    if(!cookieMap.containsKey("uniqueId") || !cookieMap.containsKey("webToken")) {
        return null
    }
    return ParsedTokens(
        uniqueId = cookieMap.getOrDefault("uniqueId", ""),
        webToken = cookieMap.getOrDefault("webToken", "")
    )
}