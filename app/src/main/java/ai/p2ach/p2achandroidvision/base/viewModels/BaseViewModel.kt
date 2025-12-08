package ai.p2ach.p2achandroidvision.base.viewmodel

import ai.p2ach.p2achandroidvision.base.repos.BaseRepo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

abstract class BaseViewModel<T, R: BaseRepo<T, *>>(
   protected val repo :R
) : ViewModel() {

    init {
        collectRepoFlow(repo.stream())
    }


    protected val _data = MutableStateFlow<T?>(null)
    val data: StateFlow<T?> = _data

    protected val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    protected val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error

    protected fun launchIO(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.value = true
                block()
            } catch (t: Throwable) {
                _error.value = t
            } finally {
                _loading.value = false
            }
        }
    }

    protected fun <T> collectRepoFlow(
        flow: Flow<T>,
        state: MutableStateFlow<T?>? = null,
        onSuccess: ((T) -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null
    ) {
        viewModelScope.launch {
            flow
                .catch { e ->
                    state?.value = null
                    onError?.invoke(e)
                }
                .collect { data ->
                    state?.value = data
                    onSuccess?.invoke(data)
                }
        }
    }
}