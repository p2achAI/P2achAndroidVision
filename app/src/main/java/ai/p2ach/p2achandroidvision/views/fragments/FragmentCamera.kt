package ai.p2ach.p2achandroidvision.views.fragments

import ai.p2ach.p2achandroidlibrary.base.fragments.BaseFragment
import ai.p2ach.p2achandroidvision.databinding.FragmentCameraBinding
import ai.p2ach.p2achandroidvision.utils.toUiItems
import ai.p2ach.p2achandroidvision.viewmodels.CameraViewModel
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class FragmentCamera : BaseFragment<FragmentCameraBinding>(){

    val cameraViewModel : CameraViewModel by viewModel()

    override fun viewInit(savedInstanceState: Bundle?) {
        super.viewInit(savedInstanceState)

        autoBinding {


            viewLifecycleOwner.lifecycleScope.launch {
                cameraViewModel.data.collect {
                        cameraService ->

                        tvCamera.text = cameraService?.cameraManager?.detectCameraType()?.name?:"service not connect"

                }
            }



        }
    }
}



