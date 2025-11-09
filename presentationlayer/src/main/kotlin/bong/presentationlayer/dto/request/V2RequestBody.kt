package bong.presentationlayer.dto.request

import bong.presentationlayer.dto.common.UserIdentifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * V2 API Request Body
 * CI, DI, Email 세 가지 유저 식별 코드를 지원하는 확장 버전
 * UserIdentifier 타입 계층을 사용하여 다양한 식별 방식 지원
 */
@Serializable
data class V2RequestBody(
    @SerialName("api_transaction_id")
    override val requestId: String,

    @SerialName("request_time")
    override val requestDateTime: String,

    @SerialName("user_identifier")
    override val userIdentifier: UserIdentifier  // V2 특화 필드: 다양한 유저 식별 타입 지원
) : BaseRequestBody
