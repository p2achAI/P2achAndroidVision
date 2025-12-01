package ai.p2ach.p2achandroidvision.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

typealias AlarmAction = () -> Unit

object AlarmManagerUtil {

    private const val ACTION_ALARM = "ai.p2ach.p2achandroidvision.ALARM"
    private const val EXTRA_ID = "extra_id"

    private val tasks = ConcurrentHashMap<String, AlarmTaskConfig>()

    fun scheduleSeries(
        context: Context,
        startAtMillis: Long,
        intervalMillis: Long,
        count: Int,
        action: AlarmAction
    ): String {
        val id = UUID.randomUUID().toString()
        val config = AlarmTaskConfig(
            id = id,
            intervalMillis = intervalMillis,
            remaining = count,
            action = action
        )
        tasks[id] = config
        scheduleAlarm(context, config, triggerAt = startAtMillis)
        return id
    }

    fun cancel(context: Context, id: String) {
        val config = tasks.remove(id) ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, config.id)
        alarmManager.cancel(pendingIntent)
    }

    internal fun onAlarm(context: Context, id: String) {
        val config = tasks[id] ?: return
        if (config.remaining <= 0) {
            tasks.remove(id)
            return
        }
        runCatching { config.action.invoke() }
        config.remaining -= 1
        if (config.remaining > 0) {
            val nextAt = System.currentTimeMillis() + config.intervalMillis
            scheduleAlarm(context, config, triggerAt = nextAt)
        } else {
            tasks.remove(id)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarm(context: Context, config: AlarmTaskConfig, triggerAt: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, config.id)


        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent
        )
    }

    private fun buildPendingIntent(context: Context, id: String): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM
            putExtra(EXTRA_ID, id)
        }
        val requestCode = id.hashCode()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    data class AlarmTaskConfig(
        val id: String,
        val intervalMillis: Long,
        var remaining: Int,
        val action: AlarmAction
    )
}


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("extra_id") ?: return
        AlarmManagerUtil.onAlarm(context, id)
    }
}