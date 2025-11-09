package bong.presentationlayer.controller

import bong.presentationlayer.dto.response.ServiceResponse
import bong.presentationlayer.dto.response.V1ResponseBody
import bong.presentationlayer.dto.response.V2ResponseBody
import bong.presentationlayer.exception.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

/**
 * Global Exception Handler
 *
 * 모든 예외를 처리하고 적절한 응답을 생성합니다.
 * UserNotFoundException의 경우 '회원 아님' 에러 응답을 생성합니다.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * UserNotFoundException 처리
     */
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(ex: UserNotFoundException): ResponseEntity<Map<String, Any>> {
        logger.error("User not found: ${ex.userIdentifier.value}", ex)

        val errorResponse = mapOf(
            "statusCode" to 404,
            "message" to "회원 아님",
            "userIdentifier" to ex.userIdentifier.value,
            "timestamp" to Instant.now().toString()
        )

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(errorResponse)
    }

    /**
     * 일반 예외 처리
     */
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<Map<String, Any>> {
        logger.error("Unexpected error occurred", ex)

        val errorResponse = mapOf(
            "statusCode" to 500,
            "message" to "Internal Server Error",
            "detail" to (ex.message ?: "Unknown error"),
            "timestamp" to Instant.now().toString()
        )

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse)
    }
}
