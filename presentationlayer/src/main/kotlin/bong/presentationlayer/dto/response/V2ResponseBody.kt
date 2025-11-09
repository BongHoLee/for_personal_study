package bong.presentationlayer.dto.response

import bong.presentationlayer.dto.common.UserIdentifier
import bong.presentationlayer.dto.request.V2RequestBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * V2 API Response Body
 *
 * ## 정상 응답 JSON 형식 (flatten)
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
 * ## 에러 응답 JSON 형식
 * ```json
 * {
 *   "status_code": 404,
 *   "user_identifier": {
 *     "type": "CI",
 *     "value": "ci-value-12345"
 *   },
 *   "api_transaction_id": null,
 *   "request_time": null,
 *   "response_time": null,
 *   "data": null
 * }
 * ```
 *
 * ## 설계 포인트
 * - 정상/에러 응답 모두 동일한 구조 사용
 * - 에러 응답: transactionId, statusCode, userIdentifier만 필수, 나머지는 null
 * - JSON은 flatten되어 중첩 없음 (user_identifier는 하나의 필드)
 * - user_identifier는 CI, DI, Email 모두 지원
 */
@Serializable
data class V2ResponseBody(
    @SerialName("status_code")
    override val statusCode: Int,

    @SerialName("user_identifier")
    override val userIdentifier: UserIdentifier?,

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
         * V2RequestBody로부터 정상 응답 생성
         */
        fun from(
            request: V2RequestBody,
            responseDateTime: String,
            statusCode: Int,
            data: String?
        ): V2ResponseBody {
            return V2ResponseBody(
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
            userIdentifier: UserIdentifier
        ): V2ResponseBody {
            return V2ResponseBody(
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
