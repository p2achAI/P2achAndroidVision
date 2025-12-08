package ai.p2ach.p2achandroidvision.repos.receivers

import ai.p2ach.p2achandroidvision.repos.camera.CaptureReportRepo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.java.KoinJavaComponent

class TimeChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {

            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                val repo = KoinJavaComponent.get<CaptureReportRepo>(CaptureReportRepo::class.java)
//                repo.rescheduleAll()
            }
        }
    }
}