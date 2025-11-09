package bong.presentationlayer.dto.response

import bong.presentationlayer.dto.common.UserIdentifier
import bong.presentationlayer.dto.request.V2RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * V2 API Response Body
 *
 * ## JSON 형식 (flatten)
 * ```json
 * {
 *   "api_transaction_id": "tx-123",
 *   "request_time": "2025-11-04T10:00:00",
 *   "user_identifier": {
 *     "type": "CI",
 *     "value": "ci-value-12345"
 *   },
 *   "response_time": "2025-11-04T10:00:01",
 *   "status_code": 200,
 *   "data": "success"
 * }
 * ```
 *
 * ## 설계 포인트
 * - Request의 모든 필드를 포함 (요청-응답 추적)
 * - JSON은 flatten되어 중첩 없음 (user_identifier는 하나의 필드)
 * - user_identifier는 CI, DI, Email 모두 지원
 */
@Serializable
data class V2ResponseBody(
    @SerialName("api_transaction_id")
    override val requestId: String,

    @SerialName("request_time")
    override val requestDateTime: String,

    @SerialName("user_identifier")
    override val userIdentifier: UserIdentifier,

    @SerialName("response_time")
    override val responseDateTime: String,

    @SerialName("status_code")
    override val statusCode: Int,

    val data: String?
) : BaseResponseBody {
    companion object {
        /**
         * V2RequestBody로부터 Response 생성
         */
        fun from(
            request: V2RequestBody,
            responseDateTime: String,
            statusCode: Int,
            data: String?
        ): V2ResponseBody {
            return V2ResponseBody(
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
