package ai.p2ach.p2achandroidvision.camera

import ai.p2ach.p2achandroidvision.repos.camera.handlers.CameraHandler
import android.content.Context
import android.media.MediaPlayer
import android.view.SurfaceHolder

class RtspCameraHandler(
    private val context: Context,
    private val url: String
) : CameraHandler {

    private var surface: SurfaceHolder? = null
    private var player: MediaPlayer? = null

    override fun setSurface(holder: SurfaceHolder?) {
        surface = holder
        player?.setDisplay(holder)
    }

    override fun clearSurface(holder: SurfaceHolder?) {
        if (surface == holder) surface = null
        player?.setDisplay(null)
    }

    override fun start() {
        val holder = surface ?: return

        player = MediaPlayer().apply {
            setDataSource(url)
            setDisplay(holder)
            setOnPreparedListener { it.start() }
            setOnErrorListener { _, what, extra ->
//                Log.e("RtspCameraHandler error what=$what extra=$extra")
                true
            }
            prepareAsync()
        }
    }

    override fun stop() {
        player?.stop()
        player?.release()
        player = null
    }
}
