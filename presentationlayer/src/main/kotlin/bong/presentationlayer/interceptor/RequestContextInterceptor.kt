package bong.presentationlayer.interceptor

import bong.presentationlayer.context.RequestContext
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

/**
 * Request Context Interceptor
 *
 * HTTP 요청의 Header에서 'service-id'를 추출하여 RequestContext에 저장합니다.
 *
 * ## 처리 흐름
 * 1. preHandle: Request Header에서 'service-id' 추출
 * 2. RequestContext에 저장
 * 3. Controller 및 ResponseBodyAdvice에서 사용 가능
 */
@Component
class RequestContextInterceptor(
    private val requestContext: RequestContext
) : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(RequestContextInterceptor::class.java)

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val serviceId = request.getHeader("service-id")

        if (serviceId != null) {
            requestContext.serviceId = serviceId
            logger.debug("RequestContext initialized with serviceId: {}", serviceId)
        } else {
            logger.warn("Request without 'service-id' header: {} {}", request.method, request.requestURI)
        }

        return true
    }
}
