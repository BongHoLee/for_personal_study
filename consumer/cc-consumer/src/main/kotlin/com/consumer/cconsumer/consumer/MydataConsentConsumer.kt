package com.consumer.cconsumer.consumer

import com.consumer.cconsumer.message.model.ConsentMessage
import com.consumer.cconsumer.service.MydataTerminateService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class MydataConsentConsumer(
    private val mydataTerminateService: MydataTerminateService
) {
    private val logger = LoggerFactory.getLogger(MydataConsentConsumer::class.java)

    @KafkaListener(
        topics = ["mydata.consent.v1"],
        groupId = "cc-consumer-group",
        containerFactory = "jsonKafkaListenerContainerFactory"
    )
    fun consumeConsentMessage(
        @Payload message: ConsentMessage,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment
    ) {
        try {
            logger.info("Received consent message from topic: {}, partition: {}, offset: {}, payAccountId: {}, isRemove: {}", 
                topic, partition, offset, message.data.payAccountId, message.data.isRemove)

            if (message.data.isRemove) {
                logger.info("Processing termination for payAccountId: {}, reason: {}", 
                    message.data.payAccountId, message.data.deleteEventType)
                
                mydataTerminateService.processTermination(
                    payAccountId = message.data.payAccountId,
                    reason = message.data.deleteEventType
                )
                
                logger.info("Successfully processed termination for payAccountId: {}", message.data.payAccountId)
            } else {
                logger.debug("Skipping message with isRemove=false for payAccountId: {}", message.data.payAccountId)
            }

            acknowledgment.acknowledge()
        } catch (exception: Exception) {
            logger.error("Failed to process consent message for payAccountId: {}, error: {}", 
                message.data.payAccountId, exception.message, exception)
            // 에러 발생 시 acknowledge하지 않아 재처리됨
            throw exception
        }
    }
}