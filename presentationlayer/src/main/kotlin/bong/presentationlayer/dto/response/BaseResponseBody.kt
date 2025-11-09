package bong.presentationlayer.dto.response

import bong.presentationlayer.dto.common.UserIdentifier

/**
 * 모든 Response Body의 기본 인터페이스
 *
 * Request의 필드를 포함하여 요청-응답 추적을 가능하게 하며,
 * 추가적으로 응답에 필요한 정보를 정의합니다.
 *
 * ## 공통 필드
 * - **requestId**: API 트랜잭션 ID (요청과 동일)
 * - **requestDateTime**: 요청 시간 (요청과 동일)
 * - **userIdentifier**: 사용자 식별 정보 (요청과 동일)
 * - **responseDateTime**: 응답 생성 시간
 * - **statusCode**: HTTP 상태 코드
 *
 * ## 설계 의도
 * - Response는 Request의 모든 필드를 포함해야 함 (요청-응답 추적)
 * - Response와 Request는 독립적인 타입 (상속 관계 없음)
 * - JSON은 flatten되어 출력됨
 */
interface BaseResponseBody {
    val requestId: String
    val requestDateTime: String
    val userIdentifier: UserIdentifier
    val responseDateTime: String
    val statusCode: Int
}
