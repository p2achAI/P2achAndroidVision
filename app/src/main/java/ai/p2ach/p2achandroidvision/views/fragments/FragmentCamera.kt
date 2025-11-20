package ai.p2ach.p2achandroidvision.views.fragments

import ai.p2ach.p2achandroidlibrary.base.fragments.BaseFragment
import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.databinding.FragmentCameraBinding
import ai.p2ach.p2achandroidvision.utils.toUiItems
import ai.p2ach.p2achandroidvision.viewmodels.CameraViewModel
import android.graphics.PixelFormat
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class FragmentCamera : BaseFragment<FragmentCameraBinding>(){

    val cameraViewModel : CameraViewModel by viewModel()

    override val enableUsbEvents: Boolean = true

    override fun viewInit(savedInstanceState: Bundle?) {
        super.viewInit(savedInstanceState)

        autoBinding {
            // 서비스 바인딩 상태 관찰
            viewLifecycleOwner.lifecycleScope.launch {
                cameraViewModel.data.collect { cameraService ->
                    Log.d("FragmentCamera", "service bound: $cameraService")
                    // 서비스가 붙은 뒤에만 startUsbPreview 호출해도 됨
                }
            }

            surfaceView.setZOrderOnTop(true)
            surfaceView.holder.setFormat(PixelFormat.TRANSLUCENT)

            surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    Log.d("UVC surfaceCreated")
                    holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

                    holder.setFixedSize(640, 480)
                    cameraViewModel.attachPreview(holder)
                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                    Log.d("UVC surfaceChanged width=$width height=$height")
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    Log.d("UVC surfaceDestroyed")
                    cameraViewModel.detachPreview(holder)
                }
            })
        }
    }

    override fun onReadyUsbCamera(device: UsbDevice) {
        super.onReadyUsbCamera(device)

        autoBinding {
            cameraViewModel.startUsbPreview()
        }
    }

    override fun onPause() {
        super.onPause()
        cameraViewModel.stopPreview()
    }

}



