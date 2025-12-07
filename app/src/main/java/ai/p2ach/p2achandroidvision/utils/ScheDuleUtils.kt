package ai.p2ach.p2achandroidvision.utils

import ai.p2ach.p2achandroidvision.Const
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

typealias AlarmAction = () -> Unit

object AlarmManagerUtil {

    private const val ACTION_ALARM = "ai.p2ach.p2achandroidvision.ALARM"
    private const val EXTRA_ID = "extra_id"

    private val tasks = ConcurrentHashMap<String, AlarmTaskConfig>()

    // 기존 그대로
    fun scheduleSeries(
        context: Context,
        startAtMillis: Long,
        intervalMillis: Long = 7000,
        count: Int = 1,
        action: AlarmAction,
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
        runCatching { config.action.invoke() }

        if (config.remaining == Const.ALARM_WOKER.WORK_INFINITY) {
            val nextAt = System.currentTimeMillis() + config.intervalMillis
            scheduleAlarm(context, config, triggerAt = nextAt)
            return
        }

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

    /**
     * 특정 요일 + 시각 기준으로 첫 트리거를 잡는 버전
     *
     * @param dayOfWeek Calendar.MONDAY..SUNDAY, null이면 기존처럼 "오늘/내일" 기준
     */
    fun scheduleAtSpecificTime(
        context: Context,
        hourOfDay: Int,
        minute: Int,
        second: Int = 0,
        intervalMillis: Long = 0,
        count: Int = 1,
        dayOfWeek: Int? = null,
        action: AlarmAction,
    ): String {
        val id = UUID.randomUUID().toString()
        val config = AlarmTaskConfig(
            id = id,
            intervalMillis = intervalMillis,
            remaining = count,
            action = action
        )
        tasks[id] = config

        val triggerAt = calculateNextTriggerTime(
            hour = hourOfDay,
            minute = minute,
            second = second,
            dayOfWeek = dayOfWeek
        )
        scheduleAlarm(context, config, triggerAt)

        return id
    }

    private fun calculateNextTriggerTime(
        hour: Int,
        minute: Int,
        second: Int,
        dayOfWeek: Int? = null
    ): Long {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            timeInMillis = now

            if (dayOfWeek != null) {
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
            }
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, 0)
        }

        // 이미 지난 시각이면
        if (cal.timeInMillis <= now) {
            if (dayOfWeek != null) {
                // 지정 요일 기준으로 다음 주
                cal.add(Calendar.WEEK_OF_YEAR, 1)
            } else {
                // 기존 동작: 다음 날
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        return cal.timeInMillis
    }

    data class AlarmTaskConfig(
        val id: String,
        val intervalMillis: Long,
        var remaining: Int,
        val action: AlarmAction
    )

    fun scheduleInfiniteFromNow(
        context: Context,
        intervalMillis: Long,
        action: AlarmAction
    ): String {
        val id = UUID.randomUUID().toString()
        val config = AlarmTaskConfig(
            id = id,
            intervalMillis = intervalMillis,
            remaining = Const.ALARM_WOKER.WORK_INFINITY,
            action = action
        )
        tasks[id] = config

        scheduleAlarm(context, config, System.currentTimeMillis())
        return id
    }

}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("extra_id") ?: return
        AlarmManagerUtil.onAlarm(context, id)
    }
}