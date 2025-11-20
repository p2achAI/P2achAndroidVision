package ai.p2ach.p2achandroidvision.repos.camera.handlers

import android.graphics.Bitmap
import android.view.SurfaceHolder


enum class CameraSourceType {
    RTSP,
    USB_UVC
}

fun interface FrameListener {
    fun onFrame(bitmap: Bitmap?)
}

interface CameraHandler {
    fun setSurface(holder: SurfaceHolder?)
    fun clearSurface(holder: SurfaceHolder?)
    fun start()
    fun stop()
}


