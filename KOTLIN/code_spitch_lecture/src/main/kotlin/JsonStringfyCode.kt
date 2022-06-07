import kotlin.reflect.KProperty

// T 타입의 객체를 JSON 문자열 형태로 반환하는 함수
fun <T: Any> stringify(target: T): String {
    val builder = StringBuilder()
    builder.append("{")

    target::class.members.filterIsInstance<KProperty<*>>().forEach { it ->
        builder.append(it.name, ":")
        builder.append(it.getter.call(target), ",")
    }

    builder.append("}")
    return "$builder"
}


