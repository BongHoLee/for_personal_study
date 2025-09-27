package com.codex.consumer.consumer

import com.codex.consumer.model.ConsentMessage
import com.codex.consumer.service.MydataTerminateService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class MydataConsentConsumer(
    private val mydataTerminateService: MydataTerminateService
) {

    @KafkaListener(
        topics = [MYDATA_CONSENT_TOPIC],
        containerFactory = "consentKafkaListenerContainerFactory"
    )
    fun consume(message: ConsentMessage, acknowledgment: Acknowledgment) {
        try {
            if (!message.data.isRemove) {
                log.debug("Skip message for payAccountId={} because is_remove is false", message.data.payAccountId)
                acknowledgment.acknowledge()
                return
            }

            mydataTerminateService.handleTermination(
                payAccountId = message.data.payAccountId,
                reason = message.data.deleteEventType
            )
            acknowledgment.acknowledge()
        } catch (ex: Exception) {
            log.error("Failed to process mydata consent message for payAccountId={}", message.data.payAccountId, ex)
            throw ex
        }
    }

    companion object {
        const val MYDATA_CONSENT_TOPIC = "mydata.consent.v1"
        private val log = LoggerFactory.getLogger(MydataConsentConsumer::class.java)
    }
}
