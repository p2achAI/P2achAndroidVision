package ai.p2ach.p2achandroidvision.repos.ai

data class FaceResult (
    var id: Int,
    var age: Float = 0f,
    var gender: Float = 0f,
    var dwell_time: Float = 0F,
    var attention_time: Float = 0F,
    val pos3d: FloatArray,
    val gazePt: FloatArray,
    val gazeCov: FloatArray,
    var isAttention: Boolean,
    var dest: String = "",
    var frameResults: Array<FaceFrameResult>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceResult
        if (id != other.id) return false
        if (!gazePt.contentEquals(other.gazePt)) return false
        if (!gazeCov.contentEquals(other.gazeCov)) return false

        return frameResults.contentEquals(other.frameResults)
    }

    override fun hashCode(): Int {
        return frameResults.contentHashCode()
    }

    fun getGenderStr(): String {
        return when(gender) {
            in 0.0f..0.49f -> "Female"
            else -> "Male"
        }
    }

    fun getAgeStr(): String {
        return when(age) {
            in 0.0f..19.99f -> "10s"
            in 20.0f..29.99f -> "20s"
            in 30.0f..39.99f -> "30s"
            in 40.0f..49.99f -> "40s"
            else -> "50s"
        }
    }
}

data class FaceFrameResult (
    var age: Float = 0f,
    var gender: Float = 0f,
    var headpose: FloatArray?,
    var bbox: IntArray?,
    var bboxConf: Float = 0f,
    var timestamp: String? = null,
    var mask: Int = -1
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FaceFrameResult

        if (age != other.age) return false
        if (gender != other.gender) return false
        if (headpose != null) {
            if (other.headpose == null) return false
            if (!headpose.contentEquals(other.headpose)) return false
        } else if (other.headpose != null) return false
        if (bbox != null) {
            if (other.bbox == null) return false
            if (!bbox.contentEquals(other.bbox)) return false
        } else if (other.bbox != null) return false
        if (timestamp != other.timestamp) return false
        if (mask != other.mask) return false

        return true
    }

    override fun hashCode(): Int {
        var result = age.hashCode()
        result = 31 * result + gender.hashCode()
        result = 31 * result + (headpose?.contentHashCode() ?: 0)
        result = 31 * result + (bbox?.contentHashCode() ?: 0)
        result = 31 * result + (timestamp?.hashCode() ?: 0)
        result = 31 * result + mask
        return result
    }
}
