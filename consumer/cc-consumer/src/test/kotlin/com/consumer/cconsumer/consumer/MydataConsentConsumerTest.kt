package com.consumer.cconsumer.consumer

import com.consumer.cconsumer.message.model.ConsentData
import com.consumer.cconsumer.message.model.ConsentMessage
import com.consumer.cconsumer.service.MydataTerminateService
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.kafka.support.Acknowledgment

class MydataConsentConsumerTest : DescribeSpec({

    describe("MydataConsentConsumer") {
        lateinit var mydataTerminateService: MydataTerminateService
        lateinit var acknowledgment: Acknowledgment
        lateinit var consumer: MydataConsentConsumer

        beforeEach {
            mydataTerminateService = mockk<MydataTerminateService>()
            acknowledgment = mockk<Acknowledgment>()
            consumer = MydataConsentConsumer(mydataTerminateService)
            every { acknowledgment.acknowledge() } just runs
        }

        describe("consumeConsentMessage 메서드 실행 시") {
            context("isRemove가 true인 메시지인 경우") {
                it("termination 처리를 수행하고 acknowledge 한다") {
                    // given
                    val consentData = ConsentData(
                        deleteEventType = "USER_CONSENT_WITHDRAWN",
                        payAccountId = 12345L,
                        isRemove = true,
                        isForce = false
                    )
                    val consentMessage = ConsentMessage(
                        data = consentData,
                        type = "CONSENT_EVENT"
                    )

                    every { 
                        mydataTerminateService.processTermination(12345L, "USER_CONSENT_WITHDRAWN")
                    } just runs

                    // when
                    consumer.consumeConsentMessage(
                        message = consentMessage,
                        topic = "mydata.consent.v1",
                        partition = 0,
                        offset = 100L,
                        acknowledgment = acknowledgment
                    )

                    // then
                    verify(exactly = 1) {
                        mydataTerminateService.processTermination(12345L, "USER_CONSENT_WITHDRAWN")
                    }
                    verify(exactly = 1) { acknowledgment.acknowledge() }
                }
            }

            context("isRemove가 false인 메시지인 경우") {
                it("termination 처리를 건너뛰고 acknowledge만 한다") {
                    // given
                    val consentData = ConsentData(
                        deleteEventType = "USER_CONSENT_GRANTED",
                        payAccountId = 67890L,
                        isRemove = false,
                        isForce = false
                    )
                    val consentMessage = ConsentMessage(
                        data = consentData,
                        type = "CONSENT_EVENT"
                    )

                    // when
                    consumer.consumeConsentMessage(
                        message = consentMessage,
                        topic = "mydata.consent.v1",
                        partition = 0,
                        offset = 200L,
                        acknowledgment = acknowledgment
                    )

                    // then
                    verify(exactly = 0) {
                        mydataTerminateService.processTermination(any(), any())
                    }
                    verify(exactly = 1) { acknowledgment.acknowledge() }
                }
            }

            context("Service에서 예외가 발생한 경우") {
                it("예외를 다시 던지고 acknowledge하지 않는다") {
                    // given
                    val consentData = ConsentData(
                        deleteEventType = "USER_CONSENT_WITHDRAWN",
                        payAccountId = 99999L,
                        isRemove = true,
                        isForce = false
                    )
                    val consentMessage = ConsentMessage(
                        data = consentData,
                        type = "CONSENT_EVENT"
                    )

                    every { 
                        mydataTerminateService.processTermination(99999L, "USER_CONSENT_WITHDRAWN")
                    } throws RuntimeException("Database connection failed")

                    // when & then
                    val exception = kotlin.runCatching {
                        consumer.consumeConsentMessage(
                            message = consentMessage,
                            topic = "mydata.consent.v1",
                            partition = 0,
                            offset = 300L,
                            acknowledgment = acknowledgment
                        )
                    }.exceptionOrNull()
                    
                    exception shouldNotBe null
                    exception?.message shouldBe "Database connection failed"

                    verify(exactly = 1) {
                        mydataTerminateService.processTermination(99999L, "USER_CONSENT_WITHDRAWN")
                    }
                    verify(exactly = 0) { acknowledgment.acknowledge() }
                }
            }
        }
    }
})