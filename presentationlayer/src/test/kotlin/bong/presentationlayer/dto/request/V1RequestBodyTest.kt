package bong.presentationlayer.dto.request

import bong.presentationlayer.dto.common.UserIdentifier
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class V1RequestBodyTest : FunSpec({

    val json = Json { prettyPrint = false }

    test("V1RequestBody는 JSON으로 정상적으로 직렬화된다") {
        // given
        val request = V1RequestBody(
            requestId = "test-tx-123",
            requestDateTime = "2025-11-04T10:00:00",
            userIdentifier = UserIdentifier.CI(value = "test-ci-value-12345")
        )

        // when
        val jsonString = json.encodeToString(request)

        // then
        jsonString shouldContain "api_transaction_id"
        jsonString shouldContain "test-tx-123"
        jsonString shouldContain "request_time"
        jsonString shouldContain "2025-11-04T10:00:00"
        jsonString shouldContain "ci"
        jsonString shouldContain "test-ci-value-12345"
    }

    test("V1RequestBody는 JSON에서 정상적으로 역직렬화된다") {
        // given
        val jsonString = """
            {
                "api_transaction_id": "test-tx-456",
                "request_time": "2025-11-04T11:00:00",
                "ci": "ci-value-67890"
            }
        """.trimIndent()

        // when
        val request = json.decodeFromString<V1RequestBody>(jsonString)

        // then
        request.requestId shouldBe "test-tx-456"
        request.requestDateTime shouldBe "2025-11-04T11:00:00"
        request.userIdentifier.value shouldBe "ci-value-67890"
    }

    test("V1RequestBody는 BaseRequestBody 인터페이스를 구현한다") {
        // given
        val request = V1RequestBody(
            requestId = "test-tx-789",
            requestDateTime = "2025-11-04T12:00:00",
            userIdentifier = UserIdentifier.CI(value = "ci-value-abc")
        )

        // when
        val baseRequest: BaseRequestBody = request

        // then
        baseRequest.requestId shouldBe "test-tx-789"
        baseRequest.requestDateTime shouldBe "2025-11-04T12:00:00"
    }
})
