package ai.p2ach.p2achandroidvision.base.activites



import ai.p2ach.p2achandroidvision.utils.Log
import android.net.Uri
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding
/*
*Navigation으로 Fragment 이동할 수 있는 Activity
* */

abstract class BaseNavigationActivity<VB : ViewBinding> : BaseActivity<VB>() {

    protected open var navHostViewId : Int =-1

    private val navController by lazy {
        if (navHostViewId == -1) {
            error("navHostViewId must be set before using navigate()")
        }
        (supportFragmentManager.findFragmentById(navHostViewId) as NavHostFragment).navController
    }

    fun navigate(
        @IdRes destinationId: Int,
        args: Bundle? = null,
        navOptions: NavOptions? = null
    ) {
        navController.navigate(destinationId, args, navOptions)
    }

    fun navigateDeepLink(deepLink: Uri) {
        navController.navigate(deepLink)
    }

    fun popBack() {
        navController.popBackStack()
    }

    fun popBackTo(@IdRes destinationId: Int, inclusive: Boolean = false) {
        navController.popBackStack(destinationId, inclusive)
    }

}