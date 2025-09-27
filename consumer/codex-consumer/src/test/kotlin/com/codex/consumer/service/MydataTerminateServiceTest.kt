package com.codex.consumer.service

import com.codex.consumer.domain.entity.TerminateStatus
import com.codex.consumer.domain.repository.MydataTerminateUserRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest(
    properties = [
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.sql.init.mode=never"
    ]
)
@Import(MydataTerminateServiceImpl::class)
class MydataTerminateServiceTest : DescribeSpec() {

    @Autowired
    private lateinit var repository: MydataTerminateUserRepository

    @Autowired
    private lateinit var service: MydataTerminateService

    override fun extensions() = listOf(SpringExtension)

    init {
        describe("MydataTerminateService의 handleTermination 메소드는") {
            it("새로운 파기 대상자를 저장한다") {
                service.handleTermination(payAccountId = 1L, reason = "PFM_CANCEL")

                val stored = repository.findByPayAccountIdAndTerminateStatus(1L, TerminateStatus.PENDING)
                stored shouldNotBe null
                stored!!.reason shouldBe "PFM_CANCEL"
            }

            it("이미 PENDING 레코드가 있으면 중복 삽입하지 않고 사유만 갱신한다") {
                service.handleTermination(2L, "INITIAL")
                service.handleTermination(2L, "UPDATED")

                val all = repository.findAll().filter { it.payAccountId == 2L }
                all shouldHaveSize 1
                all.first().reason shouldBe "UPDATED"
            }

            it("기존 사유가 null 이면 새 사유로 대체한다") {
                service.handleTermination(4L, null)
                service.handleTermination(4L, "NEW_REASON")

                val stored = repository.findByPayAccountIdAndTerminateStatus(4L, TerminateStatus.PENDING)
                stored shouldNotBe null
                stored!!.reason shouldBe "NEW_REASON"
            }

            it("COMPLETED 이후에는 새로운 PENDING 레코드를 허용한다") {
                service.handleTermination(3L, "FIRST")
                val pending = repository.findByPayAccountIdAndTerminateStatus(3L, TerminateStatus.PENDING)!!
                pending.terminateStatus = TerminateStatus.COMPLETED
                repository.saveAndFlush(pending)

                service.handleTermination(3L, "SECOND")

                val records = repository.findAll().filter { it.payAccountId == 3L }
                records shouldHaveSize 2
                records.map { it.terminateStatus } shouldContainExactlyInAnyOrder listOf(
                    TerminateStatus.COMPLETED,
                    TerminateStatus.PENDING
                )
            }
        }
    }
}
