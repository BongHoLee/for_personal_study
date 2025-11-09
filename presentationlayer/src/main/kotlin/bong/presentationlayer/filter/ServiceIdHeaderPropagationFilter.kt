package bong.presentationlayer.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 모든 /api 요청에 대해 요청 헤더의 `service-id`를 응답 헤더로 전파한다.
 * 예외(400 등)로 컨트롤러/어드바이스가 실행되지 않는 경우를 대비한 안전장치.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class ServiceIdHeaderPropagationFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val serviceId = request.getHeader("service-id")
        try {
            filterChain.doFilter(request, response)
        } finally {
            if (!response.containsHeader("service-id") && serviceId != null) {
                response.addHeader("service-id", serviceId)
            }
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val uri = request.requestURI ?: return true
        return !uri.startsWith("/api/")
    }
}

