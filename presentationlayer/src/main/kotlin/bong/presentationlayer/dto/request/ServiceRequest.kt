package bong.presentationlayer.dto.request

/**
 * Service Request Wrapper
 *
 * HTTP Header의 'service-id'와 Request Body를 함께 포함하는 wrapper 클래스입니다.
 * Controller는 HttpServletRequest에 직접 의존하지 않고 이 클래스를 통해 필요한 정보를 받습니다.
 *
 * ## 설계 의도
 * - Controller가 서블릿 API에 의존하지 않도록 추상화
 * - Header 정보와 Body 정보를 타입 안전하게 전달
 * - Interceptor에서 변환 처리
 *
 * @param T Request Body 타입 (V1RequestBody, V2RequestBody 등)
 */
data class ServiceRequest<T : BaseRequestBody>(
    val serviceId: String,
    val body: T
) {
    init {
        require(serviceId.isNotBlank()) { "serviceId cannot be blank" }
    }
}
