import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe


class JsonStringify2Test: AnnotationSpec(){

    @Test
    fun 프로퍼티_문자열_숫자_정상반환_테스트() {
        jsonStringify(JSONCLASS("leebongho", 31)) shouldBe """ {"age":31,"name":"leebongho"} """.trim()
    }

    @Test
    fun 프로퍼티_Booealn_정상반환_테스트() {
        val jsonClass = JSONCLASS2(true)
        jsonStringify(jsonClass) shouldBe """ {"isMail":true} """.trim()
    }

    @Test
    fun 일반_문자열_정상반환_테스트() {
        jsonStringify("abc") shouldBe """ "abc" """.trim()
    }

    @Test
    fun 일반_Number_정상반환_테스트() {
        jsonStringify(10) shouldBe """ 10 """.trim()
        jsonStringify(10.1) shouldBe """ 10.1 """.trim()
        jsonStringify(-10) shouldBe """ -10 """.trim()
    }

    @Test
    fun 일반_Boolean_정상반환_테스트() {
        jsonStringify(true) shouldBe """ true """.trim()
        jsonStringify(false) shouldBe """ false """.trim()
    }

    @Test
    fun 기본_List_정상반환_테스트() {
        jsonStringify(listOf("a", "b")) shouldBe """ ["a","b"] """.trim()
    }

    @Test
    fun List_포함_프로퍼티_정상반환_테스트() {
        class IncludeList(val name: String, val age: Int, val isMail: Boolean, val hobbies: List<String>)

        jsonStringify(
            IncludeList("leebongho", 31, true, listOf("soccer", "baseball"))
        ) shouldBe """ {"age":31,"hobbies":["soccer","baseball"],"isMail":true,"name":"leebongho"} """.trim()

    }


}


class JSONCLASS(val name: String, val age: Int)

class JSONCLASS2(val isMail: Boolean)