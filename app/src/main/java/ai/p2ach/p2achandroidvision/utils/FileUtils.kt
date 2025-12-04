package ai.p2ach.p2achandroidvision.utils

import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent
import java.io.File

suspend fun Bitmap.saveBitmapAsJpeg(mdmEntity: MDMEntity?
): File = withContext(Dispatchers.IO) {


    val context = KoinJavaComponent.get<Context>(Context::class.java)

    val directory = File(context.filesDir, Const.LOCAL.FILE.IMAGE.CAPTURE_FILE_DIR).apply {
        if (!exists()) mkdirs()
    }

    val file = File(directory, generateTimestampFileName(mdmEntity?.deviceName?:"",
        Const.LOCAL.FILE.IMAGE.FORMAT ))

    file.outputStream().use { out ->
        this@saveBitmapAsJpeg.compress(Bitmap.CompressFormat.JPEG, Const.LOCAL.FILE.IMAGE.COMPRESS_QUALITY, out)
    }

    file
}

fun generateTimestampFileName(
    prefix: String = "",
    extension: String = "jpg"
): String {
    val now = System.currentTimeMillis()
    val zone = java.util.TimeZone.getDefault()
    val locale = java.util.Locale.getDefault()

    val sdf = java.text.SimpleDateFormat("yyyyMMddHHmmss", locale)
    sdf.timeZone = zone

    val timeText = sdf.format(java.util.Date(now))

    return if (prefix.isBlank()) {
        "${timeText}.$extension"
    } else {
        "${prefix}_${timeText}${Const.LOCAL.FILE.IMAGE.DOT}$extension"
    }
}