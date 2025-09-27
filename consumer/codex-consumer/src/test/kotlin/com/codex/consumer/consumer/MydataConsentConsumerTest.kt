package com.codex.consumer.consumer

import com.codex.consumer.model.ConsentData
import com.codex.consumer.model.ConsentMessage
import com.codex.consumer.service.MydataTerminateService
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkClear
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.verify
import org.awaitility.Awaitility.await
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.time.Duration

@SpringBootTest(
    properties = [
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.sql.init.mode=never"
    ]
)
@EmbeddedKafka(partitions = 1, topics = [MydataConsentConsumer.MYDATA_CONSENT_TOPIC])
class MydataConsentConsumerTest : DescribeSpec() {

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, ConsentMessage>

    @MockkBean(relaxed = true, clear = MockkClear.BEFORE)
    private lateinit var mydataTerminateService: MydataTerminateService

    override fun extensions() = listOf(SpringExtension)

    init {
        describe("MydataConsentConsumer는") {
            it("is_remove가 true인 메시지를 받으면 서비스에 처리를 위임한다") {
                val message = ConsentMessage(
                    data = ConsentData(
                        deleteEventType = "PFM_SERVICE_CLOSED_BY_USER",
                        payAccountId = 777L,
                        isRemove = true,
                        isForce = false
                    ),
                    type = "WITHDRAW"
                )

                kafkaTemplate.send(MydataConsentConsumer.MYDATA_CONSENT_TOPIC, message).get()

                await().atMost(Duration.ofSeconds(5)).untilAsserted {
                    verify(exactly = 1) { mydataTerminateService.handleTermination(777L, "PFM_SERVICE_CLOSED_BY_USER") }
                }
            }

            it("is_remove가 false인 메시지는 무시한다") {
                val message = ConsentMessage(
                    data = ConsentData(
                        deleteEventType = "PFM_SERVICE_CLOSED_BY_USER",
                        payAccountId = 888L,
                        isRemove = false,
                        isForce = false
                    ),
                    type = "WITHDRAW"
                )

                kafkaTemplate.send(MydataConsentConsumer.MYDATA_CONSENT_TOPIC, message).get()

                Thread.sleep(1000)

                verify(exactly = 0) { mydataTerminateService.handleTermination(any(), any()) }
            }
        }
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
