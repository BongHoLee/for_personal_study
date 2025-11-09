package bong.presentationlayer.dto

import kotlinx.serialization.Serializable

/**
 * 모든 API 응답의 공통 래퍼 클래스
 * 에러 응답도 이 구조를 사용하여 statusCode로 구분
 *
 * @param T 응답 데이터의 타입
 * @param statusCode HTTP 상태 코드
 * @param message 응답 메시지
 * @param data 실제 응답 데이터 (성공 시) 또는 에러 정보 (실패 시)
 * @param timestamp 응답 생성 시간
 */
@Serializable
data class BaseResponse<T>(
    val statusCode: Int,
    val message: String,
    val data: T? = null,
    val timestamp: Long = System.currentTimeMillis()
)
