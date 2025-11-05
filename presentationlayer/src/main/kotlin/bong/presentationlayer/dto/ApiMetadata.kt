package bong.presentationlayer.dto

/**
 * API 호출의 공통 메타데이터
 *
 * Request와 Response 모두에서 사용되는 공통 정보를 정의합니다.
 * 이 인터페이스를 통해 Request와 Response가 같은 식별 정보를 공유한다는 것을 명시적으로 표현합니다.
 *
 * ## 설계 의도
 * - Request와 Response는 "is-a" 관계가 아닌 "shares metadata" 관계
 * - 동일한 API 호출 컨텍스트를 공유하는 것을 타입으로 표현
 * - 공통 메타데이터를 처리하는 유틸리티 함수 작성 가능
 *
 * ## 공통 필드
 * - **requestId**: API 트랜잭션 ID (요청-응답 추적용)
 * - **requestDateTime**: 요청 시간
 * - **userIdentifier**: 사용자 식별 정보 (CI, DI, Email 등)
 */
interface ApiMetadata {
    val requestId: String
    val requestDateTime: String
    val userIdentifier: UserIdentifier
}
