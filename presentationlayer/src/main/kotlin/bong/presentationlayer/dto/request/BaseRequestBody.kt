package bong.presentationlayer.dto.request

import bong.presentationlayer.dto.common.UserIdentifier

/**
 * 모든 Request Body의 기본 인터페이스
 *
 * V1RequestBody, V2RequestBody 등 모든 요청 타입의 공통 필드를 정의합니다.
 *
 * ## 공통 필드
 * - **requestId**: API 트랜잭션 ID
 * - **requestDateTime**: 요청 시간
 * - **userIdentifier**: 사용자 식별 정보
 */
interface BaseRequestBody {
    val requestId: String
    val requestDateTime: String
    val userIdentifier: UserIdentifier
}
