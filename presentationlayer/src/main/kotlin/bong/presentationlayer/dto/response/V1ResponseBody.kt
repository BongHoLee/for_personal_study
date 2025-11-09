package bong.presentationlayer.dto.response

import bong.presentationlayer.dto.common.UserIdentifier
import bong.presentationlayer.dto.common.UserIdentifierAsStringSerializer
import bong.presentationlayer.dto.request.V1RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * V1 API Response Body
 *
 * ## 정상 응답 JSON 형식 (flatten)
 * ```json
 * {
 *   "api_transaction_id": "tx-123",
 *   "request_time": "2025-11-04T10:00:00",
 *   "ci": "ci-value-12345",
 *   "response_time": "2025-11-04T10:00:01",
 *   "status_code": 200,
 *   "data": "success"
 * }
 * ```
 *
 * ## 에러 응답 JSON 형식
 * ```json
 * {
 *   "api_transaction_id": "tx-123",
 *   "status_code": 404,
 *   "ci": "ci-value-12345",
 *   "request_time": null,
 *   "response_time": null,
 *   "data": null
 * }
 * ```
 *
 * ## 설계 포인트
 * - 정상/에러 응답 모두 동일한 구조 사용
 * - 에러 응답: transactionId, statusCode, userIdentifier만 필수, 나머지는 null
 * - JSON은 flatten되어 중첩 없음
 * - ci 필드는 UserIdentifierAsStringSerializer로 문자열로 직렬화
 */
@Serializable
data class V1ResponseBody(
    @SerialName("status_code")
    override val statusCode: Int,

    @SerialName("ci")
    @Serializable(with = UserIdentifierAsStringSerializer::class)
    override val userIdentifier: UserIdentifier.CI?,

    @SerialName("api_transaction_id")
    override val requestId: String? = null,

    @SerialName("request_time")
    override val requestDateTime: String? = null,

    @SerialName("response_time")
    override val responseDateTime: String? = null,

    val data: String? = null
) : BaseResponseBody {
    companion object {
        /**
         * V1RequestBody로부터 정상 응답 생성
         */
        fun from(
            request: V1RequestBody,
            responseDateTime: String,
            statusCode: Int,
            data: String?
        ): V1ResponseBody {
            return V1ResponseBody(
                statusCode = statusCode,
                userIdentifier = request.userIdentifier,
                requestId = request.requestId,
                requestDateTime = request.requestDateTime,
                responseDateTime = responseDateTime,
                data = data
            )
        }

        /**
         * 에러 응답 생성
         * transactionId, statusCode, userIdentifier만 필수, 나머지는 null
         */
        fun error(
            requestId: String?,
            statusCode: Int,
            userIdentifier: UserIdentifier.CI
        ): V1ResponseBody {
            return V1ResponseBody(
                statusCode = statusCode,
                userIdentifier = userIdentifier,
                requestId = requestId,
                requestDateTime = null,
                responseDateTime = null,
                data = null
            )
        }
    }
}
