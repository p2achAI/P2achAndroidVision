package ai.p2ach.p2achandroidvision.views.fragments

import ai.p2ach.p2achandroidlibrary.base.fragments.BaseFragment
import ai.p2ach.p2achandroidvision.databinding.FragmentCameraBinding
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class FragmentCamera : BaseFragment<FragmentCameraBinding>() {

    val cameraManager : P2achCameraManager by inject()

    override fun viewInit(savedInstanceState: Bundle?) {
        super.viewInit(savedInstanceState)

        autoBinding {

            viewLifecycleOwner.lifecycleScope.launch {

                    tvCamera.text = cameraManager.detectCameraType().name

                }
            }
    }
}



