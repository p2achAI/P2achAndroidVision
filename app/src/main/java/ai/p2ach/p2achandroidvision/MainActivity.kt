package ai.p2ach.p2achandroidvision



import ai.p2ach.p2achandroidlibrary.base.activites.BaseActivity
import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.databinding.ActivityMainBinding

import android.os.Bundle
import com.serenegiant.usb.UVCCamera


class MainActivity : BaseActivity<ActivityMainBinding>(){

    override fun viewInit(savedInstanceState: Bundle?) {
        super.viewInit(savedInstanceState)
        Log.d("MainActivity onCreate. ${UVCCamera.CTRL_AE_ABS}")


    }

}
