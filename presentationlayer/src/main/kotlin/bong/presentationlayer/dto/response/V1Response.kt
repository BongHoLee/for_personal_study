package bong.presentationlayer.dto

import kotlinx.serialization.Serializable

/**
 * V1 API 성공 응답 데이터
 * V1RequestBody와 쌍을 이루는 응답 구조
 */
@Serializable
data class V1Response(
    val userId: String,           // 처리된 유저 ID (CI 기반)
    val status: String,            // 처리 상태
    val processedAt: String        // 처리 시간
)
