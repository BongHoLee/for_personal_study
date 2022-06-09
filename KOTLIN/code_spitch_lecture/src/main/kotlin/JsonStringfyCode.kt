import kotlin.reflect.KProperty

// T 타입의 객체를 JSON 문자열 형태로 반환하는 함수
fun stringify(target: Any?) = when (target) {
    null -> "null"  // smart casting -> 여기서 type check를 했으니 이하 코드에서는 value가 null이 아님이 보장된다.
    is String -> jsonString(target)
    is Boolean, is Number -> "$target"
    is List<*> -> jsonList(target)
    else -> jsonObject(target)       // 재귀적 (stringify -call-> jsonObject -call-> stringify ...)
}

private fun <T : Any> jsonObject(target: T): String {
    return target::class.members.filterIsInstance<KProperty<*>>()
        .joinTo(StringBuilder(), ",", "{", "}") {
            val value = it.getter.call(target)
            "${stringify(it.name)}:${stringify(value)}"
        }.toString()
}

private fun jsonString(value: String) = """"${value.replace("\"", "\\\"")}""""

private fun jsonList(target: List<*>): String {
    return target.joinTo(StringBuilder(), ",", "[", "]", transform = ::stringify).toString()
}

// ============== 커스텀 joinTo ============= //

fun <T> Iterable<T>.joinTo(separator: () -> Unit, transform: (T) -> Unit) {
    this.forEachIndexed {count, element ->
        if (count != 0) separator()
        transform(element)
    }
}

fun stringify2(value: Any): String {
    val builder = StringBuilder()
    jsonValue(value, builder)
    return builder.toString()
}

fun jsonValue(value: Any?, builder: StringBuilder) {
    when(value) {
        null -> builder.append("null")
        is String -> jsonString(value, builder)
        is Boolean, is Number -> builder.append("$value")
        is List<*> -> jsonList(value, builder)
        else -> jsonObject(value, builder)
    }
}

private fun jsonString(value: String, builder: StringBuilder) = builder.append(jsonString(value))

private fun jsonObject(target: Any, builder: StringBuilder) {
    builder.append("{")
    target::class.members
        .filterIsInstance<KProperty<*>>()
        .joinTo({builder.append(",")}) {
            jsonValue(it.name, builder)
            builder.append(":")
            jsonValue(it.getter.call(target), builder)
        }
    builder.append("}")
}

private fun jsonList(target: List<*>, builder: StringBuilder) {
    builder.append("[")
    target.joinTo({builder.append(",")}) { jsonValue(it, builder) }
    builder.append("]")
}


class JSON0(val a: Int, val b: String, val c: List<String>)

fun main() {
    println(stringify2(JSON0(3, "abc", listOf("abc", "def"))))
}



