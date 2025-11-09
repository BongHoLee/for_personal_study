package bong.presentationlayer.config

import bong.presentationlayer.interceptor.RequestContextInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Spring MVC 설정
 *
 * Interceptor 등록 및 기타 MVC 설정을 관리합니다.
 */
@Configuration
class WebMvcConfig(
    private val requestContextInterceptor: RequestContextInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(requestContextInterceptor)
            .addPathPatterns("/api/**")
    }
}
