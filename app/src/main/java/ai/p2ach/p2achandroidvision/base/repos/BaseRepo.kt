package ai.p2ach.p2achandroidvision.base.repos



import kotlinx.coroutines.flow.Flow


abstract class BaseRepo<T>(){
    abstract fun stream(): Flow<T>
}