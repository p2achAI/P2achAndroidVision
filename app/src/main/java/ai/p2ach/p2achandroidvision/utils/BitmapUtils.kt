package ai.p2ach.p2achandroidvision.utils

import ai.p2ach.p2achandroidvision.Const
import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent
import java.io.File

suspend fun Bitmap.saveBitmapAsJpeg(

): File = withContext(Dispatchers.IO) {

    delay(10000)

    val context = KoinJavaComponent.get<Context>(Context::class.java)

    val directory = File(context.filesDir, Const.LOCAL.FILE.IMAGE.CAPTURE_FILE_DIR).apply {
        if (!exists()) mkdirs()
    }

    val file = File(directory, System.currentTimeMillis().toString()+ Const.LOCAL.FILE.IMAGE.FORMAT)

    file.outputStream().use { out ->
        this@saveBitmapAsJpeg.compress(Bitmap.CompressFormat.JPEG, Const.LOCAL.FILE.IMAGE.COMPRESS_QUALITY, out)
    }

    file
}

