package bong.presentationlayer.dto

import kotlinx.serialization.Serializable

/**
 * 에러 응답 데이터 구조
 * BaseResponse의 data 필드에 담겨 statusCode와 함께 응답됨
 *
 * @param errorCode 에러 코드 (예: "INVALID_REQUEST", "USER_NOT_FOUND")
 * @param errorMessage 에러 메시지
 * @param details 추가 에러 상세 정보 (선택)
 */
@Serializable
data class ErrorResponse(
    val errorCode: String,
    val errorMessage: String,
    val details: Map<String, String>? = null
)
