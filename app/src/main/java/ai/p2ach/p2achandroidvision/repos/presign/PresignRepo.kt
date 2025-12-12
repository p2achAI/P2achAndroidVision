package ai.p2ach.p2achandroidvision.repos.presign

import ai.p2ach.p2achandroidvision.BuildConfig
import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.base.repos.BaseRepo
import ai.p2ach.p2achandroidvision.utils.DeviceUtils
import ai.p2ach.p2achandroidvision.utils.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.io.File

data class PreSignDisPlayReportRequest(
    val action: String,
    val path: String,
    val content_type: String
)

data class PreSignDisplayReportUrlResponse(
    val url: String,
    val filename: String,
    val key: String
)


data class PreSignAiModelRequest(
    val file_key: String
)


data class PreSignAiModelUrlResponse(
    val presigned_url: String,
    val file_key: String,
    val bucket: String,
    val expires_in : Long,
    val file_size : Long,
    val checksum : String,
)


interface PreSignApi {

    @POST(BuildConfig.PRESIGN_DISPLAY_REPORT)
    suspend fun getPreSignDisPlayReportUrl(
        @Body body: PreSignDisPlayReportRequest,
        @Header(Const.REST_API.RETROFIT.HEADER.KEY.X_API_KEY) apiKey: String = BuildConfig.API_KEY,
        @Header(Const.REST_API.RETROFIT.HEADER.KEY.CONTENT_TYPE) contentType: String = Const.REST_API.RETROFIT.CONTENT_TYPE.APPLICATION_JSON
    ): Response<PreSignDisplayReportUrlResponse>


    @POST(BuildConfig.PRESIGN_URL_GENERATOR)
    suspend fun getAiModelPreSignUrl(
        @Body body: PreSignAiModelRequest,
        @Header(Const.REST_API.RETROFIT.HEADER.KEY.X_API_KEY) apiKey: String = BuildConfig.API_KEY,
        @Header(Const.REST_API.RETROFIT.HEADER.KEY.CONTENT_TYPE) contentType: String = Const.REST_API.RETROFIT.CONTENT_TYPE.APPLICATION_JSON
    ): Response<PreSignAiModelUrlResponse>

}

class PreSignRepo() : BaseRepo<Unit, PreSignApi>(PreSignApi::class) {

    private val uploadClient by lazy { OkHttpClient() }

    override fun stream(): Flow<Unit> = emptyFlow()

    suspend fun requestUploadUrl(path: String, contentType: String): PreSignDisplayReportUrlResponse? {
        return request{ getPreSignDisPlayReportUrl(
            PreSignDisPlayReportRequest(
                action = Const.REST_API.RETROFIT.PRE_SIGN.ACTION.UPLOAD,
                path = path,
                content_type = contentType
            )
        )}

    }


    private suspend fun uploadFileToPresignedUrl(url: String, file: File, contentType: String, where : String): Boolean {
        val body = file.asRequestBody(contentType.toMediaType())
        val request = Request.Builder()
            .url(url)
            .put(body)
            .addHeader(Const.REST_API.RETROFIT.HEADER.KEY.CONTENT_TYPE, contentType)
            .build()

        uploadClient.newCall(request).execute().use { response ->
            Log.d("$where $response")
            return response.isSuccessful
        }
    }

    suspend fun uploadCaptureReportImage(captureId: String, file: File, path:String): Boolean {

        val contentType = Const.REST_API.RETROFIT.CONTENT_TYPE.IMAGE_JPEG
        val path = "${Const.REST_API.RETROFIT.PATH.CAPTURE_REPORT_UPLOAD_PATH}$path/$captureId"
        val upLoadPresign = requestUploadUrl(path, contentType) ?: return false
        Log.d("uploadCaptureReport presign url ${upLoadPresign.url}")
        return uploadFileToPresignedUrl(upLoadPresign.url, file, contentType,"CaptureReport")
    }


    suspend fun requestAiModelDownLoadUrl(fileKey : String): PreSignAiModelUrlResponse? {
      return  request { getAiModelPreSignUrl(body = PreSignAiModelRequest(file_key = fileKey)) }
    }






}




