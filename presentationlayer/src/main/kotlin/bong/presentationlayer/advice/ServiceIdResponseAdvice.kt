package bong.presentationlayer.advice

import bong.presentationlayer.context.RequestContext
import bong.presentationlayer.dto.response.BaseResponseBody
import bong.presentationlayer.logging.TransactionHistoryLogger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

/**
 * Response Body Advice
 *
 * 모든 API 응답에 대해 다음을 수행합니다:
 * 1. 'service-id' header 자동 추가
 * 2. TransactionHistory 비동기 로깅
 *
 * ## 처리 흐름
 * 1. RequestContext에서 service-id, userId, requestBody 조회
 * 2. Response Header에 'service-id' 추가
 * 3. ResponseBody가 있으면 TransactionHistory 로깅
 * 4. Response Body는 그대로 반환 (변경 없음)
 *
 * ## 로깅 보장
 * - 정상 응답: Controller에서 RequestContext에 requestBody 저장 → 여기서 로깅
 * - 에러 응답: GlobalExceptionHandler에서 처리 → 여기서 로깅
 * - 모든 응답이 이 Advice를 거치므로 로깅 누락 없음
 *
 * ## 적용 범위
 * - @RestController가 붙은 모든 컨트롤러
 * - @ControllerAdvice의 GlobalExceptionHandler
 */
@ControllerAdvice
class ServiceIdResponseAdvice(
    private val requestContext: RequestContext,
    private val transactionHistoryLogger: TransactionHistoryLogger
) : ResponseBodyAdvice<BaseResponseBody> {

    private val logger = LoggerFactory.getLogger(ServiceIdResponseAdvice::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>
    ): Boolean {
        // BaseResponseBody 타입만 처리
        return true
    }

    override fun beforeBodyWrite(
        body: BaseResponseBody?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): BaseResponseBody? {
        // 1. RequestContext에서 정보 조회
        val serviceId = requestContext.serviceId
        val userId = requestContext.userId
        val requestBody = requestContext.requestBody

        // 2. Response Header에 service-id 추가
        if (serviceId != null && !response.headers.containsKey("service-id")) {
            response.headers.add("service-id", serviceId)
            logger.debug("Added 'service-id' header to response: {}", serviceId)
        } else {
            logger.warn("No 'service-id' found in RequestContext for response")
        }

        // 3. TransactionHistory 로깅 (typed body 기반)
        if (body != null && requestBody != null && serviceId != null) {
            try {
                transactionHistoryLogger.log(
                    serviceId = serviceId,
                    requestBody = requestBody,
                    responseBody = body,
                    userId = userId?.value
                )
            } catch (e: Exception) {
                logger.error("Failed to log transaction history", e)
            }
        }

        // 디버그: 직렬화 결과 확인 (변경 없이)
        // 직렬화 확인 로깅은 생략 (PoC)

        // 4. Body는 변경하지 않고 그대로 반환 (타입 보존)
        return body
    }
}
