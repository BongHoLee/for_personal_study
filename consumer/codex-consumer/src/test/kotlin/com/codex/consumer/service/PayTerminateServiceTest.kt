package com.codex.consumer.service

import com.codex.consumer.domain.entity.TerminateStatus
import com.codex.consumer.domain.repository.PayTerminateUserRepository
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
@Import(PayTerminateServiceImpl::class)
class PayTerminateServiceTest : DescribeSpec() {

    @Autowired
    private lateinit var repository: PayTerminateUserRepository

    @Autowired
    private lateinit var service: PayTerminateService

    override fun extensions() = listOf(SpringExtension)

    init {
        describe("PayTerminateService의 handleTermination 메소드는") {
            it("새로운 파기 대상자를 저장한다") {
                service.handleTermination(payAccountId = 10L, reason = "CUSTOMER_REQUEST")

                val stored = repository.findByPayAccountIdAndTerminateStatus(10L, TerminateStatus.PENDING)
                stored shouldNotBe null
                stored!!.reason shouldBe "CUSTOMER_REQUEST"
            }

            it("이미 PENDING 레코드가 있으면 중복 삽입하지 않고 사유만 갱신한다") {
                service.handleTermination(11L, "FIRST")
                service.handleTermination(11L, "CHANGED")

                val all = repository.findAll().filter { it.payAccountId == 11L }
                all shouldHaveSize 1
                all.first().reason shouldBe "CHANGED"
            }

            it("기존 사유가 null 이면 새 사유로 대체한다") {
                service.handleTermination(13L, null)
                service.handleTermination(13L, "FILLED")

                val stored = repository.findByPayAccountIdAndTerminateStatus(13L, TerminateStatus.PENDING)
                stored shouldNotBe null
                stored!!.reason shouldBe "FILLED"
            }

            it("COMPLETED 이후에는 새로운 PENDING 레코드를 허용한다") {
                service.handleTermination(12L, "FIRST")
                val pending = repository.findByPayAccountIdAndTerminateStatus(12L, TerminateStatus.PENDING)!!
                pending.terminateStatus = TerminateStatus.COMPLETED
                repository.saveAndFlush(pending)

                service.handleTermination(12L, "SECOND")

                val records = repository.findAll().filter { it.payAccountId == 12L }
                records shouldHaveSize 2
                records.map { it.terminateStatus } shouldContainExactlyInAnyOrder listOf(
                    TerminateStatus.COMPLETED,
                    TerminateStatus.PENDING
                )
            }
        }
    }
}
