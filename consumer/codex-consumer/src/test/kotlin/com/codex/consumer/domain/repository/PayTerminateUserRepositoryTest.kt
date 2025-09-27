package com.codex.consumer.domain.repository

import com.codex.consumer.domain.entity.PayTerminateUser
import com.codex.consumer.domain.entity.TerminateStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException

@DataJpaTest(
    properties = [
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create"
    ]
)
class PayTerminateUserRepositoryTest : DescribeSpec() {

    @Autowired
    private lateinit var repository: PayTerminateUserRepository

    override fun extensions() = listOf(SpringExtension)

    init {
        this.describe("PayTerminateUserRepository") {
            describe("save 메소드는") {
                it("자동 증가된 ID를 할당한다") {
                    val first = repository.saveAndFlush(payTerminateUser(payAccountId = 100L, reason = "first"))
                    val second = repository.saveAndFlush(payTerminateUser(payAccountId = 101L, reason = "second"))

                    first.id shouldNotBe null
                    second.id shouldNotBe null
                    second.id!! shouldBeGreaterThan first.id!!
                }

                it("동일한 payAccountId에 대해 중복된 PENDING 상태의 레코드를 거부한다") {
                    repository.saveAndFlush(payTerminateUser(payAccountId = 200L))

                    val exception = shouldThrow<DataIntegrityViolationException> {
                        repository.saveAndFlush(payTerminateUser(payAccountId = 200L))
                    }

                    exception.mostSpecificCause::class.simpleName shouldBe "JdbcSQLIntegrityConstraintViolationException"
                }

                it("COMPLETED 상태 완료 후 새로운 PENDING 상태의 레코드를 허용한다") {
                    val initial = repository.saveAndFlush(payTerminateUser(payAccountId = 300L, reason = "initial"))
                    initial.terminateStatus = TerminateStatus.COMPLETED
                    repository.saveAndFlush(initial)

                    val reRegistered = repository.saveAndFlush(payTerminateUser(payAccountId = 300L, reason = "second run"))

                    val stored = repository.findAll().filter { it.payAccountId == 300L }
                    stored shouldHaveSize 2
                    stored.map { it.terminateStatus } shouldContainExactlyInAnyOrder listOf(
                        TerminateStatus.COMPLETED,
                        TerminateStatus.PENDING
                    )
                    reRegistered.terminateStatus shouldBe TerminateStatus.PENDING
                }
            }

            describe("findByPayAccountIdAndTerminateStatus 메소드는") {
                it("일치하는 PENDING 상태의 레코드를 반환한다") {
                    val saved = repository.saveAndFlush(payTerminateUser(payAccountId = 400L, reason = "persisted"))

                    val found = repository.findByPayAccountIdAndTerminateStatus(400L, TerminateStatus.PENDING)

                    found shouldNotBe null
                    found!!.id shouldBe saved.id
                    found.terminateStatus shouldBe TerminateStatus.PENDING
                }

                it("상태가 일치하지 않으면 null을 반환한다") {
                    repository.saveAndFlush(payTerminateUser(payAccountId = 500L, reason = "persisted"))

                    val absent = repository.findByPayAccountIdAndTerminateStatus(500L, TerminateStatus.COMPLETED)

                    absent shouldBe null
                }
            }
        }
    }
}

private fun payTerminateUser(
    payAccountId: Long,
    status: TerminateStatus = TerminateStatus.PENDING,
    reason: String? = null
) = PayTerminateUser(
    payAccountId = payAccountId,
    terminateStatus = status,
    reason = reason
)
