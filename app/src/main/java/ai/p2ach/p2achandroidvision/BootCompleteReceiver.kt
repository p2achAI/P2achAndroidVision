package ai.p2ach.p2achandroidvision

import ai.p2ach.p2achandroidvision.utils.Log
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.java.KoinJavaComponent
import org.opencv.android.CameraActivity

class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        Log.d("BootCompleteReceiver ${intent?.action}")
        val context = KoinJavaComponent.get<Context>(Context::class.java)
        val mainIntent = Intent(context, CameraActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        context.startActivity(mainIntent)



    }
}