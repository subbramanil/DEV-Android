package to.dev.dev_android.view.main.view

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.io.IOException

class FileUploaderUtil {

    @Throws(IOException::class)
    fun uploadProfilePic(filePath: String) {
        val client = OkHttpClient()
        val file = File(filePath)

        val request = Request.Builder()
            .url("https://api.github.com/markdown/raw")
            .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, file))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $response")
            } else {
                println(response.code())
            }

        }
    }

    companion object {
        private val MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8")
    }
}