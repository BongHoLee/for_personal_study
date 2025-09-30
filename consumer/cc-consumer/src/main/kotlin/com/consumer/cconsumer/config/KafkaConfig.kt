package com.consumer.cconsumer.config

import com.consumer.cconsumer.message.avro.PayAccountDeletedEnvelop
import com.consumer.cconsumer.message.model.ConsentMessage
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.listener.RetryListener
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.util.backoff.FixedBackOff

@Configuration
@EnableKafka
@ConditionalOnProperty(
    name = ["spring.kafka.enabled"],
    havingValue = "true",
    matchIfMissing = true
)
class KafkaConfig {

    private val log = LoggerFactory.getLogger(KafkaConfig::class.java)

    @Value("\${spring.kafka.consumer.bootstrap-servers:localhost:9092}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.consumer.group-id:cc-consumer-group}")
    private lateinit var groupId: String

    @Value("\${spring.kafka.consumer.auto-offset-reset:earliest}")
    private lateinit var autoOffsetReset: String

    @Value("\${spring.kafka.schema-registry.url:http://localhost:8081}")
    private lateinit var schemaRegistryUrl: String

    @Bean
    fun kafkaErrorHandler(): DefaultErrorHandler {
        val fixedBackOff = FixedBackOff(1000L, 3L) // 1초 간격, 3회 재시도
        val errorHandler = DefaultErrorHandler(fixedBackOff)
        
        // JSON 파싱 에러 등 재시도해도 의미없는 예외는 재시도하지 않음
        errorHandler.addNotRetryableExceptions(
            JsonProcessingException::class.java,
            IllegalArgumentException::class.java
        )
        
        // 재시도 및 최종 실패 로깅
        errorHandler.setRetryListeners(object : RetryListener {
            override fun failedDelivery(
                record: ConsumerRecord<*, *>,
                ex: Exception,
                deliveryAttempt: Int
            ) {
                log.warn(
                    "Retry attempt {} failed for topic: {}, partition: {}, offset: {}, key: {}", 
                    deliveryAttempt, record.topic(), record.partition(), record.offset(), record.key(), ex
                )
            }
            
            override fun recovered(
                record: ConsumerRecord<*, *>, 
                ex: Exception
            ) {
                log.error(
                    "All retries exhausted for topic: {}, partition: {}, offset: {}, key: {}. Message will be skipped.", 
                    record.topic(), record.partition(), record.offset(), record.key(), ex
                )
                // 필요시 DLQ(Dead Letter Queue) 전송이나 알림 로직 추가 가능
            }
        })
        
        return errorHandler
    }

    // JSON 메시지용 Consumer Factory
    @Bean
    fun jsonConsumerFactory(): ConsumerFactory<String, ConsentMessage> {
        val props = mutableMapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to autoOffsetReset,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            JsonDeserializer.VALUE_DEFAULT_TYPE to ConsentMessage::class.java.name,
            JsonDeserializer.TRUSTED_PACKAGES to "com.consumer.cconsumer.message.model",
            
            // 성능 최적화 설정
            ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 500,
            ConsumerConfig.FETCH_MIN_BYTES_CONFIG to 1024,
            ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG to 500,
            ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG to 30000,
            ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG to 10000
        )
        
        return DefaultKafkaConsumerFactory(
            props,
            StringDeserializer(),
            JsonDeserializer(ConsentMessage::class.java)
        )
    }

    // JSON 메시지용 Listener Container Factory
    @Bean
    fun jsonKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, ConsentMessage> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, ConsentMessage>()
        factory.consumerFactory = jsonConsumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.setCommonErrorHandler(kafkaErrorHandler()) // 에러 핸들러 적용
        
        // 성능 최적화 설정
        factory.setConcurrency(3) // 동시 처리할 컨슈머 스레드 수
        factory.containerProperties.pollTimeout = 3000L // 폴링 타임아웃 3초
        
        return factory
    }

    // Avro 메시지용 Consumer Factory
    @Bean
    fun avroConsumerFactory(): ConsumerFactory<String, PayAccountDeletedEnvelop> {
        val props = mutableMapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to autoOffsetReset,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            
            // Avro 관련 설정
            KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
            KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to true,
            
            // 성능 최적화 설정
            ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 500,
            ConsumerConfig.FETCH_MIN_BYTES_CONFIG to 1024,
            ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG to 500,
            ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG to 30000,
            ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG to 10000
        )
        
        return DefaultKafkaConsumerFactory(props)
    }

    // Avro 메시지용 Listener Container Factory
    @Bean
    fun avroKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, PayAccountDeletedEnvelop> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, PayAccountDeletedEnvelop>()
        factory.consumerFactory = avroConsumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.setCommonErrorHandler(kafkaErrorHandler()) // 에러 핸들러 적용
        
        // 성능 최적화 설정
        factory.setConcurrency(3) // 동시 처리할 컨슈머 스레드 수
        factory.containerProperties.pollTimeout = 3000L // 폴링 타임아웃 3초
        
        return factory
    }
}