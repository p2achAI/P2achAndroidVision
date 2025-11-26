package ai.p2ach.p2achandroidvision.repos.ai
import ai.p2ach.p2achandroidvision.repos.mdm.MDMEntity





class NativeLib {
    enum class ProcessorStatus {
        NotInitialized,
        Initialized,
        Running

    }




    data class AppSettings(private val test: String = "")

    companion object {
        var processorStatus = ProcessorStatus.NotInitialized
        @JvmStatic external fun initialize(resourcePath: String, json:String) : Int
        @JvmStatic external fun process(objMat: Long): AnalysisResult?
        @JvmStatic external fun finalize()
        @JvmStatic private external fun updateSettings(appSettings: MDMEntity) : Int
        @JvmStatic private external fun getCurInfo(filterUndetected: Boolean = true, maxObj: Int = -1) : AnalysisResult?
        @JvmStatic private external fun clearRecogData() : Int
        @JvmStatic fun getCurInfoSafe(filterUndetected: Boolean = true, maxObj: Int = -1): AnalysisResult? {
            return if (processorStatus != ProcessorStatus.NotInitialized) {
                getCurInfo(filterUndetected, maxObj)
            } else {
                null
            }
        }
        @JvmStatic fun updateSettingsSafe(appSettings: MDMEntity): Int {
            return if (processorStatus != ProcessorStatus.NotInitialized) {
                updateSettings(appSettings)
            } else {
                -1
            }
        }
        @JvmStatic fun clearRecogDataSafe(): Int {
            return if (processorStatus != ProcessorStatus.NotInitialized) {
                clearRecogData()
            } else {
                -1
            }
        }
    }
}