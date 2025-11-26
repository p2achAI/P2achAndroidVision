package ai.p2ach.p2achandroidvision.repos.ai

data class HumanResult (
    var id: Int,
    var has_faceInfo: Boolean = false,
    var facebbox: IntArray = intArrayOf(),
    var age: Float = -1f,
    var gender: Float = -1f,
    var face_age: Float = -1f,
    var face_gender: Float = -1f,
    var pos3d : FloatArray = floatArrayOf(),
    val avgFeature : FloatArray = floatArrayOf(),
    var dwelltime: Float = 0F,
    var dest: String = "",
    var inDirection: Int = -1,
    var outDirection: Int = -1,
    var frameResults: Array<HumanFrameResult>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HumanResult
        if (id != other.id) return false
        return frameResults.contentEquals(other.frameResults)
    }

    override fun hashCode(): Int {
        return frameResults.contentHashCode()
    }
}

data class HumanFrameResult (
    var bodypose: FloatArray = floatArrayOf(),
    var bbox: IntArray = intArrayOf(),
    var feature: FloatArray = floatArrayOf(),
    var timestamp: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HumanFrameResult

        if (!bodypose.contentEquals(other.bodypose)) return false
        if (!bbox.contentEquals(other.bbox)) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bodypose.contentHashCode()
        result = 31 * result + bbox.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}