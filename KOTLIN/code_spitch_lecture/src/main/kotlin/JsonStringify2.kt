import kotlin.reflect.KProperty

fun <T: Any?> jsonStringify(target: T) = when (target) {
    null -> "null"
    is String -> makeJsonString(target)
    is Number, is Boolean -> "$target"
    is List<*> -> jsonList2(target)
    else -> jsonObject2(target)
}

private fun <T: Any> jsonObject2(target: T) : String {
    return target::class.members
        .filterIsInstance<KProperty<*>>()
        .joinTo(StringBuilder(), ",", "{", "}" ) {
            val key = it.name
            val value = it.getter.call(target)
            "${jsonStringify(key)}:${jsonStringify(value)}"
    }.toString()
}

private fun jsonList2(target: List<*>): String {
    return target.joinTo(StringBuilder(), ",", "[", "]", transform = ::jsonStringify).toString()
}

private fun makeJsonString(value: String) = """"${value.replace("\"", "\\\"")}""""