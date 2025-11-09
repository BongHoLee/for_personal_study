package bong.presentationlayer.dto.response

import bong.presentationlayer.dto.common.UserIdentifier

/**
 * 모든 Response Body의 공통 인터페이스
 *
 * 스펙 3항에 따라 응답 공통 스키마를 정의한다.
 * - 공통 프로퍼티: api_transaction_id, request_time, response_time, status_code, user_identifier(or ci), data
 * - 에러 시 값 유지: api_transaction_id(상황에 따라 null 가능), status_code, user_identifier(or ci)
 */
interface BaseResponseBody {
    val statusCode: Int
    val userIdentifier: UserIdentifier?   // 파싱 오류(400) 시 null 허용
    val requestId: String?                // api_transaction_id
    val requestDateTime: String?          // request_time
    val responseDateTime: String?         // response_time
}
