package ai.p2ach.p2achandroidvision.base.repos

import ai.p2ach.p2achandroidvision.BuildConfig
import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.utils.Log
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass


//sealed class ApiResult<out T> {
//    data class Success<T>(val data: T) : ApiResult<T>()
//    data class Error(val throwable: Throwable) : ApiResult<Nothing>()
//}

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

    protected suspend fun <R> request(
        onError: ((Throwable) -> Unit)? = null,
        block: suspend API_SERVICE.() -> retrofit2.Response<R>
    ): R? {
        val service = api ?: run {
            val e = IllegalStateException("Api not initialized")
            Log.d("retrofit error api not initialized")
            onError?.invoke(e)
            return null
        }

        return try {
            val response = block.invoke(service)

            val raw = response.raw()
            val req = raw.request
            val url = req.url
            val method = req.method

            if (!response.isSuccessful) {
                val errorBody = runCatching { response.errorBody()?.string() }.getOrNull()
                val serverMessage = errorBody?.let { extractServerErrorMessage(it) }

                Log.e(
                    "retrofit error $method $url code=${response.code()} " +
                            (serverMessage ?: response.message())
                )

                onError?.invoke(retrofit2.HttpException(response))
                null
            } else {
                Log.d("retrofit success $method $url")
                response.body()
            }
        } catch (e: Throwable) {
            Log.d("retrofit exception ${e::class.java.simpleName} ${e.message}")
            onError?.invoke(e)
            null
        }
    }


    private fun extractServerErrorMessage(raw: String): String? {
        val t = raw.trim()
        if (t.isBlank()) return null

        if (t.startsWith("{") && t.endsWith("}")) {
            return runCatching {
                val obj = org.json.JSONObject(t)
                when {
                    obj.has("error") -> obj.optString("error")
                    obj.has("message") -> obj.optString("message")
                    obj.has("detail") -> obj.optString("detail")
                    else -> null
                }?.takeIf { it.isNotBlank() }
            }.getOrNull()
        }

        if (t.startsWith("<")) {
            return t.replace(Regex("<[^>]*>"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
                .takeIf { it.isNotBlank() }
        }

        return t
    }
}