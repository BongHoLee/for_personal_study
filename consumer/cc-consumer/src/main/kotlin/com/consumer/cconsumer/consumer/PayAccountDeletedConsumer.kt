package com.consumer.cconsumer.consumer

import com.consumer.cconsumer.message.avro.PayAccountDeletedEnvelop
import com.consumer.cconsumer.service.PayTerminateService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class PayAccountDeletedConsumer(
    private val payTerminateService: PayTerminateService
) {
    private val logger = LoggerFactory.getLogger(PayAccountDeletedConsumer::class.java)

    @KafkaListener(
        topics = ["pay-account.payaccount-deleted.v2"],
        groupId = "cc-consumer-group",
        containerFactory = "avroKafkaListenerContainerFactory"
    )
    fun consumePayAccountDeleted(
        @Payload envelope: PayAccountDeletedEnvelop,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
        acknowledgment: Acknowledgment
    ) {
        try {
            logger.info("Received pay account deleted message from topic: {}, partition: {}, offset: {}, payAccountId: {}, uuid: {}", 
                topic, partition, offset, envelope.payAccountId, envelope.uuid)

            logger.info("Processing termination for payAccountId: {}, reason: {}", 
                envelope.payAccountId, envelope.reason)
            
            payTerminateService.processTermination(
                payAccountId = envelope.payAccountId,
                reason = envelope.reason.toString()
            )
            
            logger.info("Successfully processed termination for payAccountId: {}", envelope.payAccountId)

            acknowledgment.acknowledge()
        } catch (exception: Exception) {
            logger.error("Failed to process pay account deleted message for payAccountId: {}, error: {}", 
                envelope.payAccountId, exception.message, exception)
            // 에러 발생 시 acknowledge하지 않아 재처리됨
            throw exception
        }
    }
}