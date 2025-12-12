package ai.p2ach.p2achandroidvision.utils

import ai.p2ach.p2achandroidvision.Const
import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
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
                    match?.substringAfter(":")?.trim()?.takeIf { it.isNotBlank() }?.let { return it }
                }
            } catch (_: Throwable) {}

            readTextIfExists("/proc/device-tree/model")?.let { return it }
            readTextIfExists("/proc/device-tree/compatible")?.let { return it }

            listOf(
                "ro.soc.model",
                "ro.board.platform",
                "ro.hardware",
                "ro.product.board",
                "ro.vendor.sdkversion"
            ).forEach { k ->
                runGetprop(k)?.let { return it }
            }

            return listOf(Build.BOARD, Build.HARDWARE, Build.DEVICE, Build.MODEL)
                .firstOrNull { !it.isNullOrBlank() }
                ?: Const.MDM.SETTING.DEFAULT.DEFAULT_DEVICE_NAME
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
            "rk3328" in v || "3328" in v -> "rk3328"
            "amls905x5" in v || "s905x5" in v -> "S905X5"
            else -> "cpu"
        }
    }

    private fun readTextIfExists(path: String): String? = try {
        val f = File(path)
        if (f.exists() && f.canRead()) f.readText().trim('\u0000', '\n', ' ', '\t') else null
    } catch (_: Throwable) {
        null
    }

    private fun runGetprop(key: String): String? {
        return try {
            val process = Runtime.getRuntime().exec("getprop $key")
            process.inputStream.bufferedReader().use { it.readText().trim() }
                .takeIf { it.isNotEmpty() }
        } catch (_: Throwable) {
            null
        }
    }

    fun getDeviceName(context: Context, mdmDeviceId: String): String {
        val globalName = try {
            Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
        } catch (e: Exception) {
            null
        }

        val baseName = (globalName?.takeIf { it.isNotBlank() }
            ?: Build.MODEL
            ?: Const.MDM.SETTING.DEFAULT.DEFAULT_DEVICE_NAME
                ).trim()

        val uuid = getOrCreateDeviceUuid()
        val short = uuid.replace("-", "").take(8)
        val launchedName = "$baseName-$short"

        return if (mdmDeviceId.isEmpty()) {
            launchedName
        } else if (mdmDeviceId != launchedName) {
            mdmDeviceId
        } else {
            mdmDeviceId
        }
    }

    fun getOrCreateDeviceUuid(): String = UUID.randomUUID().toString()

    fun UsbDevice.isCameraDevice(): Boolean {
        if (deviceClass == UsbConstants.USB_CLASS_VIDEO) return true
        for (i in 0 until interfaceCount) {
            val intf = getInterface(i)
            if (intf.interfaceClass == UsbConstants.USB_CLASS_VIDEO) return true
        }
        return false
    }


    data class AiFeatureToggles(
        val autoStartCameraActivity: Boolean? = null,
        val use_headpose: Boolean? = null,
        val use_yolo: Boolean? = null,
        val use_pose: Boolean? = null,
        val use_reid: Boolean? = null,
        val use_par: Boolean? = null,
        val autoRotation: Boolean? = null,
        val rotation: Int? = null,
        val genderThr: Float? = null,
        val use_ageGender_NpuModel: Boolean? = null
    )

    private data class AiModelEntry(
        val relPath: String,
        val enabledIf: (AiFeatureToggles, HwType) -> Boolean = { _, _ -> true }
    )

    private data class AiHwProfile(
        val basePathFor: () -> String,
        val defaults: AiFeatureToggles,
        val models: List<AiModelEntry>
    )

    private fun defaultModelBasePath(hw: HwType): String =
        when (hw) {
            HwType.RK3328,
            HwType.CPU -> "models/cpu"
            else -> "models/${hw.name.lowercase()}"
        }

    private val AI_PROFILES: Map<HwType, AiHwProfile> by lazy {
        val cpuProfile = AiHwProfile(
            basePathFor = { defaultModelBasePath(HwType.CPU) },
            defaults = AiFeatureToggles(
                autoStartCameraActivity = true,
                use_headpose = true,
                autoRotation = false,
                rotation = 2,
                genderThr = 0.65f
            ),
            models = listOf(
                AiModelEntry("face_detection_back_256x256_float32.tflite"),
                AiModelEntry(
                    relPath = "head-pose-estimation.tflite",
                    enabledIf = { t, _ -> t.use_headpose == true }
                ),
                AiModelEntry(
                    relPath = "age-gender-model.tflite",
                    enabledIf = { t, _ -> t.use_headpose == true }
                )
            )
        )

        val rk3588Profile = AiHwProfile(
            basePathFor = { defaultModelBasePath(HwType.RK3588) },
            defaults = AiFeatureToggles(use_pose = true, use_headpose = true, use_par = true),
            models = listOf(
                AiModelEntry(
                    "yolov8_pose.rknn",
                    enabledIf = { t, _ -> t.use_pose == true }
                ),
                AiModelEntry(
                    "yolov8n_crowdhuman.rknn",
                    enabledIf = { t, _ -> t.use_pose != true }
                ),
                AiModelEntry(
                    "label_crowdhuman.txt",
                    enabledIf = { t, _ -> t.use_pose != true }
                ),
                AiModelEntry(
                    "yolov8n.rknn",
                    enabledIf = { t, hw -> t.use_yolo == true && hw == HwType.RK3588 }
                ),
                AiModelEntry(
                    "label_coco_80.txt",
                    enabledIf = { t, hw -> t.use_yolo == true && hw == HwType.RK3588 }
                ),
                AiModelEntry(
                    "head-pose-estimation.tflite",
                    enabledIf = { t, _ -> t.use_headpose == true }
                ),
                AiModelEntry(
                    "age-gender-model.rknn",
                    enabledIf = { t, _ -> t.use_headpose == true && t.use_ageGender_NpuModel == true }
                ),
                AiModelEntry(
                    "age-gender-model.tflite",
                    enabledIf = { t, _ -> t.use_headpose == true && t.use_ageGender_NpuModel != true }
                ),
                AiModelEntry(
                    "osnet_x0_25_market1501.rknn",
                    enabledIf = { t, _ -> t.use_reid == true }
                ),
                AiModelEntry(
                    "par_age_gender.rknn",
                    enabledIf = { t, _ -> t.use_par == true }
                ),
                AiModelEntry("retinaface_mobile640.rknn")
            )
        )

        val rk3576Profile = AiHwProfile(
            basePathFor = { defaultModelBasePath(HwType.RK3576) },
            defaults = rk3588Profile.defaults.copy(
                use_yolo = false,
                use_reid = false
            ),
            models = rk3588Profile.models.filterNot {
                it.relPath.startsWith("osnet_")
            }
        )

        val rk3566Profile = AiHwProfile(
            basePathFor = { defaultModelBasePath(HwType.RK3566) },
            defaults = rk3588Profile.defaults.copy(
                use_yolo = false,
                use_reid = false
            ),
            models = rk3588Profile.models.filterNot {
                it.relPath == "yolov8n.rknn" ||
                        it.relPath == "label_coco_80.txt" ||
                        it.relPath.startsWith("osnet_")
            }
        )

        val rk3328Profile = AiHwProfile(
            basePathFor = { defaultModelBasePath(HwType.RK3328) },
            defaults = cpuProfile.defaults,
            models = cpuProfile.models
        )

        val s905x5Profile = AiHwProfile(
            basePathFor = { defaultModelBasePath(HwType.S905X5) },
            defaults = AiFeatureToggles(
                autoStartCameraActivity = true,
                use_headpose = true,
                autoRotation = false,
                genderThr = 0.5f
            ),
            models = listOf(
                AiModelEntry("face_detection_back_256x256_float32.tflite"),
                AiModelEntry(
                    "head-pose-estimation-full-int8.tflite",
                    enabledIf = { t, _ -> t.use_headpose == true }
                ),
                AiModelEntry(
                    "age-gender-model-full-int8.tflite",
                    enabledIf = { t, _ -> t.use_headpose == true }
                )
            )
        )

        mapOf(
            HwType.CPU to cpuProfile,
            HwType.RK3588 to rk3588Profile,
            HwType.RK3576 to rk3576Profile,
            HwType.RK3566 to rk3566Profile,
            HwType.RK3328 to rk3328Profile,
            HwType.S905X5 to s905x5Profile
        )
    }

    fun getDefaultAiFeatureToggles(): AiFeatureToggles {
        val hw = getHwType()
        return AI_PROFILES[hw]?.defaults ?: AiFeatureToggles()
    }

    fun getRequiredModelKeys(
        override: AiFeatureToggles? = null
    ): List<String> {
        val hw = getHwType()
        val profile = AI_PROFILES[hw] ?: AI_PROFILES[HwType.CPU]!!

        val toggles = mergeAiToggles(profile.defaults, override)
        val base = profile.basePathFor()

        return profile.models
            .filter { it.enabledIf(toggles, hw) }
            .map { "$base/${it.relPath}" }
    }

    private fun mergeAiToggles(
        base: AiFeatureToggles,
        override: AiFeatureToggles?
    ): AiFeatureToggles {
        if (override == null) return base
        return AiFeatureToggles(
            /*JNI*/
            use_headpose = override.use_headpose ?: base.use_headpose,
            use_yolo = override.use_yolo ?: base.use_yolo,
            use_pose = override.use_pose ?: base.use_pose,
            use_reid = override.use_reid ?: base.use_reid,
            use_par = override.use_par ?: base.use_par,
            use_ageGender_NpuModel = override.use_ageGender_NpuModel ?: base.use_ageGender_NpuModel,
            genderThr = override.genderThr ?: base.genderThr, /*TimingsAndParameters*/
            rotation = override.rotation ?: base.rotation,


            autoStartCameraActivity = override.autoStartCameraActivity ?: base.autoStartCameraActivity,
            autoRotation = override.autoRotation ?: base.autoRotation,
           /*base */


        )
    }

    private fun defaultBasePath(): String {
        val hwType = getHwType()
        return when (hwType) {
            HwType.RK3328, HwType.CPU -> "models/cpu"
            else -> "models/${hwType.name.lowercase()}"
        }
    }

    fun getModelPath(context: Context): String {
        return File(context.filesDir, "${defaultBasePath()}/").absolutePath
    }

}