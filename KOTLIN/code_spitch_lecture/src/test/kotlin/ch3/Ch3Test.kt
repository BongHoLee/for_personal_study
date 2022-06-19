package ch3

import io.kotest.core.spec.style.AnnotationSpec

class Ch3Test: AnnotationSpec() {

    @Test
    fun testUser4Test() {
        val testUser4 = TestUser4("name", 1)

    }
}