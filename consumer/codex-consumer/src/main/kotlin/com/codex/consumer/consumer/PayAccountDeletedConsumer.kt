package com.codex.consumer.consumer

import com.codex.consumer.service.PayTerminateService
import com.codex.consumer.support.PayAccountDeletedDecoder
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class PayAccountDeletedConsumer(
    private val decoder: PayAccountDeletedDecoder,
    private val payTerminateService: PayTerminateService
) {

    @KafkaListener(
        topics = [PAY_ACCOUNT_DELETED_TOPIC],
        containerFactory = "byteArrayKafkaListenerContainerFactory"
    )
    fun consume(message: ByteArray, acknowledgment: Acknowledgment) {
        try {
            val envelope = decoder.decode(message)
            payTerminateService.handleTermination(
                payAccountId = envelope.payAccountId,
                reason = envelope.reason
            )
            acknowledgment.acknowledge()
        } catch (ex: Exception) {
            log.error("Failed to process pay-account deleted message", ex)
            throw ex
        }
    }

    companion object {
        const val PAY_ACCOUNT_DELETED_TOPIC = "pay-account.payaccount-deleted.v2"
        private val log = LoggerFactory.getLogger(PayAccountDeletedConsumer::class.java)
    }
}
