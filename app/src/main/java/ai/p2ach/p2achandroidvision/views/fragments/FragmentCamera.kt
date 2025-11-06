package ai.p2ach.p2achandroidvision.views.fragments

import ai.p2ach.p2achandroidlibrary.base.fragments.BaseFragment
import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.BuildConfig
import ai.p2ach.p2achandroidvision.databinding.FragmentCameraBinding
import android.os.Bundle
import com.hmdm.MDMService


class FragmentCamera : BaseFragment<FragmentCameraBinding>(){

    override fun viewInit(savedInstanceState: Bundle?) {
        super.viewInit(savedInstanceState)
//        MDMService.getInstance().queryConfig(BuildConfig.MDM_API_KEY)

        MDMService.getInstance().connect(context?.applicationContext, object : MDMService.ResultHandler {
            override fun onMDMConnected() {
                Log.d("mdm service connected")
            }

            override fun onMDMDisconnected() {
                // Log or retry if needed
            }
        })

    }
}