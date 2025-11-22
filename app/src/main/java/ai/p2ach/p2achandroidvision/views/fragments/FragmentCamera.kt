package ai.p2ach.p2achandroidvision.views.fragments

import ai.p2ach.p2achandroidlibrary.base.fragments.BaseFragment
import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.Const
import ai.p2ach.p2achandroidvision.databinding.FragmentCameraBinding
import ai.p2ach.p2achandroidvision.viewmodels.CameraViewModel
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FragmentCamera : BaseFragment<FragmentCameraBinding>() {

    val cameraViewModel: CameraViewModel by viewModel()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun viewInit(savedInstanceState: Bundle?) {
        super.viewInit(savedInstanceState)

        val cameraType = arguments?.getSerializable(Const.BUNDLE.KEY.CAMERA_TYPE, P2achCameraManager.CameraType::class.java)

        Log.d("cameraType ${cameraType?.name}")

        autoBinding {
            viewLifecycleOwner.lifecycleScope.launch {
                cameraViewModel.data.collect { cameraService ->
                    if(cameraService == null) return@collect
                    Log.d("FragmentCamera", "service bound: $cameraService")
                }
            }

            surfaceView.setZOrderOnTop(true)
            surfaceView.holder.setFormat(PixelFormat.TRANSLUCENT)

            surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    Log.d("UVC surfaceCreated")
                    holder.setFixedSize(640, 480)
                    cameraViewModel.attachPreview(holder)
                    cameraViewModel.startUsbPreview()
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                    Log.d("UVC surfaceChanged width=$width height=$height")
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    Log.d("UVC surfaceDestroyed")
                    cameraViewModel.detachPreview(holder)
                }


            })
        }
    }

    override fun onPause() {
        super.onPause()
//        cameraViewModel.stopPreview()
    }
}