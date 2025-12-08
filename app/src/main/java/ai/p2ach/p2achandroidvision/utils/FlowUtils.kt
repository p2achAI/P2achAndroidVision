package ai.p2ach.p2achandroidvision.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


inline fun <T, R> Flow<T>.onChanged(
    crossinline selector: (T) -> R,
    crossinline block: suspend (root: T, selected: R) -> Unit
): Flow<T> = flow {
    var initialized = false
    var last: Any? = null

    this@onChanged.collect { root ->
        val selected = selector(root)

        if (!initialized || last != selected) {
            initialized = true
            last = selected
            block(root, selected)
        }

        emit(root)
    }
}