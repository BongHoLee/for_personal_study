package com.codex.consumer.domain.repository

import com.codex.consumer.domain.entity.MydataTerminateUser
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
class MydataTerminateUserRepositoryTest : DescribeSpec() {

    @Autowired
    private lateinit var repository: MydataTerminateUserRepository

    override fun extensions() = listOf(SpringExtension)

    init {
        this.describe("MydataTerminateUserRepository") {
            describe("save 메소드는") {
                it("자동 증가된 ID를 할당한다") {
                    val first = repository.saveAndFlush(mydataTerminateUser(payAccountId = 10L, reason = "first"))
                    val second = repository.saveAndFlush(mydataTerminateUser(payAccountId = 11L, reason = "second"))

                    first.id shouldNotBe null
                    second.id shouldNotBe null
                    second.id!! shouldBeGreaterThan first.id!!
                }

                it("동일한 payAccountId에 대해 중복된 PENDING 상태의 레코드를 거부한다") {
                    repository.saveAndFlush(mydataTerminateUser(payAccountId = 20L))

                    val exception = shouldThrow<DataIntegrityViolationException> {
                        repository.saveAndFlush(mydataTerminateUser(payAccountId = 20L))
                    }

                    exception.mostSpecificCause::class.simpleName shouldBe "JdbcSQLIntegrityConstraintViolationException"
                }

                it("COMPLETED 상태 완료 후 새로운 PENDING 상태의 레코드를 허용한다") {
                    val initial = repository.saveAndFlush(mydataTerminateUser(payAccountId = 30L, reason = "initial"))
                    initial.terminateStatus = TerminateStatus.COMPLETED
                    repository.saveAndFlush(initial)

                    repository.saveAndFlush(mydataTerminateUser(payAccountId = 30L, reason = "second run"))

                    val stored = repository.findAll().filter { it.payAccountId == 30L }
                    stored shouldHaveSize 2
                    stored.map { it.terminateStatus } shouldContainExactlyInAnyOrder listOf(
                        TerminateStatus.COMPLETED,
                        TerminateStatus.PENDING
                    )
                }
            }

            describe("findByPayAccountIdAndTerminateStatus 메소드는") {
                it("일치하는 PENDING 상태의 레코드를 반환한다") {
                    val saved = repository.saveAndFlush(mydataTerminateUser(payAccountId = 40L, reason = "persisted"))

                    val found = repository.findByPayAccountIdAndTerminateStatus(40L, TerminateStatus.PENDING)

                    found shouldNotBe null
                    found!!.id shouldBe saved.id
                    found.terminateStatus shouldBe TerminateStatus.PENDING
                }

                it("상태가 일치하지 않으면 null을 반환한다") {
                    repository.saveAndFlush(mydataTerminateUser(payAccountId = 50L, reason = "persisted"))

                    val absent = repository.findByPayAccountIdAndTerminateStatus(50L, TerminateStatus.COMPLETED)

                    absent shouldBe null
                }
            }
        }
    }
}

private fun mydataTerminateUser(
    payAccountId: Long,
    status: TerminateStatus = TerminateStatus.PENDING,
    reason: String? = null
) = MydataTerminateUser(
    payAccountId = payAccountId,
    terminateStatus = status,
    reason = reason
)