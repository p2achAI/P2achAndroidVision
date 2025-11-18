package ai.p2ach.p2achandroidvision.views.activities

import BaseServiceBindingActivity
import ai.p2ach.p2achandroidlibrary.base.activites.BaseActivity
import ai.p2ach.p2achandroidlibrary.base.activites.BaseNavigationActivity
import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.R
import ai.p2ach.p2achandroidvision.databinding.ActivityMainBinding
import ai.p2ach.p2achandroidvision.service.CameraService
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import com.serenegiant.usb.UVCCamera

class ActivityMain : BaseServiceBindingActivity<ActivityMainBinding , CameraService>(){

    override var navHostViewId: Int = R.id.activity_main_nav_host
    override fun createBindIntent(): Intent = Intent(this, CameraService::class.java )


    override fun viewInit(savedInstanceState: Bundle?) {
        super.viewInit(savedInstanceState)
//        Log.d("MainActivity onCreate. ${UVCCamera.CTRL_AE_ABS}")

    }

}