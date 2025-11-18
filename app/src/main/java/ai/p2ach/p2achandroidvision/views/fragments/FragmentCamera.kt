package ai.p2ach.p2achandroidvision.views.fragments

import ai.p2ach.p2achandroidlibrary.base.fragments.BaseFragment
import ai.p2ach.p2achandroidlibrary.base.service.ServiceBoundListener
import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.databinding.FragmentCameraBinding
import ai.p2ach.p2achandroidvision.service.CameraService
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class FragmentCamera : BaseFragment<FragmentCameraBinding>(), ServiceBoundListener<CameraService>{

    val cameraManager : P2achCameraManager by inject()


    override fun onServiceBound(service: CameraService) {
        Log.d("onServiceBound $service")
        
    }

    override fun onServiceUnbound() {

    }


    override fun viewInit(savedInstanceState: Bundle?) {
        super.viewInit(savedInstanceState)

        autoBinding {

            viewLifecycleOwner.lifecycleScope.launch {

                    tvCamera.text = cameraManager.detectCameraType().name

                }
            }
    }
}



