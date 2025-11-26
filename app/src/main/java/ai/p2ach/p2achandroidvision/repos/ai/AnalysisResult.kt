package ai.p2ach.p2achandroidvision.repos.ai

import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.round

data class AnalysisResult (
    var faceResults: Array<FaceResult>,
    var humanResults: Array<HumanResult>,
    val curFrameResults: CurFrameResult,
    val collectData: Int,
    var peopleCnt: Float,
    var headCnt: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnalysisResult

        if (!faceResults.contentEquals(other.faceResults)) return false
        if (!humanResults.contentEquals(other.humanResults)) return false
        if (peopleCnt != other.peopleCnt) return false
        if (headCnt != other.headCnt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = faceResults.contentHashCode()
        result = 31 * result + humanResults.contentHashCode()
        result = 31 * result + peopleCnt.hashCode()
        result = 31 * result + headCnt.hashCode()
        return result
    }
}

data class CurFrameResult (
    var faceResults: Array<FaceResult>,
    var humanResults: Array<HumanResult>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CurFrameResult

        if (!faceResults.contentEquals(other.faceResults)) return false
        if (!humanResults.contentEquals(other.humanResults)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = faceResults.contentHashCode()
        result = 31 * result + humanResults.contentHashCode()
        return result
    }
}

fun getDayOfWeek(instant: Instant, zoneId: ZoneId): String {
    val dayOfWeek = instant.atZone(zoneId).dayOfWeek
    return dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
}

fun getTimeBin(instant: Instant, zoneId: ZoneId): String {
    val localDateTime = instant.atZone(zoneId).toLocalDateTime()
    val hour = localDateTime.hour.toDouble() + localDateTime.minute / 60.0
    val roundedTime = round(hour * 2) / 2
    return roundedTime.toString()
}

fun AnalysisResult.toClientEvent(androidId: String, instant: Instant): GaClientEvent {
    val zoneId = ZoneId.systemDefault()
    val timestampMicros = instant.toEpochMilli() * 1000
    val dayOfWeek = getDayOfWeek(instant, zoneId)
    val timeBin = getTimeBin(instant, zoneId)

    val events = faceResults.map { faceResult ->
        val params = EventParams(
            deviceId = androidId,
            gender = faceResult.getGenderStr(),
            age = faceResult.getAgeStr(),
            day = dayOfWeek,
            timeBin = timeBin,
            attentionTime = faceResult.attention_time
        )
        Event(
            name = "ai_vision",
            params = params
        )
    }

    return GaClientEvent(
        clientId = androidId,
        timestampMicros = timestampMicros,
        events = events
    )
}