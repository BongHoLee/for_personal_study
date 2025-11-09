package bong.presentationlayer.dto.request

import bong.presentationlayer.dto.common.UserIdentifier
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class V2RequestBodyTest : FunSpec({

    val json = Json { prettyPrint = false }

    test("V2RequestBody는 CI 타입과 함께 JSON으로 정상적으로 직렬화된다") {
        // given
        val request = V2RequestBody(
            requestId = "test-tx-v2-123",
            requestDateTime = "2025-11-04T10:00:00",
            userIdentifier = UserIdentifier.CI(value = "ci-value-12345")
        )

        // when
        val jsonString = json.encodeToString(request)

        // then
        jsonString shouldContain "api_transaction_id"
        jsonString shouldContain "test-tx-v2-123"
        jsonString shouldContain "request_time"
        jsonString shouldContain "user_identifier"
        jsonString shouldContain "CI"
        jsonString shouldContain "ci-value-12345"
    }

    test("V2RequestBody는 DI 타입과 함께 JSON으로 정상적으로 직렬화된다") {
        // given
        val request = V2RequestBody(
            requestId = "test-tx-v2-456",
            requestDateTime = "2025-11-04T11:00:00",
            userIdentifier = UserIdentifier.DI(value = "di-value-67890")
        )

        // when
        val jsonString = json.encodeToString(request)

        // then
        jsonString shouldContain "DI"
        jsonString shouldContain "di-value-67890"
    }

    test("V2RequestBody는 Email 타입과 함께 JSON으로 정상적으로 직렬화된다") {
        // given
        val request = V2RequestBody(
            requestId = "test-tx-v2-789",
            requestDateTime = "2025-11-04T12:00:00",
            userIdentifier = UserIdentifier.Email(value = "test@example.com")
        )

        // when
        val jsonString = json.encodeToString(request)

        // then
        jsonString shouldContain "EMAIL"
        jsonString shouldContain "test@example.com"
    }

    test("V2RequestBody는 CI 타입과 함께 JSON에서 정상적으로 역직렬화된다") {
        // given
        val jsonString = """
            {
                "api_transaction_id": "test-tx-v2-111",
                "request_time": "2025-11-04T13:00:00",
                "user_identifier": {
                    "type": "CI",
                    "value": "ci-value-test"
                }
            }
        """.trimIndent()

        // when
        val request = json.decodeFromString<V2RequestBody>(jsonString)

        // then
        request.requestId shouldBe "test-tx-v2-111"
        request.requestDateTime shouldBe "2025-11-04T13:00:00"
        request.userIdentifier.shouldBeInstanceOf<UserIdentifier.CI>()
        request.userIdentifier.value shouldBe "ci-value-test"
    }

    test("V2RequestBody는 DI 타입과 함께 JSON에서 정상적으로 역직렬화된다") {
        // given
        val jsonString = """
            {
                "api_transaction_id": "test-tx-v2-222",
                "request_time": "2025-11-04T14:00:00",
                "user_identifier": {
                    "type": "DI",
                    "value": "di-value-test"
                }
            }
        """.trimIndent()

        // when
        val request = json.decodeFromString<V2RequestBody>(jsonString)

        // then
        request.userIdentifier.shouldBeInstanceOf<UserIdentifier.DI>()
        request.userIdentifier.value shouldBe "di-value-test"
    }

    test("V2RequestBody는 Email 타입과 함께 JSON에서 정상적으로 역직렬화된다") {
        // given
        val jsonString = """
            {
                "api_transaction_id": "test-tx-v2-333",
                "request_time": "2025-11-04T15:00:00",
                "user_identifier": {
                    "type": "EMAIL",
                    "value": "user@test.com"
                }
            }
        """.trimIndent()

        // when
        val request = json.decodeFromString<V2RequestBody>(jsonString)

        // then
        request.userIdentifier.shouldBeInstanceOf<UserIdentifier.Email>()
        request.userIdentifier.value shouldBe "user@test.com"
    }

    test("V2RequestBody는 BaseRequestBody 인터페이스를 구현한다") {
        // given
        val request = V2RequestBody(
            requestId = "test-tx-v2-444",
            requestDateTime = "2025-11-04T16:00:00",
            userIdentifier = UserIdentifier.CI(value = "ci-test")
        )

        // when
        val baseRequest: BaseRequestBody = request

        // then
        baseRequest.requestId shouldBe "test-tx-v2-444"
        baseRequest.requestDateTime shouldBe "2025-11-04T16:00:00"
    }
})
