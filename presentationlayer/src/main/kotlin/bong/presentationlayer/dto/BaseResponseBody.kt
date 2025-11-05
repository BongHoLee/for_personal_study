package bong.presentationlayer.dto

/**
 * 모든 Response Body의 기본 인터페이스
 *
 * ApiMetadata를 상속하여 요청의 메타데이터를 포함하며,
 * 추가적으로 응답에 필요한 정보를 정의합니다.
 *
 * ## 공통 필드
 * - **requestId, requestDateTime, userIdentifier**: 요청 메타데이터 (ApiMetadata에서 상속)
 * - **responseDateTime**: 응답 생성 시간
 * - **statusCode**: HTTP 상태 코드
 *
 * ## 설계 의도
 * - Response는 Request의 모든 메타데이터를 포함해야 함 (요청-응답 추적)
 * - Response는 Request를 상속하는 것이 아니라, 같은 메타데이터를 공유함
 * - JSON은 flatten되어 출력됨
 */
interface BaseResponseBody : ApiMetadata {
    val responseDateTime: String
    val statusCode: Int
}
