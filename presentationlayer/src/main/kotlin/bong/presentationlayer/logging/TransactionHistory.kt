package bong.presentationlayer.logging

import bong.presentationlayer.dto.request.BaseRequestBody
import bong.presentationlayer.dto.response.BaseResponseBody
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Transaction History Logger
 *
 * Request와 Response를 비동기적으로 로깅합니다.
 * 실제 환경에서는 DB나 별도 로깅 시스템에 저장할 수 있습니다.
 *
 * ## 비동기 처리
 * - @Async 어노테이션으로 별도 스레드에서 실행
 * - 요청 처리 성능에 영향을 주지 않음
 */
@Component
class TransactionHistoryLogger {

    private val logger = LoggerFactory.getLogger(TransactionHistoryLogger::class.java)
    private val json = Json { prettyPrint = true }

    /**
     * Transaction History 로깅 (비동기)
     *
     * @param serviceId 서비스 ID
     * @param requestBody Request Body
     * @param responseBody Response Body
     */
    @Async
    fun log(
        serviceId: String,
        requestBody: BaseRequestBody,
        responseBody: BaseResponseBody
    ) {
        try {
            val history = TransactionHistory(
                timestamp = Instant.now().toString(),
                serviceId = serviceId,
                requestId = requestBody.requestId,
                requestDateTime = requestBody.requestDateTime,
                responseDateTime = responseBody.responseDateTime ?: "N/A",
                statusCode = responseBody.statusCode,
                userIdentifier = requestBody.userIdentifier.value
            )

            logger.info(
                """
                |
                |========== Transaction History ==========
                |Timestamp: ${history.timestamp}
                |Service ID: ${history.serviceId}
                |Request ID: ${history.requestId}
                |Request Time: ${history.requestDateTime}
                |Response Time: ${history.responseDateTime}
                |Status Code: ${history.statusCode}
                |User Identifier: ${history.userIdentifier}
                |========================================
                |
                """.trimMargin()
            )
        } catch (e: Exception) {
            logger.error("Failed to log transaction history", e)
        }
    }
}

/**
 * Transaction History 데이터 모델
 */
data class TransactionHistory(
    val timestamp: String,
    val serviceId: String,
    val requestId: String,
    val requestDateTime: String,
    val responseDateTime: String,
    val statusCode: Int,
    val userIdentifier: String
)
