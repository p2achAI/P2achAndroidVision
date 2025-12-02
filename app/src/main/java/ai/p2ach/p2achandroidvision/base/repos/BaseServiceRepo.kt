package ai.p2ach.p2achandroidvision.base.repos


import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseServiceRepo<S : Service>(
    private val appContext: Context,
    private val serviceClass: Class<S>
) : BaseLocalRepo<S, Nothing>() {

    private val _serviceState = MutableStateFlow<S?>(null)
    val serviceState = _serviceState.asStateFlow()

    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val service = binder?.let { getServiceFromBinder(it) } ?: return
            _serviceState.value = service
            isBound = true
            onServiceBound(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            val old = _serviceState.value
            _serviceState.value = null
            isBound = false
            onServiceUnbound(old)
        }
    }

    init {
        bindService()
    }

    protected fun bindService() {
        if (isBound) return
        val intent = Intent(appContext, serviceClass)
        appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbindService() {
        if (!isBound) return
        appContext.unbindService(connection)
        isBound = false
        _serviceState.value = null
    }

    protected abstract fun getServiceFromBinder(binder: IBinder): S

    protected open fun onServiceBound(service: S) {}
    protected open fun onServiceUnbound(old: S?) {}

    override fun localFlow(): Flow<S> = serviceState.filterNotNull()

    override suspend fun saveLocal(data: S) {}

    override suspend fun clearLocal() {}
}