import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe


class JsonStringify2Test: AnnotationSpec(){

    @Test
    fun 프로퍼티_문자열_숫자_정상반환_테스트() {
        val stringify : (JSONCLASS) -> String = ::jsonStringify
        stringify(JSONCLASS("leebongho", 31)) shouldBe """ {"age":31,"name":"leebongho"} """.trim()
    }

    @Test
    fun 프로퍼티_문자열_숫자_정상반환_테스트2() {
        val stringify : (JSONCLASS) -> String = ::jsonStringify
        stringify(JSONCLASS("leebongho", 31)) shouldBe """ {"age":31,"name":"leebongho"} """.trim()
    }
}

class JSONCLASS (val name: String, val age: Int)