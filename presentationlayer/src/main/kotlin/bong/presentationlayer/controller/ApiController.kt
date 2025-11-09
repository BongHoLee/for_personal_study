package bong.presentationlayer.controller

import bong.presentationlayer.context.RequestContext
import bong.presentationlayer.domain.UserId
import bong.presentationlayer.dto.request.V1RequestBody
import bong.presentationlayer.dto.request.V2RequestBody
import bong.presentationlayer.dto.response.V1ResponseBody
import bong.presentationlayer.dto.response.V2ResponseBody
import bong.presentationlayer.logging.TransactionHistoryLogger
import bong.presentationlayer.service.UserIdResolver
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

/**
 * API Controller
 *
 * ## 특징
 * - HttpServletRequest에 직접 의존하지 않음
 * - UserIdResolver를 사용하여 UserIdentifier → UserId 변환
 * - ResponseBody를 직접 반환 (wrapper 없음)
 * - service-id는 RequestContext와 ResponseBodyAdvice를 통해 자동 처리
 * - TransactionHistoryLogger로 비동기 로깅
 */
@RestController
@RequestMapping("/api")
class ApiController(
    private val userIdResolver: UserIdResolver,
    private val transactionHistoryLogger: TransactionHistoryLogger,
    private val requestContext: RequestContext
) {

    private val logger = LoggerFactory.getLogger(ApiController::class.java)
    private val json = Json { prettyPrint = false; encodeDefaults = true }

    /**
     * V1 API Endpoint
     */
    @PostMapping("/v1/process", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun processV1(
        @RequestBody requestBody: V1RequestBody
    ): ResponseEntity<String> {
        val serviceId = requestContext.serviceId ?: "unknown"
        logger.info("Processing V1 request - serviceId: $serviceId, requestId: ${requestBody.requestId}")

        // 1. UserIdentifier → UserId 변환 (회원 검증)
        val userId: UserId = userIdResolver.resolve(requestBody.userIdentifier, requestBody.requestId)
        logger.info("Resolved userId: $userId for userIdentifier: ${requestBody.userIdentifier.value}")

        // 2. 비즈니스 로직 처리 (여기서는 간단히 성공 응답)
        val responseBody = V1ResponseBody.from(
            request = requestBody,
            responseDateTime = Instant.now().toString(),
            statusCode = 200,
            data = "Processing completed for user: $userId"
        )

        // 3. Transaction History 비동기 로깅
        transactionHistoryLogger.log(serviceId, requestBody, responseBody)

        // 4. JSON 직렬화 후 반환 (ResponseBody 직접 반환)
        val jsonString = json.encodeToString(responseBody)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonString)
    }

    /**
     * V2 API Endpoint
     */
    @PostMapping("/v2/process", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun processV2(
        @RequestBody requestBody: V2RequestBody
    ): ResponseEntity<String> {
        val serviceId = requestContext.serviceId ?: "unknown"
        logger.info("Processing V2 request - serviceId: $serviceId, requestId: ${requestBody.requestId}")

        // 1. UserIdentifier → UserId 변환 (회원 검증)
        val userId: UserId = userIdResolver.resolve(requestBody.userIdentifier, requestBody.requestId)
        logger.info("Resolved userId: $userId for userIdentifier: ${requestBody.userIdentifier.value}")

        // 2. 비즈니스 로직 처리
        val responseBody = V2ResponseBody.from(
            request = requestBody,
            responseDateTime = Instant.now().toString(),
            statusCode = 200,
            data = "Processing completed for user: $userId"
        )

        // 3. Transaction History 비동기 로깅
        transactionHistoryLogger.log(serviceId, requestBody, responseBody)

        // 4. JSON 직렬화 후 반환 (ResponseBody 직접 반환)
        val jsonString = json.encodeToString(responseBody)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonString)
    }
}
