package com.consumer.cconsumer.service

import com.consumer.cconsumer.domain.entity.PayTerminateUser
import com.consumer.cconsumer.domain.entity.TerminateStatus
import com.consumer.cconsumer.domain.repository.PayTerminateUserRepository
import com.consumer.cconsumer.service.impl.PayTerminateServiceImpl
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.dao.DataIntegrityViolationException

class PayTerminateServiceTest : DescribeSpec({

    describe("PayTerminateService") {
        lateinit var repository: PayTerminateUserRepository
        lateinit var service: PayTerminateService

        beforeEach {
            repository = mockk<PayTerminateUserRepository>()
            service = PayTerminateServiceImpl(repository)
        }

        describe("processTermination 메서드 실행 시") {
            context("PENDING 상태 레코드가 존재하지 않는 경우") {
                it("새로운 PENDING 레코드를 생성한다") {
                    // given
                    val payAccountId = 54321L
                    val reason = "ACCOUNT_CLOSURE"
                    val capturedEntity = slot<PayTerminateUser>()

                    every { 
                        repository.findByPayAccountIdAndTerminateStatus(payAccountId, TerminateStatus.PENDING)
                    } returns null
                    
                    every { 
                        repository.save(capture(capturedEntity))
                    } returns mockk()

                    // when
                    service.processTermination(payAccountId, reason)

                    // then
                    verify(exactly = 1) {
                        repository.findByPayAccountIdAndTerminateStatus(payAccountId, TerminateStatus.PENDING)
                    }
                    verify(exactly = 1) { repository.save(any()) }

                    // 저장된 엔티티 검증
                    val savedEntity = capturedEntity.captured
                    savedEntity.payAccountId shouldBe payAccountId
                    savedEntity.terminateStatus shouldBe TerminateStatus.PENDING
                    savedEntity.reason shouldBe reason
                }
            }

            context("PENDING 상태 레코드가 이미 존재하는 경우") {
                it("중복 삽입을 방지하고 저장하지 않는다") {
                    // given
                    val payAccountId = 98765L
                    val reason = "USER_REQUEST"
                    val existingRecord = PayTerminateUser(
                        payAccountId = payAccountId,
                        terminateStatus = TerminateStatus.PENDING,
                        reason = "PREVIOUS_REASON"
                    )

                    every { 
                        repository.findByPayAccountIdAndTerminateStatus(payAccountId, TerminateStatus.PENDING)
                    } returns existingRecord

                    // when
                    service.processTermination(payAccountId, reason)

                    // then
                    verify(exactly = 1) {
                        repository.findByPayAccountIdAndTerminateStatus(payAccountId, TerminateStatus.PENDING)
                    }
                    verify(exactly = 0) { repository.save(any()) }
                }
            }

            context("멱등성 검증: COMPLETED → PENDING 허용") {
                it("COMPLETED 상태가 있어도 새로운 PENDING 레코드를 생성한다") {
                    // given
                    val payAccountId = 13579L
                    val reason = "RE_TERMINATION"

                    // PENDING 상태는 없지만 COMPLETED 상태는 있을 수 있음
                    every { 
                        repository.findByPayAccountIdAndTerminateStatus(payAccountId, TerminateStatus.PENDING)
                    } returns null
                    
                    every { 
                        repository.save(any<PayTerminateUser>())
                    } returns mockk()

                    // when
                    service.processTermination(payAccountId, reason)

                    // then
                    verify(exactly = 1) {
                        repository.findByPayAccountIdAndTerminateStatus(payAccountId, TerminateStatus.PENDING)
                    }
                    verify(exactly = 1) { repository.save(any()) }
                }
            }

            context("DataIntegrityViolationException이 발생하는 경우") {
                it("멱등성을 보장하기 위해 예외를 삼키고 정상 처리한다") {
                    // given
                    val payAccountId = 24680L
                    val reason = "CONCURRENT_PROCESSING"

                    every { 
                        repository.findByPayAccountIdAndTerminateStatus(payAccountId, TerminateStatus.PENDING)
                    } returns null
                    
                    every { 
                        repository.save(any<PayTerminateUser>())
                    } throws DataIntegrityViolationException("Unique constraint violation")

                    // when & then (예외가 발생하지 않아야 함)
                    service.processTermination(payAccountId, reason)

                    verify(exactly = 1) {
                        repository.findByPayAccountIdAndTerminateStatus(payAccountId, TerminateStatus.PENDING)
                    }
                    verify(exactly = 1) { repository.save(any()) }
                }
            }
        }
    }
})