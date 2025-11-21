package ai.p2ach.p2achandroidvision.views.fragments
import ai.p2ach.p2achandroidlibrary.base.fragments.BaseFragment
import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.R
import ai.p2ach.p2achandroidvision.databinding.FragmentSplashBinding
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission



class FragmentSplash : BaseFragment<FragmentSplashBinding>() {



    override fun viewInit(savedInstanceState: Bundle?) {



        TedPermission.create()
            .setPermissionListener(object : PermissionListener{
                override fun onPermissionGranted() {

                    Log.d("onPermissionGranted")
                    navigate(R.id.nav_fragment_camera)
                }

                override fun onPermissionDenied(p0: List<String?>?) {
                    Log.d("onPermissionDenied ${p0}" )
                }
            })
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.POST_NOTIFICATIONS,
//                Manifest.permission.READ_MEDIA_IMAGES,
//                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.FOREGROUND_SERVICE,
//                Manifest.permission.FOREGROUND_SERVICE_CAMERA,
                )
            .check();

    }



}