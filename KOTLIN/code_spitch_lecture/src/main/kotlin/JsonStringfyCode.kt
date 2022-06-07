import kotlin.reflect.KProperty

// T 타입의 객체를 JSON 문자열 형태로 반환하는 함수
fun <T: Any> stringify(target: T): String {
    val builder = StringBuilder()
    builder.append("{")

    target::class.members.filterIsInstance<KProperty<*>>().forEach { it ->
        builder.append(jsonString(it.name), ":")
        val value = it.getter.call(target)
        builder.append(if (value is String) jsonString(value) else value, ",")
    }

    builder.append("}")
    return "$builder"
}

private fun jsonString(value: String) = """"${value.replace("\"", "\\\"")}""""


class JSON0(val a: Int, val b: String)

fun main() {
    println(stringify(JSON0(3, "abc")))
}



