package ai.p2ach.p2achandroidvision.repos.receivers



import ai.p2ach.p2achandroidvision.repos.camera.CameraService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class TimeChangeReceiver : BroadcastReceiver() {



    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {

            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {


                context?.startService(
                    Intent(context, CameraService::class.java).apply {
                        action = intent.action
                    }
                )

            }
        }
    }
}