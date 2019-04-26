package to.dev.dev_android.view.main.view.fileupload

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

class OkHttpUploader(bucketUrl: String, data: S3Data, cb: ResponseCallback?) : BaseUploader(bucketUrl, data, cb) {

    override fun upload(imageData: ByteArray) {
        val requestBody = getS3RequestBody(mData)
            .addFormDataPart("file", "upload.jpg", RequestBody.create(MediaType.parse("image/jpeg"), imageData))
            .build()
        makeMultipartS3Request(requestBody)
    }

    override fun upload(imageData: File) {
        val requestBody = getS3RequestBody(mData)
            .addFormDataPart("file", "upload.jpg", RequestBody.create(MediaType.parse("image/jpeg"), imageData))
            .build()
        makeMultipartS3Request(requestBody)
    }

    private fun getS3RequestBody(data: S3Data): MultipartBody.Builder {
        return MultipartBody.Builder()
            .addFormDataPart("key", data.key)
            .addFormDataPart("AWSAccessKeyId", data.awsAccessKeyId)
//            .addFormDataPart("acl", data.acl)
//            .addFormDataPart("policy", data.policy)
//            .addFormDataPart("signature", data.signature)
            .addFormDataPart("Content-Type", "image/jpeg")
//            .addFormDataPart("success_action_status", data.successActionStatus)
    }

    private fun makeMultipartS3Request(requestBody: RequestBody) {
        makeMultipartS3RequestHelper(requestBody, 0)
    }

    private fun makeMultipartS3RequestHelper(requestBody: RequestBody, currentAttempt: Int) {
        val request = Request.Builder()
            .url(mBucketUrl)
            .post(requestBody)
            .build()

        val threadLooper = Looper.myLooper() // looper of the calling thread
        val threadHandler = if (threadLooper != null) Handler(threadLooper) else null
        val client = OkHttpClient()  // You'd normally keep just one instance of OkHttpClient.
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "Failure doing multipart request" + request.url())
                if (currentAttempt >= 1) {
                    parseS3FailureResponse(request, threadHandler)
                } else {
                    Log.d(TAG, "retrying s3 upload $mBucketUrl")
                    makeMultipartS3RequestHelper(requestBody, currentAttempt + 1)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                parseS3Response(response, threadHandler)
            }
        })
    }

    private fun parseS3FailureResponse(request: Request, threadHandler: Handler?) {
        if (threadHandler != null) {
            threadHandler.post(Runnable { mCallback?.onFailure("Network Error:  headers: " + request.headers().toString()) })
        } else {
            mCallback?.onFailure("Network Error")
        }
    }

    private fun parseS3Response(response: Response, threadHandler: Handler?) {
        try {
            if (response.isSuccessful()) {
                onResponseSuccess(response, threadHandler)
            } else {
                onResponseError(response, threadHandler)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    @Throws(JSONException::class)
    private fun onResponseSuccess(response: Response, threadHandler: Handler?) {
        val responseHeaders = response.headers()
        for (i in 0 until responseHeaders.size()) {
            if ("Location" == responseHeaders.name(i)) {
                var uploadedUrl = responseHeaders.value(i)
                try {
                    uploadedUrl = URLDecoder.decode(uploadedUrl, "utf-8")
                } catch (e: UnsupportedEncodingException) {
                    Log.d(TAG, "unsupported encoding exception")
                }

                Log.d(TAG, "Uploaded: url=$uploadedUrl")
                val responseJson = JSONObject()
                responseJson.put("url", uploadedUrl)
                if (threadHandler != null) {
                    threadHandler.post(Runnable {
                        try {
                            mCallback?.onSuccess(responseJson)
                        } catch (e: JSONException) {
                            Log.d(TAG, "exce")
                        }
                    })
                } else {
                    mCallback?.onSuccess(responseJson)
                }
                return
            }
        }
    }

    @Throws(JSONException::class)
    private fun onResponseError(response: Response, threadHandler: Handler?) {
        if (threadHandler != null) {
            threadHandler.post(Runnable { mCallback?.onError("Upload Failed!") })
        } else {
            mCallback?.onError("Upload Failed!")
        }
    }

    companion object {
        private const val TAG = "OkHttpUploader"
    }
}