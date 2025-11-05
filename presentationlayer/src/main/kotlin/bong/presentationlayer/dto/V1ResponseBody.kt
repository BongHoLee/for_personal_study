package bong.presentationlayer.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * V1 API Response Body
 *
 * ## JSON 형식 (flatten)
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
 * ## 설계 포인트
 * - Request의 모든 필드를 포함 (요청-응답 추적)
 * - JSON은 flatten되어 중첩 없음
 * - ci 필드는 UserIdentifierAsStringSerializer로 문자열로 직렬화
 */
@Serializable
data class V1ResponseBody(
    @SerialName("api_transaction_id")
    override val requestId: String,

    @SerialName("request_time")
    override val requestDateTime: String,

    @SerialName("ci")
    @Serializable(with = UserIdentifierAsStringSerializer::class)
    override val userIdentifier: UserIdentifier.CI,

    @SerialName("response_time")
    override val responseDateTime: String,

    @SerialName("status_code")
    override val statusCode: Int,

    val data: String?
) : BaseResponseBody {
    companion object {
        /**
         * V1RequestBody로부터 Response 생성
         */
        fun from(
            request: V1RequestBody,
            responseDateTime: String,
            statusCode: Int,
            data: String?
        ): V1ResponseBody {
            return V1ResponseBody(
                requestId = request.requestId,
                requestDateTime = request.requestDateTime,
                userIdentifier = request.userIdentifier,
                responseDateTime = responseDateTime,
                statusCode = statusCode,
                data = data
            )
        }
    }
}
