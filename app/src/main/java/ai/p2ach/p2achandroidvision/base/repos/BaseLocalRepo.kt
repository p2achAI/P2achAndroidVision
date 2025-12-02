package ai.p2ach.p2achandroidvision.base.repos

import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

abstract class BaseLocalRepo<T,API_SERVICE: Any>(
    apiClass : KClass<API_SERVICE>?=null
) : BaseRepo<T,API_SERVICE>(apiClass) {

    abstract fun localFlow(): Flow<T>
    abstract suspend fun saveLocal(data: T)
    abstract suspend fun clearLocal()
    override fun stream(): Flow<T> =localFlow()
}