package to.dev.dev_android.view.main.view

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat.startActivity
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.KeyEvent
import android.view.View
import to.dev.dev_android.databinding.ActivityMainBinding

class CustomWebViewClient(val context: Context, val binding: ActivityMainBinding) : WebViewClient() {
    override fun onPageFinished(view: WebView, url: String?) {
        binding.splash.visibility = View.GONE
        view.visibility = View.VISIBLE
        super.onPageFinished(view, url)
    }

    override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
        Log.d("CustomWebViewClient", "shouldOverrideKeyEvent (line 24): Key event: $event.keyCode" )
        return super.shouldOverrideKeyEvent(view, event)
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        Log.d("CustomWebViewClient", "shouldOverrideUrlLoading (line 23): URL: $url")
        if (url.contains("://dev.to")) {
            return false
        } else {
            if(url.contains("api.twitter.com/oauth") or url.contains("github.com/login")) {
                openBrowser(url)
                return true
            }
            val builder = CustomTabsIntent.Builder()
            builder.setToolbarColor(-0x1000000)
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(context, Uri.parse(url))
            return true
        }

    }

    private fun openBrowser(url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
        return true
    }

    override fun onUnhandledKeyEvent(view: WebView?, event: KeyEvent?) {
        Log.d("CustomWebViewClient", "shouldOverrideKeyEvent (line 24): Key event: $event.keyCode" )
        super.onUnhandledKeyEvent(view, event)
    }
}