package com.codex.consumer.config

import com.codex.consumer.consumer.MydataConsentConsumer
import com.codex.consumer.model.ConsentMessage
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.backoff.FixedBackOff

@Configuration
@EnableConfigurationProperties(KafkaConsumerProperties::class)
class KafkaConfig(
    private val kafkaConsumerProperties: KafkaConsumerProperties
) {

    @Bean
    fun kafkaErrorHandler(): CommonErrorHandler {
        val recoverer = { record: org.apache.kafka.clients.consumer.ConsumerRecord<*, *>, exception: Exception ->
            log.error("Kafka record processing failed for topic={}, partition={}, offset={}",
                record.topic(), record.partition(), record.offset(), exception)
        }
        val backOff = FixedBackOff(
            kafkaConsumerProperties.retry.intervalMillis,
            kafkaConsumerProperties.retry.maxAttempts
        )
        return DefaultErrorHandler(recoverer, backOff).apply {
            setAckAfterHandle(true)
        }
    }

    @Bean
    fun consentConsumerFactory(kafkaProperties: KafkaProperties): ConsumerFactory<String, ConsentMessage> {
        val properties = kafkaProperties.buildConsumerProperties().toMutableMap().apply {
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer::class.java)
        }

        val jsonDeserializer = JsonDeserializer(ConsentMessage::class.java).apply {
            addTrustedPackages("com.codex.consumer.model")
            setUseTypeMapperForKey(false)
            setRemoveTypeHeaders(false)
        }

        return DefaultKafkaConsumerFactory(
            properties,
            StringDeserializer(),
            jsonDeserializer
        )
    }

    @Bean
    fun byteArrayConsumerFactory(kafkaProperties: KafkaProperties): ConsumerFactory<String, ByteArray> {
        val properties = kafkaProperties.buildConsumerProperties().toMutableMap().apply {
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer::class.java)
            put("specific.avro.reader", kafkaConsumerProperties.avro.specificReader)
        }
        return DefaultKafkaConsumerFactory(
            properties,
            StringDeserializer(),
            ByteArrayDeserializer()
        )
    }

    @Bean
    fun consentKafkaListenerContainerFactory(
        consentConsumerFactory: ConsumerFactory<String, ConsentMessage>,
        kafkaErrorHandler: CommonErrorHandler
    ): ConcurrentKafkaListenerContainerFactory<String, ConsentMessage> {
        return ConcurrentKafkaListenerContainerFactory<String, ConsentMessage>().apply {
            consumerFactory = consentConsumerFactory
            setCommonErrorHandler(kafkaErrorHandler)
            containerProperties.ackMode = ContainerProperties.AckMode.valueOf(
                kafkaConsumerProperties.listener.ackMode.uppercase()
            )
            setConcurrency(kafkaConsumerProperties.listener.concurrency)
        }
    }

    @Bean
    fun byteArrayKafkaListenerContainerFactory(
        byteArrayConsumerFactory: ConsumerFactory<String, ByteArray>,
        kafkaErrorHandler: CommonErrorHandler
    ): ConcurrentKafkaListenerContainerFactory<String, ByteArray> {
        return ConcurrentKafkaListenerContainerFactory<String, ByteArray>().apply {
            consumerFactory = byteArrayConsumerFactory
            setCommonErrorHandler(kafkaErrorHandler)
            containerProperties.ackMode = ContainerProperties.AckMode.valueOf(
                kafkaConsumerProperties.listener.ackMode.uppercase()
            )
            setConcurrency(kafkaConsumerProperties.listener.concurrency)
        }
    }

    @Bean
    fun consentKafkaTemplate(kafkaProperties: KafkaProperties): KafkaTemplate<String, ConsentMessage> {
        val properties = kafkaProperties.buildProducerProperties().toMutableMap().apply {
            put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
            put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer::class.java)
        }
        val producerFactory = DefaultKafkaProducerFactory<String, ConsentMessage>(properties)
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun byteArrayKafkaTemplate(kafkaProperties: KafkaProperties): KafkaTemplate<String, ByteArray> {
        val properties = kafkaProperties.buildProducerProperties().toMutableMap().apply {
            put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
            put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.ByteArraySerializer::class.java)
            put("specific.avro.reader", kafkaConsumerProperties.avro.specificReader)
        }
        val producerFactory = DefaultKafkaProducerFactory<String, ByteArray>(properties)
        return KafkaTemplate(producerFactory)
    }

    companion object {
        private val log = LoggerFactory.getLogger(KafkaConfig::class.java)
    }
}
