package bong.presentationlayer.controller

import bong.presentationlayer.context.RequestContext
import bong.presentationlayer.dto.common.UserIdentifier
import bong.presentationlayer.dto.response.V1ResponseBody
import bong.presentationlayer.dto.response.V2ResponseBody
import bong.presentationlayer.exception.UserNotFoundException
import jakarta.servlet.http.HttpServletRequest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Global Exception Handler
 *
 * 모든 예외를 처리하고 적절한 응답을 생성합니다.
 * V1/V2 API에 따라 각각 V1ResponseBody, V2ResponseBody로 에러 응답을 생성합니다.
 *
 * ## 에러 응답 규칙
 * - transactionId, statusCode, userIdentifier만 필수 값을 가짐
 * - 나머지 필드는 null로 설정
 * - V1: V1ResponseBody 사용 (UserIdentifier.CI만 가능)
 * - V2: V2ResponseBody 사용 (UserIdentifier.CI, DI, Email 모두 가능)
 */
@RestControllerAdvice
class GlobalExceptionHandler(
    private val requestContext: RequestContext
) {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    private val json = Json { prettyPrint = false; encodeDefaults = true }

    /**
     * UserNotFoundException 처리
     *
     * API 버전에 따라 V1ResponseBody 또는 V2ResponseBody로 응답
     */
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(
        ex: UserNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        logger.error("User not found: ${ex.userIdentifier.value}, transactionId: ${ex.transactionId}", ex)

        val requestPath = request.requestURI

        // API 버전 판단: /api/v1 또는 /api/v2
        val isV1 = requestPath.contains("/v1/")

        return if (isV1) {
            // V1 에러 응답 (ResponseBody 직접 반환)
            val errorBody = V1ResponseBody.error(
                transactionId = ex.transactionId,
                statusCode = 404,
                userIdentifier = ex.userIdentifier as UserIdentifier.CI
            )
            val jsonString = json.encodeToString(errorBody)
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonString)
        } else {
            // V2 에러 응답 (ResponseBody 직접 반환)
            val errorBody = V2ResponseBody.error(
                transactionId = ex.transactionId,
                statusCode = 404,
                userIdentifier = ex.userIdentifier
            )
            val jsonString = json.encodeToString(errorBody)
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonString)
        }
    }

    /**
     * 일반 예외 처리
     *
     * 요청 컨텍스트가 없어 간단한 에러 응답만 반환
     */
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<Map<String, Any>> {
        logger.error("Unexpected error occurred", ex)

        val errorResponse = mapOf(
            "statusCode" to 500,
            "message" to "Internal Server Error",
            "detail" to (ex.message ?: "Unknown error")
        )

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse)
    }
}
