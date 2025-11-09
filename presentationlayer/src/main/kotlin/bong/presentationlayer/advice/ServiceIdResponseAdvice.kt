package bong.presentationlayer.advice

import bong.presentationlayer.context.RequestContext
import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

/**
 * Service ID Response Advice
 *
 * 모든 API 응답에 'service-id' header를 자동으로 추가합니다.
 *
 * ## 처리 흐름
 * 1. RequestContext에서 service-id 조회
 * 2. Response Header에 'service-id' 추가
 * 3. Response Body는 그대로 반환 (변경 없음)
 *
 * ## 적용 범위
 * - @RestController가 붙은 모든 컨트롤러
 * - @ControllerAdvice의 GlobalExceptionHandler
 */
@ControllerAdvice
class ServiceIdResponseAdvice(
    private val requestContext: RequestContext
) : ResponseBodyAdvice<Any> {

    private val logger = LoggerFactory.getLogger(ServiceIdResponseAdvice::class.java)

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>
    ): Boolean {
        // 모든 응답에 대해 처리
        return true
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {
        // RequestContext에서 service-id 조회
        val serviceId = requestContext.serviceId

        if (serviceId != null) {
            // Response Header에 service-id 추가
            response.headers.add("service-id", serviceId)
            logger.debug("Added 'service-id' header to response: {}", serviceId)
        } else {
            logger.warn("No 'service-id' found in RequestContext for response")
        }

        // Body는 변경하지 않고 그대로 반환
        return body
    }
}
