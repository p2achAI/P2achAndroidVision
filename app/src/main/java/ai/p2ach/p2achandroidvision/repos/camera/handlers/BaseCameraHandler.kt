package ai.p2ach.p2achandroidvision.repos.camera.handlers

import ai.p2ach.p2achandroidvision.repos.mdm.CamParam
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity
import ai.p2ach.p2achandroidvision.utils.toSdk
import ai.p2ach.p2achandroidvision.utils.toVisionSdk
import ai.p2ach.vision.sdk.NativeLib
import ai.p2ach.vision.sdk.Renderer
import ai.p2ach.vision.sdk.datatypes.AnalysisResult
import ai.p2ach.vision.sdk.datatypes.ROI
import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent
import org.opencv.android.Utils
import org.opencv.core.Mat

sealed class CameraUiState {
    data object Idle : CameraUiState()
    data class Switching(val type: CameraType) : CameraUiState()
    data class Connecting(val type: CameraType) : CameraUiState()
    data class Connected(val type: CameraType) : CameraUiState()
    data class Disconnected(val type: CameraType) : CameraUiState()
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

    fun onConnected()
    fun onDisconnected()

    fun pause()
    fun resume()
}


interface CameraInfo{

    fun getCameraId(): String
    fun getCameraVId():String
    fun getCameraPId(): String

    fun getCameraStatus():String
    fun getCameraStatusLog():String
    fun getCameraResolution() : String



}

interface CameraCallback {
    fun onFrameProcessed(bitmap: Bitmap?)
//    fun onAnalysisResult(analysisResult: AnalysisResult?, cameraId:String?)
}

abstract class BaseCameraHandler(
    private val type: CameraType
) : CameraHandler, CameraCallback , KoinComponent {

    private val inputImg = Mat()
    private val resultImg = Mat()
    private val drawImg = Mat()
    private var lastFrameAt: Long = 0L
    private var totalFrameCount: Long = 0L
    protected var isStarted = false
    protected var isPaused = false


    private var bckImg : Mat? = null

    private var renderer = Renderer(KoinJavaComponent.get<Context>(Context::class.java),CamParam().toSdk())


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


    override fun onConnected() {
        setCameraState(CameraUiState.Connected(type))
    }

    override fun onDisconnected() {
        setCameraState(CameraUiState.Disconnected(type))
    }







    override fun pause() { isPaused = true }
    override fun resume() { isPaused = false }

    override fun onFrameProcessed(bitmap: Bitmap?) {
        emitFrame(bitmap)
        lastFrameAt = SystemClock.elapsedRealtime()
        totalFrameCount += 1

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


        val rotation = 0
        val analysisResult : AnalysisResult?

        val inputImg = renderer.prepareInput(srcImg, rotation)

        val resultBitmap = getReusableBitmap(inputImg.cols(), inputImg.rows(), Bitmap.Config.RGB_565) ?: createBitmap(
            inputImg.cols(),
            inputImg.rows(),
            Bitmap.Config.RGB_565
        )
        inputImg.copyTo(resultImg)
        resultBitmap.eraseColor(0)


        NativeLib.processorStatus = NativeLib.ProcessorStatus.Running


        analysisResult = if(inputImg.cols() > 0 && inputImg.height() > 0) {
            NativeLib.process(inputImg.nativeObjAddr, mdmEntity?.roi?.toVisionSdk()?: ROI())
        } else {
            null
        }

//        onAnalysisResult(analysisResult, cameraId)
        NativeLib.processorStatus = NativeLib.ProcessorStatus.Initialized


        Utils.matToBitmap(inputImg, resultBitmap)

//        Log.d("processImage")
        onFrameProcessed(resultBitmap)
    }

    abstract fun applyAutoExposure(enabled: Boolean)
    abstract fun applyManualExposure(exposure: Int)
    abstract fun getAutoExposureMode() : Boolean
    abstract fun getExposure() : Int




    fun getCameraHealth() : Boolean =uiState.value.toStatus()
    fun getCameraRotation(): Int = mdmEntity?.rotation?:0
    fun getCameraFlip(): Boolean = mdmEntity?.featureFlags?.flip?:false
    fun getCameraLastFrameTs(): Long = lastFrameAt
    fun getCameraProcessedFrameCount() : Long = totalFrameCount


    fun CameraUiState.toStatus(): Boolean {
        return when (this) {
            is CameraUiState.Connected -> true
            else -> false
        }
    }





}