package ai.p2ach.p2achandroidvision.repos.camera.handlers

import ai.p2ach.p2achandroidvision.repos.camera.CaptureRepo
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.opencv.android.Utils
import org.opencv.core.Mat

sealed class CameraUiState {
    data object Idle : CameraUiState()
    data class Switching(val type: CameraType) : CameraUiState()
    data class Connecting(val type: CameraType) : CameraUiState()
    data class Connected(val type: CameraType) : CameraUiState()
    data class Stoped(val type: CameraType) : CameraUiState()
    data class Error(val type: CameraType, val message: String? = null) : CameraUiState()
}


enum class CameraType {
    UVC,
    INTERNAL,
    RTSP,
    NONE,
}

interface CameraHandler {
    fun startStreaming()
    fun stopStreaming()

    fun errorStreaming(msg:String)

    fun pause()
    fun resume()
}

interface CameraCallback {
    fun onFrameProcessed(bitmap: Bitmap?)
}

abstract class BaseCameraHandler(
    private val type: CameraType
) : CameraHandler, CameraCallback , KoinComponent {

    private val inputImg = Mat()
    private val resultImg = Mat()
    private val drawImg = Mat()
    protected var isStarted = false
    protected var isPaused = false

    private val captureRepo : CaptureRepo by inject()


    private var bckImg : Mat? = null
    var mdmEntity : MDMEntity? = null
    private var bitmapPool: MutableList<Bitmap> = mutableListOf()

    private val _frames = MutableSharedFlow<Bitmap>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val frames: SharedFlow<Bitmap> = _frames

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    protected fun emitFrame(bitmap: Bitmap?) {
        if (!isStarted || isPaused) return
        if (bitmap != null) _frames.tryEmit(bitmap)
    }


    protected fun setCameraState(state: CameraUiState) {
        _uiState.value = state
    }


    fun withMDM(mdmEntity: MDMEntity?): BaseCameraHandler{
        this.mdmEntity = mdmEntity
        return this
    }


    override fun startStreaming() {
//        setCameraState(CameraUiState.Connecting(type))
    }

    override fun stopStreaming() {
        setCameraState(CameraUiState.Stoped(type))
    }


    override fun errorStreaming(msg: String) {
        setCameraState(CameraUiState.Error(type,msg))
    }



    override fun pause() { isPaused = true }
    override fun resume() { isPaused = false }

    override fun onFrameProcessed(bitmap: Bitmap?) {
        emitFrame(bitmap)

        CoroutineScope(Dispatchers.IO).launch {
            captureRepo.writeToLocal(bitmap)
        }


    }



    object ByteArrayPool {
        private val pool = mutableListOf<ByteArray>()
        private const val MAX_POOL_SIZE = 100

        // size 크기 이상의 ByteArray를 반환, 없으면 새로 생성
        @Synchronized
        fun getByteArray(size: Int): ByteArray {
            return pool.find { it.size >= size }?.also {
                pool.remove(it)
            } ?: ByteArray(size)
        }

        // 사용한 ByteArray를 다시 풀에 반환하여 재사용 가능하게 함
        @Synchronized
        fun release(byteArray: ByteArray) {
            if(pool.size < MAX_POOL_SIZE) {
                pool.add(byteArray)
            }
        }
    }


    private fun getReusableBitmap(width: Int, height: Int, config: Bitmap.Config): Bitmap? {
        return bitmapPool.find {
            it.width == width && it.height == height && it.config == config
        }?.also {
            bitmapPool.remove(it)
        }
    }


    protected fun processImage(srcImg : Mat, cameraId: String?) {

        srcImg.copyTo(inputImg)    // rotation 0 => no rotation

        inputImg.copyTo(resultImg)

        val resultBitmap =
            getReusableBitmap(inputImg.cols(), inputImg.rows(), Bitmap.Config.RGB_565)
                ?: createBitmap(
                    inputImg.cols(),
                    inputImg.rows(),
                    Bitmap.Config.RGB_565
                )

        resultBitmap.eraseColor(0)
        Utils.matToBitmap(inputImg, resultBitmap)

//        Log.d("processImage")
        onFrameProcessed(resultBitmap)
    }

    abstract fun applyAutoExposure(enabled: Boolean)
    abstract fun applyManualExposure(exposure: Int)
    abstract fun getAutoExposureMode() : Boolean
    abstract fun getExposure() : Int


}