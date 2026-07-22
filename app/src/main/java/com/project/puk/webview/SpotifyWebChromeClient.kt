package com.project.puk.webview

import android.os.Handler
import android.os.Looper
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

class SpotifyWebChromeClient(
    private val onProgressChanged: ((Int) -> Unit)? = null
) : WebChromeClient() {

    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: android.os.Message?
    ): Boolean {
        val parentWebView = view ?: return false
        val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false

        // Intercept target="_blank" / window.open links and load them directly in the parent WebView
        val tempWebView = WebView(parentWebView.context).apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: android.webkit.WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString()
                    if (url != null && url != "about:blank") {
                        parentWebView.loadUrl(url)
                    }
                    return true
                }

                @Suppress("DEPRECATION")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url != null && url != "about:blank") {
                        parentWebView.loadUrl(url)
                    }
                    return true
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    if (url != null && url != "about:blank") {
                        parentWebView.loadUrl(url)
                        view?.stopLoading()
                    }
                }
            }
        }

        transport.webView = tempWebView
        resultMsg.sendToTarget()
        return true
    }

    @Suppress("DEPRECATION")
    override fun onPermissionRequest(permissionRequest: PermissionRequest?) {
        permissionRequest ?: return
        Handler(Looper.getMainLooper()).post {
            val resources = permissionRequest.resources
            if (resources.contains(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID)) {
                permissionRequest.grant(resources)
            } else {
                permissionRequest.deny()
            }
        }
    }

    override fun onConsoleMessage(message: String?, lineNumber: Int, sourceId: String?) {
        android.util.Log.d("SpotifyJS", "$message [$sourceId:$lineNumber]")
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onProgressChanged?.invoke(newProgress)
    }
}
