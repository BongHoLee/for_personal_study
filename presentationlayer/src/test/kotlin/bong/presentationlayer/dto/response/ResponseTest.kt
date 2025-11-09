package bong.presentationlayer.dto

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ResponseTest : FunSpec({

    val json = Json { prettyPrint = false }

    context("BaseResponse") {
        test("V1Response 데이터와 함께 성공 응답을 직렬화한다") {
            // given
            val v1Data = V1Response(
                userId = "user-123",
                status = "SUCCESS",
                processedAt = "2025-11-04T10:00:00"
            )
            val response = BaseResponse(
                statusCode = 200,
                message = "Request processed successfully",
                data = v1Data
            )

            // when
            val jsonString = json.encodeToString(response)

            // then
            jsonString shouldContain "200"
            jsonString shouldContain "Request processed successfully"
            jsonString shouldContain "user-123"
            jsonString shouldContain "SUCCESS"
        }

        test("V2Response 데이터와 함께 성공 응답을 직렬화한다") {
            // given
            val v2Data = V2Response(
                userId = "user-456",
                identifierType = "CI",
                status = "SUCCESS",
                processedAt = "2025-11-04T11:00:00",
                additionalInfo = mapOf("source" to "mobile")
            )
            val response = BaseResponse(
                statusCode = 200,
                message = "V2 request processed",
                data = v2Data
            )

            // when
            val jsonString = json.encodeToString(response)

            // then
            jsonString shouldContain "user-456"
            jsonString shouldContain "CI"
            jsonString shouldContain "mobile"
        }

        test("ErrorResponse 데이터와 함께 에러 응답을 직렬화한다") {
            // given
            val errorData = ErrorResponse(
                errorCode = "INVALID_REQUEST",
                errorMessage = "Request validation failed",
                details = mapOf("field" to "ci", "reason" to "empty value")
            )
            val response = BaseResponse(
                statusCode = 400,
                message = "Bad Request",
                data = errorData
            )

            // when
            val jsonString = json.encodeToString(response)

            // then
            jsonString shouldContain "400"
            jsonString shouldContain "INVALID_REQUEST"
            jsonString shouldContain "Request validation failed"
            jsonString shouldContain "ci"
        }

        test("타임스탬프가 자동으로 생성된다") {
            // given
            val response = BaseResponse<String>(
                statusCode = 200,
                message = "OK",
                data = "test"
            )

            // then
            response.timestamp shouldNotBe 0L
        }

        test("data가 null일 수 있다") {
            // given
            val response = BaseResponse<String>(
                statusCode = 204,
                message = "No Content",
                data = null
            )

            // when
            val jsonString = json.encodeToString(response)

            // then
            jsonString shouldContain "204"
            jsonString shouldContain "No Content"
            response.data shouldBe null
        }
    }

    context("V1Response") {
        test("정상적으로 직렬화 및 역직렬화된다") {
            // given
            val v1Response = V1Response(
                userId = "user-789",
                status = "COMPLETED",
                processedAt = "2025-11-04T12:00:00"
            )

            // when
            val jsonString = json.encodeToString(v1Response)
            val decoded = json.decodeFromString<V1Response>(jsonString)

            // then
            decoded.userId shouldBe "user-789"
            decoded.status shouldBe "COMPLETED"
            decoded.processedAt shouldBe "2025-11-04T12:00:00"
        }
    }

    context("V2Response") {
        test("정상적으로 직렬화 및 역직렬화된다") {
            // given
            val v2Response = V2Response(
                userId = "user-999",
                identifierType = "EMAIL",
                status = "PENDING",
                processedAt = "2025-11-04T13:00:00",
                additionalInfo = mapOf("priority" to "high", "source" to "web")
            )

            // when
            val jsonString = json.encodeToString(v2Response)
            val decoded = json.decodeFromString<V2Response>(jsonString)

            // then
            decoded.userId shouldBe "user-999"
            decoded.identifierType shouldBe "EMAIL"
            decoded.status shouldBe "PENDING"
            decoded.processedAt shouldBe "2025-11-04T13:00:00"
            decoded.additionalInfo shouldBe mapOf("priority" to "high", "source" to "web")
        }

        test("additionalInfo가 null일 수 있다") {
            // given
            val v2Response = V2Response(
                userId = "user-111",
                identifierType = "DI",
                status = "SUCCESS",
                processedAt = "2025-11-04T14:00:00",
                additionalInfo = null
            )

            // when
            val jsonString = json.encodeToString(v2Response)
            val decoded = json.decodeFromString<V2Response>(jsonString)

            // then
            decoded.additionalInfo shouldBe null
        }
    }

    context("ErrorResponse") {
        test("정상적으로 직렬화 및 역직렬화된다") {
            // given
            val errorResponse = ErrorResponse(
                errorCode = "USER_NOT_FOUND",
                errorMessage = "User does not exist",
                details = mapOf("userId" to "unknown-123")
            )

            // when
            val jsonString = json.encodeToString(errorResponse)
            val decoded = json.decodeFromString<ErrorResponse>(jsonString)

            // then
            decoded.errorCode shouldBe "USER_NOT_FOUND"
            decoded.errorMessage shouldBe "User does not exist"
            decoded.details shouldBe mapOf("userId" to "unknown-123")
        }

        test("details가 null일 수 있다") {
            // given
            val errorResponse = ErrorResponse(
                errorCode = "INTERNAL_ERROR",
                errorMessage = "Something went wrong",
                details = null
            )

            // when
            val jsonString = json.encodeToString(errorResponse)
            val decoded = json.decodeFromString<ErrorResponse>(jsonString)

            // then
            decoded.details shouldBe null
        }
    }
})
