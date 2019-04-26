package to.dev.dev_android.view.main.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import to.dev.dev_android.R
import to.dev.dev_android.base.activity.BaseActivity
import to.dev.dev_android.databinding.ActivityMainBinding
import to.dev.dev_android.view.main.view.fileupload.OkHttpUploader
import to.dev.dev_android.view.main.view.fileupload.S3Data
import java.io.File


class MainActivity : BaseActivity<ActivityMainBinding>(), CustomWebChromeClient.CustomListener {

    override fun layout(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWebViewSettings()
        navigateToHome()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val appLinkData: Uri? = intent.data
        if (appLinkData != null) {
            binding.webView.loadUrl(appLinkData.toString())
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setWebViewSettings() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.webViewClient = CustomWebViewClient(this@MainActivity, binding)
        binding.webView.webChromeClient = CustomWebChromeClient(this@MainActivity, binding, this)
    }

    private fun navigateToHome() {
        binding.webView.loadUrl(resources.getString(R.string.main_url))
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun launchFileBrowser() {
        val intent = Intent()
        // Show only images, no videos or anything else
        intent.type = "image/*"
        intent.action = Intent.ACTION_PICK
        // Always show the chooser (if there are multiple options available)
        ActivityCompat.startActivityForResult(
            this,
            Intent.createChooser(intent, "Select Picture"),
            100,
            null
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                if (data != null) {
                    Log.d("MainActivity", "onActivityResult (line 76): data: ${data.data}")
                    val realPathFromUri = getRealPathFromUri(this, data.data)
                    val picPath = realPathFromUri.split("/").last()
                    Log.d("MainActivity", "onActivityResult (line 87): picPath: $picPath" )
                    Log.d("MainActivity", "onActivityResult (line 82): $realPathFromUri")
                    val s3Data = S3Data(
                        picPath,
                        "AKIAIIZT567EAZJDZYYA",
                        "596f435962c528881727e3dafcd8aded70ef0eafb3e9e2da8b4daf1e44e55ce1",
                        "arn:aws:s3:::test-dev.to-profile-pics",
                        "",
                        "test-dev.to-profile-pics",
                        "https://s3-us-west-2.amazonaws.com/test-dev.to-profile-pics",
                        ""
                    )
                    val uploader = OkHttpUploader("https://s3-us-west-2.amazonaws.com/test-dev.to-profile-pics", s3Data, null)
                    uploader.upload(File(realPathFromUri))
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getRealPathFromUri(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }
}
