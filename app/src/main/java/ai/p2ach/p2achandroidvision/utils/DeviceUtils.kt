package ai.p2ach.p2achandroidvision.utils

import ai.p2ach.p2achandroidvision.Const
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.io.File
import java.util.UUID

object DeviceUtils {


    enum class HwType {
        RK3588,
        RK3576,
        RK3566,
        RK3328,
        S905X5,
        CPU
    }



    fun detectSoC(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return Build.SOC_MODEL
        } else {
            try {
                val cpuinfo = File("/proc/cpuinfo")
                if (cpuinfo.canRead()) {
                    val match = cpuinfo.useLines { line ->
                        line.firstOrNull { it.contains("Hardware", true) || it.contains("model name", true) }
                    }
                    match?.substringAfter(":")?.trim()?.takeIf { it.isNotBlank() }?.let {return it}
                }
            } catch (_: Throwable) {}

            readTextIfExists("/proc/device-tree/model")?.let {return it}
            readTextIfExists("/proc/device-tree/compatible")?.let {return it}

            // 5) getprop via shell (no hidden API)
            listOf(
                "ro.soc.model",
                "ro.board.platform",
                "ro.hardware",
                "ro.product.board",
                "ro.vendor.sdkversion"
            ).forEach { k ->
                runGetprop(k)?.let { return it }
            }

            return listOf(Build.BOARD, Build.HARDWARE, Build.DEVICE, Build.MODEL).firstOrNull {!it.isNullOrBlank()} ?: Const.MDM.SETTING.DEFAULT.DEFAULT_DEVICE_NAME
        }
    }

    fun getHwType(): HwType {
        val socName = detectSoC()
        val socShortName = shortenSoCName(socName)
        return when (socShortName.lowercase()) {
            "rk3588" -> HwType.RK3588
            "rk3576" -> HwType.RK3576
            "rk3566" -> HwType.RK3566
            "rk3328" -> HwType.RK3328
            "s905x5" -> HwType.S905X5
            else -> HwType.CPU
        }
    }



    fun shortenSoCName(socName: String): String {
        val v = socName.lowercase()
        return when {
            "rk3588" in v || "3588" in v -> "rk3588"
            "rk3576" in v || "3576" in v -> "rk3576"
            "rk3566" in v || "3566" in v -> "rk3566"
            "rk3328" in v || "3328" in v -> "rk3328" // RK3328 has no NPU
            "amls905x5" in v || "s905x5" in v -> "S905X5"
            else -> "cpu"
        }
    }

    private fun readTextIfExists(path: String): String? = try {
        val f = File(path)
        if (f.exists() && f.canRead()) f.readText().trim('\u0000', '\n', ' ', '\t') else null
    } catch (_: Throwable) { null }

    private fun runGetprop(key: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $key")
            process.inputStream.bufferedReader().use { it.readText().trim() }.takeIf { it.isNotEmpty() }
        } catch (_: Throwable) {
            null
        }
    }




    fun getDeviceName(context: Context, mdmDeviceId: String): String {


        val globalName =  try {
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
        }catch (e : Exception){
            null
        }
        val baseName = (globalName?.takeIf { it.isNotBlank() } ?: Build.MODEL ?: Const.MDM.SETTING.DEFAULT.DEFAULT_DEVICE_NAME).trim()
        val uuid = getOrCreateDeviceUuid()
        val short = uuid.replace("-", "").take(8)
        val launchedName = "$baseName-$short"

        return if(mdmDeviceId.isEmpty()) launchedName
        else if(mdmDeviceId != launchedName)mdmDeviceId
        else mdmDeviceId

    }


    private fun getOrCreateDeviceUuid(): String = UUID.randomUUID().toString()



}