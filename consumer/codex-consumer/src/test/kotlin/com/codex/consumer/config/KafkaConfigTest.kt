package com.codex.consumer.config

import com.codex.consumer.model.ConsentMessage
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.test.util.ReflectionTestUtils

@SpringBootTest(
    properties = [
        "codex.kafka.listener.concurrency=3",
        "codex.kafka.listener.ack-mode=MANUAL_IMMEDIATE",
        "codex.kafka.retry.interval-millis=1500",
        "codex.kafka.retry.max-attempts=5",
        "codex.kafka.avro.specific-reader=false",
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.sql.init.mode=never"
    ]
)
@DisplayName("KafkaConfig")
class KafkaConfigTest {

    @Autowired
    private lateinit var kafkaConsumerProperties: KafkaConsumerProperties

    @Autowired
    private lateinit var kafkaErrorHandler: CommonErrorHandler

    @Autowired
    private lateinit var consentKafkaListenerContainerFactory: ConcurrentKafkaListenerContainerFactory<String, ConsentMessage>

    @Autowired
    private lateinit var consentConsumerFactory: ConsumerFactory<String, ConsentMessage>

    @Test
    fun `설정 프로퍼티가 바인딩된다`() {
        assertThat(kafkaConsumerProperties.listener.concurrency).isEqualTo(3)
        assertThat(kafkaConsumerProperties.retry.intervalMillis).isEqualTo(1_500)
        assertThat(kafkaConsumerProperties.retry.maxAttempts).isEqualTo(5)
        assertThat(kafkaConsumerProperties.avro.specificReader).isFalse()
    }

    @Test
    fun `Listener 팩토리에 병렬성과 Ack 모드가 적용된다`() {
        val concurrency = ReflectionTestUtils.getField(consentKafkaListenerContainerFactory, "concurrency") as? Int
        assertThat(concurrency).isEqualTo(3)
        assertThat(consentKafkaListenerContainerFactory.containerProperties.ackMode.name)
            .isEqualTo(kafkaConsumerProperties.listener.ackMode.uppercase())
    }

    @Test
    fun `CommonErrorHandler 가 DefaultErrorHandler 로 구성된다`() {
        val handler = kafkaErrorHandler as DefaultErrorHandler
        assertThat(handler).isNotNull
    }

    @Test
    fun `ConsumerFactory 에 기본 역직렬화 설정이 적용된다`() {
        val config = consentConsumerFactory.configurationProperties
        assertThat(config[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG]).isEqualTo(JsonDeserializer::class.java)
        assertThat(config[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG]).isEqualTo(StringDeserializer::class.java)
    }
}
