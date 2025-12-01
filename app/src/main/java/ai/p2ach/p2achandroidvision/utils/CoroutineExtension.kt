package ai.p2ach.p2achandroidvision.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


object CoroutineExtension{

    fun launch(dispatcher: CoroutineDispatcher = Dispatchers.IO, block: suspend  CoroutineScope.()->Unit) : Job{

       return CoroutineScope(dispatcher).launch(block = block)
    }

    fun <T> async(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        block: () -> T
    ): Deferred<T> {
        return CoroutineScope(dispatcher).async {
            block.invoke()
        }
    }
}
