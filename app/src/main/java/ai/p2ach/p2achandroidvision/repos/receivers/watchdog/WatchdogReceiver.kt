package ai.p2ach.p2achandroidvision.repos.receivers.watchdog


import ai.p2ach.p2achandroidlibrary.utils.Log
import ai.p2ach.p2achandroidvision.views.activities.ActivityMain
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WatchdogReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        WatchdogScheduler.scheduleNext(context)


        if (!isMainProcessAlive(context)) {

//            Log.d("WatchdogReceiver Main is destroyed. restart..")
//
//            val i = Intent(context, ActivityMain::class.java).apply {
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//            }
//            context.startActivity(i)
        }
    }

    private fun isMainProcessAlive(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pkg = context.packageName
        val procs = am.runningAppProcesses ?: return false
        return procs.any { it.processName == pkg }
    }
}