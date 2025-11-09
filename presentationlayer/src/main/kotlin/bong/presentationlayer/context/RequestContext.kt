package bong.presentationlayer.context

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
 *
 * ## 사용 시나리오
 * 1. HandlerInterceptor에서 Request Header에서 service-id를 읽어 저장
 * 2. Controller에서 필요 시 조회 가능
 * 3. ResponseBodyAdvice에서 Response Header에 service-id 추가 시 사용
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
}
