package bong.presentationlayer.dto.response

import bong.presentationlayer.dto.common.UserIdentifier

/**
 * 모든 Response Body의 기본 인터페이스
 *
 * Request의 필드를 포함하여 요청-응답 추적을 가능하게 하며,
 * 추가적으로 응답에 필요한 정보를 정의합니다.
 *
 * ## 공통 필드
 * - **transactionId**: API 트랜잭션 ID (필수)
 * - **statusCode**: HTTP 상태 코드 (필수)
 * - **userIdentifier**: 사용자 식별 정보 (필수)
 * - **requestId**: API 트랜잭션 ID (정상 응답 시 필수, 에러 응답 시 null)
 * - **requestDateTime**: 요청 시간 (정상 응답 시 필수, 에러 응답 시 null)
 * - **responseDateTime**: 응답 생성 시간 (정상 응답 시 필수, 에러 응답 시 null)
 *
 * ## 정상 응답 vs 에러 응답
 * - **정상 응답**: 모든 필드가 값을 가짐
 * - **에러 응답**: transactionId, statusCode, userIdentifier만 값을 가지고 나머지는 null
 *
 * ## 설계 의도
 * - Response는 Request의 모든 필드를 포함해야 함 (요청-응답 추적)
 * - transactionId는 요청-응답 추적을 위한 명시적 필드
 * - 정상/에러 응답 모두 동일한 ResponseBody 타입 사용
 * - Response와 Request는 독립적인 타입 (상속 관계 없음)
 * - JSON은 flatten되어 출력됨
 */
interface BaseResponseBody {
    val transactionId: String
    val statusCode: Int
    val userIdentifier: UserIdentifier
    val requestId: String?
    val requestDateTime: String?
    val responseDateTime: String?
}
