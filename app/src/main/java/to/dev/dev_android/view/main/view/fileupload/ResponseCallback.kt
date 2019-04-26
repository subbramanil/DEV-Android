package to.dev.dev_android.view.main.view.fileupload

import org.json.JSONObject

interface ResponseCallback {
    fun onSuccess(result: JSONObject)

    fun onError(msg: String)

    fun onFailure(msg: String)
}