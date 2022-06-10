import kotlin.reflect.KProperty

fun <T: Any> jsonStringify(target: T) : String {
    return target::class.members
        .filterIsInstance<KProperty<*>>()
        .joinTo(StringBuilder(), ",", "{", "}" ) {
            val key = it.name
            val value = it.getter.call(target)
            "${makeJsonString(key)} : ${ if (value is String) makeJsonString(value) else value}".replace(" ","")
    }.toString()
}

private fun makeJsonString(value: String) = """"${value.replace("\"", "\\\"")}""""