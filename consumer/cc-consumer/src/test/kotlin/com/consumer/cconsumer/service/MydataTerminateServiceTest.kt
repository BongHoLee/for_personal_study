package com.consumer.cconsumer.service

import com.consumer.cconsumer.domain.entity.MydataTerminateUser
import com.consumer.cconsumer.domain.entity.TerminateStatus
import com.consumer.cconsumer.domain.repository.MydataTerminateUserRepository
import com.consumer.cconsumer.service.impl.MydataTerminateServiceImpl
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.springframework.dao.DataIntegrityViolationException

class MydataTerminateServiceTest : DescribeSpec({

    describe("MydataTerminateService") {
        lateinit var repository: MydataTerminateUserRepository
        lateinit var service: MydataTerminateService

        beforeEach {
            repository = mockk<MydataTerminateUserRepository>()
            service = MydataTerminateServiceImpl(repository)
        }

        describe("processTermination 메서드 실행 시") {
            context("PENDING 상태 레코드가 존재하지 않는 경우") {
                it("새로운 PENDING 레코드를 생성한다") {
                    // given
                    val payAccountId = 12345L
                    val reason = "USER_CONSENT_WITHDRAWN"
                    val capturedEntity = slot<MydataTerminateUser>()

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
                    val payAccountId = 67890L
                    val reason = "ACCOUNT_DELETED"
                    val existingRecord = MydataTerminateUser(
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

            context("DataIntegrityViolationException이 발생하는 경우") {
                it("멱등성을 보장하기 위해 예외를 삼키고 정상 처리한다") {
                    // given
                    val payAccountId = 99999L
                    val reason = "CONCURRENT_INSERT"

                    every { 
                        repository.findByPayAccountIdAndTerminateStatus(payAccountId, TerminateStatus.PENDING)
                    } returns null
                    
                    every { 
                        repository.save(any<MydataTerminateUser>())
                    } throws DataIntegrityViolationException("Unique constraint violation")

                    // when & then (예외가 발생하지 않아야 함)
                    service.processTermination(payAccountId, reason)

                    verify(exactly = 1) {
                        repository.findByPayAccountIdAndTerminateStatus(payAccountId, TerminateStatus.PENDING)
                    }
                    verify(exactly = 1) { repository.save(any()) }
                }
            }

            context("일반적인 예외가 발생하는 경우") {
                it("예외를 다시 던진다") {
                    // given
                    val payAccountId = 11111L
                    val reason = "DATABASE_ERROR"

                    every { 
                        repository.findByPayAccountIdAndTerminateStatus(payAccountId, TerminateStatus.PENDING)
                    } throws RuntimeException("Database connection failed")

                    // when & then
                    val exception = kotlin.runCatching {
                        service.processTermination(payAccountId, reason)
                    }.exceptionOrNull()

                    exception shouldNotBe null
                    exception?.message shouldBe "Database connection failed"

                    verify(exactly = 1) {
                        repository.findByPayAccountIdAndTerminateStatus(payAccountId, TerminateStatus.PENDING)
                    }
                    verify(exactly = 0) { repository.save(any()) }
                }
            }
        }
    }
})