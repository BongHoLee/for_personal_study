package bong.presentationlayer.dto.request

import bong.presentationlayer.dto.common.UserIdentifier
import bong.presentationlayer.dto.common.UserIdentifierAsStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * V1 API Request Body
 * CI만을 유저 식별 코드로 사용하는 초기 버전
 *
 * ## JSON 형식
 * ```json
 * {
 *   "api_transaction_id": "tx-123",
 *   "request_time": "2025-11-04T10:00:00",
 *   "ci": "ci-value-12345"
 * }
 * ```
 *
 * ## ci 필드의 특별한 처리
 * - 코드에서는 `UserIdentifier.CI` 타입으로 타입 안정성 확보 (V2와 동일한 타입 사용)
 * - JSON에서는 단순 문자열로 flatten되어 표현
 * - `@Serializable(with = UserIdentifierAsStringSerializer::class)`로 변환 처리
 *
 * ## V1과 V2의 차이
 * - V1: `"ci": "value"` (단순 문자열)
 * - V2: `"user_identifier": {"type": "CI", "value": "value"}` (객체 형태)
 * - 코드에서는 둘 다 `UserIdentifier.CI` 타입 사용
 */
@Serializable
data class V1RequestBody(
    @SerialName("api_transaction_id")
    override val requestId: String,

    @SerialName("request_time")
    override val requestDateTime: String,

    @SerialName("ci")
    @Serializable(with = UserIdentifierAsStringSerializer::class)
    override val userIdentifier: UserIdentifier.CI  // V1 특화 필드: CI 유저 식별 코드 (JSON에서는 단순 문자열로 flatten)

) : BaseRequestBody
