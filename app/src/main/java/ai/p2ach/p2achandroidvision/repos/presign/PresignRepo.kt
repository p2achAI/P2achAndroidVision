package ai.p2ach.p2achandroidvision.repos.presign

import ai.p2ach.p2achandroidvision.BuildConfig
import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.base.repos.BaseRepo
import ai.p2ach.p2achandroidvision.database.AppDataBase
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.io.File

data class PreSignRequest(
    val action: String,
    val path: String,
    val content_type: String
)

data class PreSignResponse(
    val url: String,
    val filename: String,
    val key: String
)

interface PreSignApi {

    @POST(BuildConfig.PRESIGN_PATH)
    suspend fun getPreSignUrl(
        @Body body: PreSignRequest,
        @Header(Const.REST_API.RETROFIT.HEADER.KEY.X_API_KEY) apiKey: String = BuildConfig.API_KEY
    ): PreSignResponse
}

class PreSignRepo() : BaseRepo<Unit, PreSignApi>(PreSignApi::class) {

    private val uploadClient by lazy { OkHttpClient() }

    override fun stream(): Flow<Unit> = emptyFlow()

    suspend fun requestUploadUrl(path: String, contentType: String): PreSignResponse? {
        val service = api ?: return null
        return service.getPreSignUrl(
            PreSignRequest(
                action = Const.REST_API.RETROFIT.PRE_SIGN.ACTION.UPLOAD,
                path = path,
                content_type = contentType
            )
        )
    }

    suspend fun uploadFileToPresignedUrl(url: String, file: File, contentType: String): Boolean {
        val body = file.asRequestBody(contentType.toMediaType())
        val request = Request.Builder()
            .url(url)
            .put(body)
            .addHeader(Const.REST_API.RETROFIT.HEADER.KEY.CONTENT_TYPE, contentType)
            .build()

        uploadClient.newCall(request).execute().use { response ->
            return response.isSuccessful
        }
    }

    suspend fun uploadCaptureReportImage(captureId: String, file: File, path:String): Boolean {

        val contentType = Const.REST_API.RETROFIT.CONTENT_TYPE.IMAGE_JPEG
        val path = "${Const.REST_API.RETROFIT.PATH.CAPTURE_REPORT_UPLOAD_PATH}$path/$captureId"
        val presign = requestUploadUrl(path, contentType) ?: return false
        return uploadFileToPresignedUrl(presign.url, file, contentType)
    }
}