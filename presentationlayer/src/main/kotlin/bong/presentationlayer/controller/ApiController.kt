package bong.presentationlayer.controller

import bong.presentationlayer.domain.UserId
import bong.presentationlayer.dto.request.ServiceRequest
import bong.presentationlayer.dto.request.V1RequestBody
import bong.presentationlayer.dto.request.V2RequestBody
import bong.presentationlayer.dto.response.ServiceResponse
import bong.presentationlayer.dto.response.V1ResponseBody
import bong.presentationlayer.dto.response.V2ResponseBody
import bong.presentationlayer.logging.TransactionHistoryLogger
import bong.presentationlayer.service.UserIdResolver
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.time.Instant

/**
 * API Controller
 *
 * ## 특징
 * - HttpServletRequest에 직접 의존하지 않음
 * - ServiceRequest를 통해 service-id와 body를 받음
 * - UserIdResolver를 사용하여 UserIdentifier → UserId 변환
 * - ServiceResponse를 반환하여 service-id를 Response에 전파
 * - TransactionHistoryLogger로 비동기 로깅
 */
@RestController
@RequestMapping("/api")
class ApiController(
    private val userIdResolver: UserIdResolver,
    private val transactionHistoryLogger: TransactionHistoryLogger
) {

    private val logger = LoggerFactory.getLogger(ApiController::class.java)

    /**
     * V1 API Endpoint
     */
    @PostMapping("/v1/process")
    fun processV1(
        @RequestHeader("service-id") serviceId: String,
        @RequestBody requestBody: V1RequestBody
    ): ServiceResponse<V1ResponseBody> {
        logger.info("Processing V1 request - serviceId: $serviceId, requestId: ${requestBody.requestId}")

        // 1. UserIdentifier → UserId 변환 (회원 검증)
        val userId: UserId = userIdResolver.resolve(requestBody.userIdentifier)
        logger.info("Resolved userId: $userId for userIdentifier: ${requestBody.userIdentifier.value}")

        // 2. 비즈니스 로직 처리 (여기서는 간단히 성공 응답)
        val responseBody = V1ResponseBody.from(
            request = requestBody,
            responseDateTime = Instant.now().toString(),
            statusCode = 200,
            data = "Processing completed for user: $userId"
        )

        // 3. ServiceResponse 생성
        val serviceRequest = ServiceRequest(serviceId, requestBody)
        val serviceResponse = ServiceResponse.from(serviceRequest, responseBody)

        // 4. Transaction History 비동기 로깅
        transactionHistoryLogger.log(serviceId, requestBody, responseBody)

        return serviceResponse
    }

    /**
     * V2 API Endpoint
     */
    @PostMapping("/v2/process")
    fun processV2(
        @RequestHeader("service-id") serviceId: String,
        @RequestBody requestBody: V2RequestBody
    ): ServiceResponse<V2ResponseBody> {
        logger.info("Processing V2 request - serviceId: $serviceId, requestId: ${requestBody.requestId}")

        // 1. UserIdentifier → UserId 변환 (회원 검증)
        val userId: UserId = userIdResolver.resolve(requestBody.userIdentifier)
        logger.info("Resolved userId: $userId for userIdentifier: ${requestBody.userIdentifier.value}")

        // 2. 비즈니스 로직 처리
        val responseBody = V2ResponseBody.from(
            request = requestBody,
            responseDateTime = Instant.now().toString(),
            statusCode = 200,
            data = "Processing completed for user: $userId"
        )

        // 3. ServiceResponse 생성
        val serviceRequest = ServiceRequest(serviceId, requestBody)
        val serviceResponse = ServiceResponse.from(serviceRequest, responseBody)

        // 4. Transaction History 비동기 로깅
        transactionHistoryLogger.log(serviceId, requestBody, responseBody)

        return serviceResponse
    }
}
