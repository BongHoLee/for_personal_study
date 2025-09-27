package com.codex.consumer.consumer

import com.codex.consumer.model.avro.PayAccountDeletedEnvelop
import com.codex.consumer.service.PayTerminateService
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkClear
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.verify
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter
import org.awaitility.Awaitility.await
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.io.ByteArrayOutputStream
import java.time.Duration

@SpringBootTest(
    properties = [
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.sql.init.mode=never"
    ]
)
@EmbeddedKafka(partitions = 1, topics = [PayAccountDeletedConsumer.PAY_ACCOUNT_DELETED_TOPIC])
class PayAccountDeletedConsumerTest : DescribeSpec() {

    @Autowired
    private lateinit var byteArrayKafkaTemplate: KafkaTemplate<String, ByteArray>

    @MockkBean(relaxed = true, clear = MockkClear.BEFORE)
    private lateinit var payTerminateService: PayTerminateService

    override fun extensions() = listOf(SpringExtension)

    init {
        describe("PayAccountDeletedConsumer는") {
            it("Avro 메시지를 디코딩하여 서비스에 처리를 위임한다") {
                val record = PayAccountDeletedEnvelop(
                    "e95e31d6-6c3c-4bb5-a5ef-baceb0e7d111",
                    1_725_123_456_789L,
                    55_001L,
                    "CUSTOMER_REQUEST"
                )

                byteArrayKafkaTemplate.send(
                    PayAccountDeletedConsumer.PAY_ACCOUNT_DELETED_TOPIC,
                    encode(record)
                ).get()

                await().atMost(Duration.ofSeconds(5)).untilAsserted {
                    verify(exactly = 1) { payTerminateService.handleTermination(55_001L, "CUSTOMER_REQUEST") }
                }
            }

            it("reason 필드가 null인 메시지도 정상 처리한다") {
                val record = PayAccountDeletedEnvelop(
                    "e95e31d6-6c3c-4bb5-a5ef-baceb0e7d112",
                    1_725_123_456_790L,
                    55_002L,
                    null
                )

                byteArrayKafkaTemplate.send(
                    PayAccountDeletedConsumer.PAY_ACCOUNT_DELETED_TOPIC,
                    encode(record)
                ).get()

                await().atMost(Duration.ofSeconds(5)).untilAsserted {
                    verify(exactly = 1) { payTerminateService.handleTermination(55_002L, null) }
                }
            }
        }
    }

    private fun encode(record: PayAccountDeletedEnvelop): ByteArray {
        val writer = SpecificDatumWriter(PayAccountDeletedEnvelop::class.java)
        val output = ByteArrayOutputStream()
        val encoder = EncoderFactory.get().binaryEncoder(output, null)
        writer.write(record, encoder)
        encoder.flush()
        return output.toByteArray()
    }

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun registerKafkaProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers") { System.getProperty("spring.embedded.kafka.brokers") }
            registry.add("spring.kafka.consumer.auto-offset-reset") { "earliest" }
        }
    }
}
