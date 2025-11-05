package bong.presentationlayer.dto

/**
 * 모든 Request의 기본 인터페이스
 * 공통 프로퍼티: requestId, requestDateTime
 */
interface BaseRequestBody {
    val requestId: String         // 요청 ID
    val requestDateTime: String   // 요청 시간
    val userIdentifier: UserIdentifier
}
