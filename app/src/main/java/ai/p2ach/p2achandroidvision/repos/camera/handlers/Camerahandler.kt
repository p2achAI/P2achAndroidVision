package ai.p2ach.p2achandroidvision.repos.camera.handlers

import android.graphics.Bitmap
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

enum class CameraType {
    UVC,
    INTERNAL,
    RTSP,
    NONE,
}

interface CameraHandler {
    fun startStreaming()
    fun stopStreaming()
    fun pause()
    fun resume()
}

interface CameraCallback {
    fun onFrameProcessed(bitmap: Bitmap?)
}

abstract class BaseCameraHandler(
    private val type: CameraType
) : CameraHandler, CameraCallback {

    protected var isPaused = false
    protected var isStarted = false

    private val _frames = MutableSharedFlow<Bitmap>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val frames: SharedFlow<Bitmap> = _frames

    protected fun emitFrame(bitmap: Bitmap?) {
        if (!isStarted || isPaused) return
        if (bitmap != null) _frames.tryEmit(bitmap)
    }

    override fun pause() { isPaused = true }
    override fun resume() { isPaused = false }

    override fun onFrameProcessed(bitmap: Bitmap?) {
        emitFrame(bitmap)
    }
}