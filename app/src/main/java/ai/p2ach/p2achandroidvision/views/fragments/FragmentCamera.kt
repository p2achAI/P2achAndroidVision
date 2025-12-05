package ai.p2ach.p2achandroidvision.views.fragments





import ai.p2ach.p2achandroidvision.utils.Log
import ai.p2ach.p2achandroidvision.base.fragments.BaseFragment
import ai.p2ach.p2achandroidvision.databinding.FragmentCameraBinding


import ai.p2ach.p2achandroidvision.utils.getCameraStatusMessage
import ai.p2ach.p2achandroidvision.utils.toCameraType
import ai.p2ach.p2achandroidvision.utils.toDisplayName
import ai.p2ach.p2achandroidvision.viewmodels.CameraViewModel
import ai.p2ach.p2achandroidvision.views.common.StatusLightView

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import androidx.core.view.doOnLayout
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentCamera : BaseFragment<FragmentCameraBinding>() {

    private val cameraViewModel: CameraViewModel by viewModel()

    private var surfaceReady = false
    private var inputWidth = 0f
    private var inputHeight = 0f

    override fun viewInit(savedInstanceState: Bundle?) {
        super.viewInit(savedInstanceState)

        autoBinding {


            preview.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    binding.preview.doOnLayout {
                        surfaceReady = true
                    }
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    surfaceReady = false
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {


                }
            })

            viewLifecycleOwner.lifecycleScope.launch {
                cameraViewModel.frames.collect { bmp ->
                    drawUsingCanvas(bmp)
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                cameraViewModel.uiState.distinctUntilChanged().filterNotNull().collect { cameraUiState ->
                    Log.d("cameraUiState $cameraUiState")

                    val message = getCameraStatusMessage(cameraUiState)
                    if (message.isEmpty()) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            clProgress.visibility = View.GONE
                            preview.visibility = View.VISIBLE
                            slvStatus.state = StatusLightView.State.CONNECTED
                        }, 1000)

                    } else {
                        clProgress.visibility = View.VISIBLE
                        preview.visibility = View.GONE
                        tvProgress.text = message
                        slvStatus.state = StatusLightView.State.DISCONNECTED

                    }

                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                cameraViewModel.mdmFlow.distinctUntilChanged().collect {
                    mdm->
                    tvCameraType.text  = mdm.toCameraType().toDisplayName()
                }
            }

        }
    }

    private fun drawUsingCanvas(bitmap: Bitmap) {
        if (!surfaceReady) return
//        Log.d("drawUsingCanvas")
        val holder = binding.preview.holder
        val canvas: Canvas? = try {
            holder.lockCanvas()
        } catch (_: Throwable) {
            null
        }
        if (canvas == null) return

        val canvasWidth = canvas.width.toFloat()
        val canvasHeight = canvas.height.toFloat()
        inputWidth = bitmap.width.toFloat()
        inputHeight = bitmap.height.toFloat()

        var rotationDeg = 0


//        val rotationDeg = if (cameraViewModel.autoRotationEnabled()) {
//            val uiRotation = getUiRotationDegrees()
//            val streamRotation = cameraViewModel.getStreamRotationDegrees()
//            ((streamRotation - uiRotation) % 360 + 360) % 360
//        } else {
//            0
//        }

        val rotatedW = if (rotationDeg % 180 == 0) inputWidth else inputHeight
        val rotatedH = if (rotationDeg % 180 == 0) inputHeight else inputWidth

        val scale = minOf(canvasWidth / rotatedW, canvasHeight / rotatedH)
        val dx = (canvasWidth - rotatedW * scale) / 2f
        val dy = (canvasHeight - rotatedH * scale) / 2f

        val matrix = Matrix().apply {
            postRotate(rotationDeg.toFloat(), inputWidth / 2f, inputHeight / 2f)
            postScale(scale, scale)
            postTranslate(dx, dy)
        }

        canvas.drawColor(Color.BLACK)
        canvas.drawBitmap(bitmap, matrix, null)

        try {
            holder.unlockCanvasAndPost(canvas)
        } catch (t: Throwable) {
            Log.d("${t.toString()}")
        }
    }

    private fun getUiRotationDegrees(): Int {
        val rotation: Int = requireActivity().display?.rotation ?: Surface.ROTATION_0
        return when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
    }


}


