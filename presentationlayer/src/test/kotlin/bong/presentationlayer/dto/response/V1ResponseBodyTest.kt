package bong.presentationlayer.dto.response

import bong.presentationlayer.dto.common.UserIdentifier
import bong.presentationlayer.dto.request.V1RequestBody
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class V1ResponseBodyTest : FunSpec({

    val json = Json { prettyPrint = false }

    test("V1ResponseBody는 JSON으로 정상적으로 직렬화된다") {
        // given
        val response = V1ResponseBody(
            requestId = "test-tx-123",
            requestDateTime = "2025-11-04T10:00:00",
            userIdentifier = UserIdentifier.CI(value = "test-ci-value-12345"),
            responseDateTime = "2025-11-04T10:00:01",
            statusCode = 200,
            data = "success"
        )

        // when
        val jsonString = json.encodeToString(response)

        // then
        jsonString shouldContain "api_transaction_id"
        jsonString shouldContain "test-tx-123"
        jsonString shouldContain "request_time"
        jsonString shouldContain "2025-11-04T10:00:00"
        jsonString shouldContain "ci"
        jsonString shouldContain "test-ci-value-12345"
        jsonString shouldContain "response_time"
        jsonString shouldContain "2025-11-04T10:00:01"
        jsonString shouldContain "status_code"
        jsonString shouldContain "200"
        jsonString shouldContain "success"
    }

    test("V1ResponseBody는 JSON에서 정상적으로 역직렬화된다") {
        // given
        val jsonString = """
            {
                "api_transaction_id": "test-tx-456",
                "request_time": "2025-11-04T11:00:00",
                "ci": "ci-value-67890",
                "response_time": "2025-11-04T11:00:01",
                "status_code": 200,
                "data": "success"
            }
        """.trimIndent()

        // when
        val response = json.decodeFromString<V1ResponseBody>(jsonString)

        // then
        response.requestId shouldBe "test-tx-456"
        response.requestDateTime shouldBe "2025-11-04T11:00:00"
        response.userIdentifier.value shouldBe "ci-value-67890"
        response.responseDateTime shouldBe "2025-11-04T11:00:01"
        response.statusCode shouldBe 200
        response.data shouldBe "success"
    }

    test("V1ResponseBody는 BaseResponseBody 인터페이스를 구현한다") {
        // given
        val response = V1ResponseBody(
            requestId = "test-tx-789",
            requestDateTime = "2025-11-04T12:00:00",
            userIdentifier = UserIdentifier.CI(value = "ci-value-abc"),
            responseDateTime = "2025-11-04T12:00:01",
            statusCode = 200,
            data = null
        )

        // when
        val baseResponse: BaseResponseBody = response

        // then
        baseResponse.requestId shouldBe "test-tx-789"
        baseResponse.requestDateTime shouldBe "2025-11-04T12:00:00"
        baseResponse.responseDateTime shouldBe "2025-11-04T12:00:01"
        baseResponse.statusCode shouldBe 200
    }

    test("V1ResponseBody는 Request 필드를 모두 포함한다") {
        // given
        val response = V1ResponseBody(
            requestId = "test-tx-999",
            requestDateTime = "2025-11-04T13:00:00",
            userIdentifier = UserIdentifier.CI(value = "ci-value-xyz"),
            responseDateTime = "2025-11-04T13:00:01",
            statusCode = 200,
            data = "test"
        )

        // then - Request 필드들
        response.requestId shouldBe "test-tx-999"
        response.requestDateTime shouldBe "2025-11-04T13:00:00"
        response.userIdentifier.value shouldBe "ci-value-xyz"

        // Response 추가 필드들
        response.responseDateTime shouldBe "2025-11-04T13:00:01"
        response.statusCode shouldBe 200
    }

    test("V1ResponseBody는 V1RequestBody로부터 생성할 수 있다") {
        // given
        val request = V1RequestBody(
            requestId = "test-tx-111",
            requestDateTime = "2025-11-04T14:00:00",
            userIdentifier = UserIdentifier.CI(value = "ci-value-111")
        )

        // when
        val response = V1ResponseBody.from(
            request = request,
            responseDateTime = "2025-11-04T14:00:01",
            statusCode = 200,
            data = "processed"
        )

        // then
        response.requestId shouldBe request.requestId
        response.requestDateTime shouldBe request.requestDateTime
        response.userIdentifier shouldBe request.userIdentifier
        response.responseDateTime shouldBe "2025-11-04T14:00:01"
        response.statusCode shouldBe 200
        response.data shouldBe "processed"
    }

    test("V1ResponseBody는 에러 응답도 표현할 수 있다") {
        // given
        val errorResponse = V1ResponseBody(
            requestId = "test-tx-error",
            requestDateTime = "2025-11-04T15:00:00",
            userIdentifier = UserIdentifier.CI(value = "ci-error"),
            responseDateTime = "2025-11-04T15:00:01",
            statusCode = 400,
            data = null
        )

        // when
        val jsonString = json.encodeToString(errorResponse)

        // then
        jsonString shouldContain "status_code"
        jsonString shouldContain "400"
        errorResponse.statusCode shouldBe 400
        errorResponse.data shouldBe null
    }
})
