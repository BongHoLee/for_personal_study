package bong.presentationlayer.dto

import kotlinx.serialization.Serializable

/**
 * V2 API 성공 응답 데이터
 * V2RequestBody와 쌍을 이루는 응답 구조
 */
@Serializable
data class V2Response(
    val userId: String,                // 처리된 유저 ID
    val identifierType: String,        // 사용된 식별 타입 (CI/DI/EMAIL)
    val status: String,                // 처리 상태
    val processedAt: String,           // 처리 시간
    val additionalInfo: Map<String, String>? = null  // 추가 정보 (선택)
)
