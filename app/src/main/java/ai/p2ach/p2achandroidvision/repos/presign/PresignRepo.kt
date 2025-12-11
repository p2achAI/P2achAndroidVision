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


//{
//    "presigned_url": "https://p2ach-android-apk.s3.amazonaws.com/models/rk3588/retinaface_mobile640.rknn?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=ASIAVX43SRVEWJULMIFJ%2F20251211%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Date=20251211T003928Z&X-Amz-Expires=900&X-Amz-SignedHeaders=host&X-Amz-Security-Token=IQoJb3JpZ2luX2VjEBkaDmFwLW5vcnRoZWFzdC0yIkcwRQIhAJzv%2BVW6jp%2FqLMFUJCEma3t5Xq3fMPGReo2C2Z%2FHeWseAiBB5oUu2LY%2FO74h0m%2BEjWG0s1lGnZNz%2F5rkgkvoN6Jx8SqRAwji%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F8BEAIaDDM5NDkyNjM5NDY5NyIMW0aroOi5CeMmsmneKuUCoIPO3xUgoGtVyi2QkFN3ML%2F3odD1OIDBiuedWJfXj3Sm6x016qsnoSMJTCVdy90zTTtR%2BQ2VcsKWqmWzG1XGN9fdmX46uAg7KUHH6zg8HHimOy1VeWCWayr%2Bn%2BWiUDH3hljODnWBOMmBxeGXqYJmJvZ8cpQkh5%2B1AHz%2FdXtSIw1BQ8fJnWLn6rvW%2BbXZ5H5zlAE0L0zKsRI9qgh1Or8CyDLy2nkt45Ijg4UmHWCw%2FLOtO8OVNqnmAbGxlus2V2lgnlLMBaH6lhjmdo3Dy4N9x1ZULKLXpThv0eGrbXSUFelY4WYMZb5G4KdAk9xUGEkopnf0YiwY7ax8Z5CL%2B%2B0rZR050o%2B4odrhphv58cyTHtFDy3m9j6i1S8buuBfMzwzhHLc7OIBK%2BfcuBwX62t0gVNKXygoyoNpA73LYwbdLR2jyt9kje22Ljl6HihdYVjHncFCqj3xi3bbUzzoH%2BHYnG9yWCh%2BBMIKl6MkGOp0BYaVJmFojP9rHokVkc%2Fy0qeYFRajDPqvDBhK0xElLLMKEwqaVL8%2BBhxukhMPix9rRisgAAXPb9L3nI6eU0Gj%2B6R%2FtdVUX2aFJUbfVR2M0hl6s2fIoF6yTZD6uR4BhHP%2B%2BOJucrYMO2bOKGqx2Yydy9rVGU6PSo9NRfcHll%2Bz3c6n4FkvyDZNrka98Jq9xVxM7LBc347tUayKPltFgJA%3D%3D&X-Amz-Signature=c6cc8becb9621bfbbef9c3842d5548f3a14f46a893e63b359594496bfcbd50bc",
//    "file_key": "models/rk3588/retinaface_mobile640.rknn",
//    "bucket": "p2ach-android-apk",
//    "expires_in": 900,
//    "file_size": 1263991,
//    "checksum": "d6b2f5126c0e1115aff5875a1e7f1006"
//}




interface PreSignApi {

    @POST(BuildConfig.PRESIGN_DISPLAY_REPORT)
    suspend fun getPreSignDisPlayReportUrl(
        @Body body: PreSignDisPlayReportRequest,
        @Header(Const.REST_API.RETROFIT.HEADER.KEY.X_API_KEY) apiKey: String = BuildConfig.API_KEY,
        @Header(Const.REST_API.RETROFIT.HEADER.KEY.CONTENT_TYPE) contentType: String = Const.REST_API.RETROFIT.CONTENT_TYPE.APPLICATION_JSON
    ): PreSignDisplayReportUrlResponse


    @POST(BuildConfig.PRESIGN_URL_GENERATOR)
    suspend fun getAiModelPreSignUrl(
        @Body body: PreSignAiModelRequest,
        @Header(Const.REST_API.RETROFIT.HEADER.KEY.X_API_KEY) apiKey: String = BuildConfig.API_KEY,
        @Header(Const.REST_API.RETROFIT.HEADER.KEY.CONTENT_TYPE) contentType: String = Const.REST_API.RETROFIT.CONTENT_TYPE.APPLICATION_JSON
    ): PreSignAiModelUrlResponse

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


    suspend fun requestAiModelDownLoadUrl(fileKey:String): PreSignAiModelUrlResponse? {
      return  request { getAiModelPreSignUrl(body = PreSignAiModelRequest(file_key = fileKey)) }
    }


    suspend fun downLoadAiModel(){

        val modelKeys  = DeviceUtils.getRequiredModelKeys()
        val modelUrls = modelKeys.associateWith {

            Log.d("downLoadAiModel $it")




        }






    }




}




