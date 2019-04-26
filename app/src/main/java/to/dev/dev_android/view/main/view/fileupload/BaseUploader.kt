package to.dev.dev_android.view.main.view.fileupload

import java.io.File

abstract class BaseUploader(
    protected var mBucketUrl: String,
    protected var mData: S3Data,
    protected var mCallback: ResponseCallback?
) {

    abstract fun upload(imageData: ByteArray)

    abstract fun upload(imageData: File)
}