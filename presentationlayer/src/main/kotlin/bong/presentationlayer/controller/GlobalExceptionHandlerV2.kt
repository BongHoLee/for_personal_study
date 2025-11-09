package bong.presentationlayer.controller

import bong.presentationlayer.context.RequestContext
import bong.presentationlayer.dto.response.V2ResponseBody
import bong.presentationlayer.exception.UserNotFoundException
import bong.presentationlayer.exception.factory.V2ApiErrorResponseFactory
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [ApiV2Controller::class])
@Order(Ordered.HIGHEST_PRECEDENCE)
class GlobalExceptionHandlerV2(
    private val requestContext: RequestContext,
    private val factory: V2ApiErrorResponseFactory
) {
    private fun serviceIdHeader(request: HttpServletRequest): String =
        request.getHeader("service-id") ?: requestContext.serviceId ?: "unknown"

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(
        ex: UserNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<V2ResponseBody> {
        val body = factory.userNotFound(ex.userIdentifier, ex.transactionId) as V2ResponseBody
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .header("service-id", serviceIdHeader(request))
            .body(body)
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException::class)
    fun handleNotReadable(
        ex: org.springframework.http.converter.HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<V2ResponseBody> {
        val body = factory.badRequest() as V2ResponseBody
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .header("service-id", serviceIdHeader(request))
            .body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<V2ResponseBody> {
        val body = factory.internalError() as V2ResponseBody
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .header("service-id", serviceIdHeader(request))
            .body(body)
    }
}

