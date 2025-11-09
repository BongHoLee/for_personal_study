package bong.presentationlayer.context

import bong.presentationlayer.domain.UserId
import bong.presentationlayer.dto.common.UserIdentifier
import bong.presentationlayer.dto.request.BaseRequestBody
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

/**
 * Request Scope Bean
 *
 * HTTP 요청의 컨텍스트 정보를 저장합니다.
 * Request scope이므로 각 요청마다 새로운 인스턴스가 생성되며,
 * 요청이 끝나면 자동으로 정리됩니다.
 *
 * ## 저장 정보
 * - **serviceId**: Request Header의 'service-id' 값
 * - **userId**: 회원 조회 성공 시 UserId (미가입자는 null)
 * - **requestBody**: TransactionHistory 로깅을 위한 Request Body
 *
 * ## 사용 시나리오
 * 1. HandlerInterceptor에서 Request Header에서 service-id를 읽어 저장
 * 2. UserIdResolver에서 UserId 조회 성공 시 저장
 * 3. Controller에서 RequestBody 저장
 * 4. ResponseBodyAdvice에서 service-id 헤더 추가 및 TransactionHistory 로깅
 */
@Component
@RequestScope
class RequestContext {
    /**
     * Request Header의 'service-id' 값
     *
     * null일 수 있으므로 사용 시 null 체크 필요
     */
    var serviceId: String? = null

    /**
     * 회원 조회 결과 UserId
     *
     * UserIdResolver.resolve() 성공 시 저장됨
     * 미가입자이거나 조회 전에는 null
     */
    var userId: UserId? = null

    /**
     * Request Body (TransactionHistory 로깅용)
     *
     * Controller에서 저장함
     */
    var requestBody: BaseRequestBody? = null
}
