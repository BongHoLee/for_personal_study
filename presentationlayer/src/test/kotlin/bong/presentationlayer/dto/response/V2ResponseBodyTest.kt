package bong.presentationlayer.dto.response

import bong.presentationlayer.dto.common.UserIdentifier
import bong.presentationlayer.dto.request.V2RequestBody
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class V2ResponseBodyTest : FunSpec({

    val json = Json { prettyPrint = false }

    test("V2ResponseBody는 CI 타입과 함께 JSON으로 정상적으로 직렬화된다") {
        // given
        val response = V2ResponseBody(
            transactionId = "test-tx-v2-123",
            requestId = "test-tx-v2-123",
            requestDateTime = "2025-11-04T10:00:00",
            userIdentifier = UserIdentifier.CI(value = "ci-value-12345"),
            responseDateTime = "2025-11-04T10:00:01",
            statusCode = 200,
            data = "success"
        )

        // when
        val jsonString = json.encodeToString(response)

        // then
        jsonString shouldContain "transaction_id"
        jsonString shouldContain "api_transaction_id"
        jsonString shouldContain "test-tx-v2-123"
        jsonString shouldContain "request_time"
        jsonString shouldContain "user_identifier"
        jsonString shouldContain "CI"
        jsonString shouldContain "ci-value-12345"
        jsonString shouldContain "response_time"
        jsonString shouldContain "2025-11-04T10:00:01"
        jsonString shouldContain "status_code"
        jsonString shouldContain "200"
    }

    test("V2ResponseBody는 DI 타입과 함께 JSON으로 정상적으로 직렬화된다") {
        // given
        val response = V2ResponseBody(
            transactionId = "test-tx-v2-456",
            requestId = "test-tx-v2-456",
            requestDateTime = "2025-11-04T11:00:00",
            userIdentifier = UserIdentifier.DI(value = "di-value-67890"),
            responseDateTime = "2025-11-04T11:00:01",
            statusCode = 200,
            data = "success"
        )

        // when
        val jsonString = json.encodeToString(response)

        // then
        jsonString shouldContain "DI"
        jsonString shouldContain "di-value-67890"
    }

    test("V2ResponseBody는 Email 타입과 함께 JSON으로 정상적으로 직렬화된다") {
        // given
        val response = V2ResponseBody(
            transactionId = "test-tx-v2-789",
            requestId = "test-tx-v2-789",
            requestDateTime = "2025-11-04T12:00:00",
            userIdentifier = UserIdentifier.Email(value = "test@example.com"),
            responseDateTime = "2025-11-04T12:00:01",
            statusCode = 200,
            data = "success"
        )

        // when
        val jsonString = json.encodeToString(response)

        // then
        jsonString shouldContain "EMAIL"
        jsonString shouldContain "test@example.com"
    }

    test("V2ResponseBody는 CI 타입과 함께 JSON에서 정상적으로 역직렬화된다") {
        // given
        val jsonString = """
            {
                "transaction_id": "test-tx-v2-111",
                "api_transaction_id": "test-tx-v2-111",
                "request_time": "2025-11-04T13:00:00",
                "user_identifier": {
                    "type": "CI",
                    "value": "ci-value-test"
                },
                "response_time": "2025-11-04T13:00:01",
                "status_code": 200,
                "data": "success"
            }
        """.trimIndent()

        // when
        val response = json.decodeFromString<V2ResponseBody>(jsonString)

        // then
        response.transactionId shouldBe "test-tx-v2-111"
        response.requestId shouldBe "test-tx-v2-111"
        response.requestDateTime shouldBe "2025-11-04T13:00:00"
        response.userIdentifier.shouldBeInstanceOf<UserIdentifier.CI>()
        response.userIdentifier.value shouldBe "ci-value-test"
        response.responseDateTime shouldBe "2025-11-04T13:00:01"
        response.statusCode shouldBe 200
        response.data shouldBe "success"
    }

    test("V2ResponseBody는 DI 타입과 함께 JSON에서 정상적으로 역직렬화된다") {
        // given
        val jsonString = """
            {
                "transaction_id": "test-tx-v2-222",
                "api_transaction_id": "test-tx-v2-222",
                "request_time": "2025-11-04T14:00:00",
                "user_identifier": {
                    "type": "DI",
                    "value": "di-value-test"
                },
                "response_time": "2025-11-04T14:00:01",
                "status_code": 200,
                "data": "success"
            }
        """.trimIndent()

        // when
        val response = json.decodeFromString<V2ResponseBody>(jsonString)

        // then
        response.userIdentifier.shouldBeInstanceOf<UserIdentifier.DI>()
        response.userIdentifier.value shouldBe "di-value-test"
    }

    test("V2ResponseBody는 Email 타입과 함께 JSON에서 정상적으로 역직렬화된다") {
        // given
        val jsonString = """
            {
                "transaction_id": "test-tx-v2-333",
                "api_transaction_id": "test-tx-v2-333",
                "request_time": "2025-11-04T15:00:00",
                "user_identifier": {
                    "type": "EMAIL",
                    "value": "user@test.com"
                },
                "response_time": "2025-11-04T15:00:01",
                "status_code": 200,
                "data": "success"
            }
        """.trimIndent()

        // when
        val response = json.decodeFromString<V2ResponseBody>(jsonString)

        // then
        response.userIdentifier.shouldBeInstanceOf<UserIdentifier.Email>()
        response.userIdentifier.value shouldBe "user@test.com"
    }

    test("V2ResponseBody는 BaseResponseBody 인터페이스를 구현한다") {
        // given
        val response = V2ResponseBody(
            transactionId = "test-tx-v2-444",
            requestId = "test-tx-v2-444",
            requestDateTime = "2025-11-04T16:00:00",
            userIdentifier = UserIdentifier.CI(value = "ci-test"),
            responseDateTime = "2025-11-04T16:00:01",
            statusCode = 200,
            data = null
        )

        // when
        val baseResponse: BaseResponseBody = response

        // then
        baseResponse.transactionId shouldBe "test-tx-v2-444"
        baseResponse.requestId shouldBe "test-tx-v2-444"
        baseResponse.requestDateTime shouldBe "2025-11-04T16:00:00"
        baseResponse.responseDateTime shouldBe "2025-11-04T16:00:01"
        baseResponse.statusCode shouldBe 200
    }

    test("V2ResponseBody는 Request 필드를 모두 포함한다") {
        // given
        val response = V2ResponseBody(
            transactionId = "test-tx-v2-555",
            requestId = "test-tx-v2-555",
            requestDateTime = "2025-11-04T17:00:00",
            userIdentifier = UserIdentifier.DI(value = "di-test"),
            responseDateTime = "2025-11-04T17:00:01",
            statusCode = 200,
            data = "test"
        )

        // then - Request 필드들
        response.transactionId shouldBe "test-tx-v2-555"
        response.requestId shouldBe "test-tx-v2-555"
        response.requestDateTime shouldBe "2025-11-04T17:00:00"
        response.userIdentifier.value shouldBe "di-test"

        // Response 추가 필드들
        response.responseDateTime shouldBe "2025-11-04T17:00:01"
        response.statusCode shouldBe 200
    }

    test("V2ResponseBody는 V2RequestBody로부터 생성할 수 있다") {
        // given
        val request = V2RequestBody(
            requestId = "test-tx-v2-666",
            requestDateTime = "2025-11-04T18:00:00",
            userIdentifier = UserIdentifier.Email(value = "test@example.com")
        )

        // when
        val response = V2ResponseBody.from(
            request = request,
            responseDateTime = "2025-11-04T18:00:01",
            statusCode = 200,
            data = "processed"
        )

        // then
        response.transactionId shouldBe request.requestId
        response.requestId shouldBe request.requestId
        response.requestDateTime shouldBe request.requestDateTime
        response.userIdentifier shouldBe request.userIdentifier
        response.responseDateTime shouldBe "2025-11-04T18:00:01"
        response.statusCode shouldBe 200
        response.data shouldBe "processed"
    }

    test("V2ResponseBody는 에러 응답도 표현할 수 있다") {
        // given
        val errorResponse = V2ResponseBody(
            transactionId = "test-tx-v2-error",
            requestId = "test-tx-v2-error",
            requestDateTime = "2025-11-04T19:00:00",
            userIdentifier = UserIdentifier.CI(value = "ci-error"),
            responseDateTime = "2025-11-04T19:00:01",
            statusCode = 500,
            data = null
        )

        // when
        val jsonString = json.encodeToString(errorResponse)

        // then
        jsonString shouldContain "status_code"
        jsonString shouldContain "500"
        errorResponse.statusCode shouldBe 500
        errorResponse.data shouldBe null
    }
})
