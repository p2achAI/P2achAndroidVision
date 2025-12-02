package ai.p2ach.p2achandroidvision.repos.presign

import ai.p2ach.p2achandroidvision.base.repos.BaseRepo
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

    @POST("display-report-presign-prod")
    suspend fun getPreSignUrl(
        @Body body: PreSignRequest,
        @Header("x-api-key") apiKey: String = "WbEl2exBfiaYj5ew7eUQO2r94Jq1MTNXy58TXbcc"
    ): PreSignResponse
}

class PreSignRepo : BaseRepo<Unit, PreSignApi>(PreSignApi::class) {

    private val uploadClient by lazy { OkHttpClient() }

    override fun stream(): Flow<Unit> = emptyFlow()

    suspend fun requestUploadUrl(path: String, contentType: String): PreSignResponse? {
        val service = api ?: return null
        return service.getPreSignUrl(
            PreSignRequest(
                action = "upload",
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
            .addHeader("Content-Type", contentType)
            .build()

        uploadClient.newCall(request).execute().use { response ->
            return response.isSuccessful
        }
    }

    suspend fun uploadDisplayReportImage(captureId: String, file: File): Boolean {
        val contentType = "image/jpeg"
        val path = "display_report/images/$captureId"
        val presign = requestUploadUrl(path, contentType) ?: return false
        return uploadFileToPresignedUrl(presign.url, file, contentType)
    }
}