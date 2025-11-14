package ai.p2ach.p2achandroidvision.repos.mdm

import ai.p2ach.p2achandroidlibrary.utils.Log
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
    fun exposureToJson(value: Exposure?): String = gson.toJson(value ?: Exposure())

    @TypeConverter
    @JvmStatic
    fun jsonToExposure(value: String?): Exposure = gson.fromJson(value ?: "{}", Exposure::class.java)



    @TypeConverter
    @JvmStatic
    fun networkAndAPIToJson(value: NetWorkAndApi?): String = gson.toJson(value ?: NetWorkAndApi())

    @TypeConverter
    @JvmStatic
    fun jsonToNetworkAndAPI(value: String?): NetWorkAndApi = gson.fromJson(value ?: "{}", NetWorkAndApi::class.java)


    @TypeConverter
    @JvmStatic
    fun versionsToJson(value: Versions?): String = gson.toJson(value ?: Versions())

    @TypeConverter
    @JvmStatic
    fun jsonToVersions(value: String?): Versions = gson.fromJson(value ?: "{}", Versions::class.java)




    @TypeConverter
    @JvmStatic
    fun quadrangleToJson(value: QuadrangleRegion?): String = gson.toJson(value ?: QuadrangleRegion())

    @TypeConverter
    @JvmStatic
    fun jsonToQuadrangle(value: String?): QuadrangleRegion = gson.fromJson(value ?: "{}", QuadrangleRegion::class.java)


    @TypeConverter
    @JvmStatic
    fun jsonToMdmEntity(value: String?): MDMEntity {

        return try {
            gson.fromJson(value ?: "{}", MDMEntity::class.java)
        }catch (e : Exception){
            Log.e("MDMEntity is not correct. ${e.message}")
            MDMEntity()
        }


    }


}