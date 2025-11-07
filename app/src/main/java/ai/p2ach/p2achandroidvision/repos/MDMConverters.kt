import ai.p2ach.p2achandroidvision.repos.CamParam
import ai.p2ach.p2achandroidvision.repos.QuadrangleRegion
import ai.p2ach.p2achandroidvision.repos.ROI
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object MDMConverters {
    private val gson = Gson()

    @TypeConverter
    @JvmStatic
    fun listStringToJson(value: List<String>?): String = gson.toJson(value ?: emptyList<String>())

    @TypeConverter
    @JvmStatic
    fun jsonToListString(value: String?): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value ?: "[]", type)
    }

    @TypeConverter
    @JvmStatic
    fun roiToJson(value: ROI?): String = gson.toJson(value ?: ROI())

    @TypeConverter
    @JvmStatic
    fun jsonToRoi(value: String?): ROI = gson.fromJson(value ?: "{}", ROI::class.java)

    @TypeConverter
    @JvmStatic
    fun camParamToJson(value: CamParam?): String = gson.toJson(value ?: CamParam())

    @TypeConverter
    @JvmStatic
    fun jsonToCamParam(value: String?): CamParam = gson.fromJson(value ?: "{}", CamParam::class.java)

    @TypeConverter
    @JvmStatic
    fun quadrangleToJson(value: QuadrangleRegion?): String = gson.toJson(value ?: QuadrangleRegion())

    @TypeConverter
    @JvmStatic
    fun jsonToQuadrangle(value: String?): QuadrangleRegion = gson.fromJson(value ?: "{}", QuadrangleRegion::class.java)
}