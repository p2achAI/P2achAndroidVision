package ai.p2ach.p2achandroidvision.base.repos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

abstract class BaseNetworkRepo<T,API_SERVICE: Any>(
     apiClass : KClass<API_SERVICE>?=null)
    : BaseLocalRepo<T,API_SERVICE>(apiClass) {

    abstract suspend fun fetchRemote(): T?
    override fun stream(): Flow<T> {
        return localFlow().onStart {
            val remote = withContext(Dispatchers.IO) { fetchRemote() }
            if (remote != null) saveLocal(remote)
        }
    }
}