package bong.presentationlayer.dto.response

/**
 * Service Response Wrapper
 *
 * Request에서 전달받은 'service-id'와 Response Body를 함께 포함하는 wrapper 클래스입니다.
 * ResponseAdvice에서 이 정보를 기반으로 HTTP Header에 service-id를 추가합니다.
 *
 * ## 설계 의도
 * - Request의 service-id를 Response에 전파
 * - HTTP Header 처리를 추상화
 * - TransactionHistory 로깅을 위한 정보 제공
 *
 * @param T Response Body 타입 (V1ResponseBody, V2ResponseBody 등)
 */
data class ServiceResponse<T : BaseResponseBody>(
    val serviceId: String,
    val body: T
) {
    init {
        require(serviceId.isNotBlank()) { "serviceId cannot be blank" }
    }

    companion object {
        /**
         * ServiceRequest와 ResponseBody로부터 ServiceResponse 생성
         */
        fun <REQ : bong.presentationlayer.dto.request.BaseRequestBody, RES : BaseResponseBody> from(
            request: bong.presentationlayer.dto.request.ServiceRequest<REQ>,
            responseBody: RES
        ): ServiceResponse<RES> {
            return ServiceResponse(
                serviceId = request.serviceId,
                body = responseBody
            )
        }
    }
}
