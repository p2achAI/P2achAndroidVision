package ai.p2ach.p2achandroidvision.utils

fun String.parseTimeString(): Triple<Int, Int, Int> {

    if (this.isBlank()) return Triple(0, 0, 0)


    val tokens = this.split(":")
        .map { it.trim() }
        .filter { it.isNotEmpty() }


    fun safeInt(str: String?): Int =
        str?.toIntOrNull()?.coerceIn(0, 59) ?: 0

    return when (tokens.size) {

        1 -> {
            // 예: "18", "19시", "abc"
            val hour = safeInt(tokens[0])
            Triple(hour, 0, 0)
        }

        2 -> {
            // 예: "18:01", "07:aa", "13:70"
            val hour = safeInt(tokens[0])
            val min = safeInt(tokens[1])
            Triple(hour, min, 0)
        }

        3 -> {
            // 예: "18:01:02", "18:01:aa"
            val hour = safeInt(tokens[0])
            val min = safeInt(tokens[1])
            val sec = safeInt(tokens[2])
            Triple(hour, min, sec)
        }

        else -> Triple(-1, -1, -1)

    }

}