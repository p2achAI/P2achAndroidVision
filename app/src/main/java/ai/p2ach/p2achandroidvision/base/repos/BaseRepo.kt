package ai.p2ach.p2achandroidvision.base.repos



import ai.p2ach.p2achandroidvision.BuildConfig
import ai.p2ach.p2achandroidvision.Const
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass


abstract class BaseRepo<T, API_SERVICE : Any>(apiClass : KClass<API_SERVICE>?=null,
){
    abstract fun stream(): Flow<T>

    protected val api: API_SERVICE? by lazy {
        apiClass?.let {
            createRetrofit(BuildConfig.API_URL).create(it.java)
        }
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        val client = OkHttpClient.Builder()
            .connectTimeout(Const.REST_API.RETROFIT.TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(Const.REST_API.RETROFIT.TIME_OUT, TimeUnit.SECONDS)
            .writeTimeout(Const.REST_API.RETROFIT.TIME_OUT, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


}