import kotlin.test.Test

class JsonStringifyTest {

    @Test
    fun test() {
        val json = stringify(JsonDataClass(100, "leebongho"))

    }
}

data class JsonDataClass(
    val id: Int,
    val name: String
)