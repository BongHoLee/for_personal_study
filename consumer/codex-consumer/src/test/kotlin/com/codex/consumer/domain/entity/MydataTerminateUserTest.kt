package com.codex.consumer.domain.entity

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MydataTerminateUserTest : DescribeSpec({

    describe("MydataTerminateUser 엔티티는") {
        context("생성 시") {
            it("terminateStatus의 기본값은 PENDING 이다") {
                val entity = MydataTerminateUser(payAccountId = 1L, reason = "sample")

                entity.terminateStatus shouldBe TerminateStatus.PENDING
                entity.id shouldBe null
            }

            it("terminateStatus를 명시적으로 지정할 수 있다") {
                val entity = MydataTerminateUser(
                    payAccountId = 1L,
                    terminateStatus = TerminateStatus.COMPLETED,
                    reason = "processed"
                )

                entity.terminateStatus shouldBe TerminateStatus.COMPLETED
                entity.reason shouldBe "processed"
            }
        }

        context("상태 변경 시") {
            it("terminateStatus를 변경할 수 있다") {
                val entity = MydataTerminateUser(payAccountId = 1L)
                entity.terminateStatus = TerminateStatus.COMPLETED
                entity.terminateStatus shouldBe TerminateStatus.COMPLETED
            }
        }
    }
})
