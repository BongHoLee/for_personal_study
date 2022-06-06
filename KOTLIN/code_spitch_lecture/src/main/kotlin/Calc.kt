class Calc {

}

// [^...] 표현식으로 [] 내 해당하는 요소만 제외하겠다는 white list 정규표현식
val trim = """[^.\d-+*/]""".toRegex()

// 파라미터 문자열 v 중 정규식 trim에 해당하는 요소들은 모두 공백으로 치환
fun trim(v: String): String {
    return v.replace(trim, "")
}

// - 연산을 +-로 치환
fun repMtoPM(v: String) = v.replace("-", "+-")

// *, / 연산을 기준으로 group
val groupMD = """((?:\+|\+-)?[.\d]+)([*/])((?:\+|\+-)?[.\d]+)""".toRegex()

//
fun foldGroup(v: String): Double = groupMD.findAll(v).fold(0.0) { acc, curr ->
    val (_, left, op, right) = curr.groupValues
    val leftValue = left.replace("+", "").toDouble()
    val rightValue = right.replace("+", "").toDouble()
    val result = when(op) {
        "+" -> leftValue * rightValue
        "/" -> leftValue / rightValue
        else -> throw Throwable("Invalid operation")
    }

    acc + result
}
