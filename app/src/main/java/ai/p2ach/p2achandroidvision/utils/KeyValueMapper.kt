package ai.p2ach.p2achandroidvision.utils


import kotlin.reflect.full.memberProperties
import kotlin.reflect.KProperty1


data class KeyValueItem(
    val key: String,
    val value: String
)

//sealed class MdmUiItem {
//    data class SectionHeader(val title: String) : MdmUiItem()
//    data class Row(val key: String, val value: String) : MdmUiItem()
//}

inline fun <reified T : Any> T.toKeyValueList(
    labelMap: Map<String, String> = emptyMap()
): List<KeyValueItem> {
    return T::class.memberProperties
        .sortedBy { it.name }
        .map { prop ->
            val rawKey = prop.name
            val label = labelMap[rawKey] ?: rawKey
            val rawValue = try {
                prop.get(this)
            } catch (_: Throwable) {
                null
            }
            KeyValueItem(
                key = label,
                value = rawValue?.toString() ?: ""
            )
        }
}