package ai.p2ach.p2achandroidvision.repos.ai

import com.google.gson.JsonArray
import com.google.gson.JsonObject

data class EventParams(
    val deviceId: String,
    val age: String,
    val gender: String,
    val day: String,
    val timeBin: String,
    val attentionTime: Float
) {
    fun toJsonObject(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("device_id", deviceId)
        jsonObject.addProperty("age", age)
        jsonObject.addProperty("gender", gender)
        jsonObject.addProperty("day", day)
        jsonObject.addProperty("time_bin", timeBin)
        jsonObject.addProperty("attention_time", attentionTime)
        return jsonObject
    }
}

data class Event(
    val name: String,
    val params: EventParams
) {
    fun toJsonObject(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("name", name)
        jsonObject.add("params", params.toJsonObject())
        return jsonObject
    }
}

data class GaClientEvent(
    val clientId: String,
    val timestampMicros: Long,
    val events: List<Event>
) {
    fun toJsonObject(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.addProperty("client_id", clientId)
        jsonObject.addProperty("timestamp_micros", timestampMicros)

        val eventsJsonArray = JsonArray()
        events.forEach { event ->
            eventsJsonArray.add(event.toJsonObject())
        }
        jsonObject.add("events", eventsJsonArray)
        return jsonObject
    }
}
