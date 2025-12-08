package ai.p2ach.p2achandroidvision.base.repos

import ai.p2ach.p2achandroidvision.BuildConfig
import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.utils.Log
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass


sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val throwable: Throwable) : ApiResult<Nothing>()
}

abstract class BaseRepo<T, API_SERVICE : Any>(
    apiClass: KClass<API_SERVICE>? = null,
) {
    abstract fun stream(): Flow<T>

    protected open fun provideHeaders(): Map<String, String> = emptyMap()

    protected val api: API_SERVICE? by lazy {
        apiClass?.let {
            createRetrofit(BuildConfig.API_URL).create(it.java)
        }
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(Const.REST_API.RETROFIT.TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(Const.REST_API.RETROFIT.TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(Const.REST_API.RETROFIT.TIME_OUT, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )

        val headers = provideHeaders()
        if (headers.isNotEmpty()) {
            clientBuilder.addInterceptor { chain ->
                val original = chain.request()

                Log.d("retrofit request url ${original.url}")


                val builder = original.newBuilder()
                headers.forEach { (k, v) -> builder.addHeader(k, v) }
                chain.proceed(builder.build())
            }
        }

        val client = clientBuilder.build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    protected suspend fun <R> safeApiCall(
        block: suspend API_SERVICE.() -> R
    ): ApiResult<R> {
        val service = api ?: return ApiResult.Error(IllegalStateException("Api not initialized"))
        return try {
            ApiResult.Success(service.block())
        }catch (e: Exception){
            ApiResult.Error(e)
        }}
}