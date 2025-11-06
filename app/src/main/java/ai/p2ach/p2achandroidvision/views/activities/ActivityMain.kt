package ai.p2ach.p2achandroidvision.views.activities

import ai.p2ach.p2achandroidlibrary.base.activites.BaseActivity
import ai.p2ach.p2achandroidlibrary.base.activites.BaseNavigationActivity
import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.R
import ai.p2ach.p2achandroidvision.databinding.ActivityMainBinding
import android.os.Bundle
import com.serenegiant.usb.UVCCamera

class ActivityMain : BaseNavigationActivity<ActivityMainBinding>(){

    override var navHostViewId: Int = R.id.activity_main_nav_host


    override fun viewInit(savedInstanceState: Bundle?) {
        super.viewInit(savedInstanceState)
        Log.d("MainActivity onCreate. ${UVCCamera.CTRL_AE_ABS}")

    }

}