package com.codex.consumer.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ConsentMessageTest : DescribeSpec({

    val objectMapper = jacksonObjectMapper()

    describe("ConsentMessage JSON 직렬화/역직렬화") {
        context("역직렬화 시") {
            it("JSON 페이로드로부터 객체를 생성한다") {
                val payload = """
                    {
                      "data": {
                        "delete_event_type": "PFM_SERVICE_CLOSED_BY_USER",
                        "pay_account_id": 46123695,
                        "is_remove": true,
                        "is_force": false,
                        "extra_field": "ignored"
                      },
                      "type": "WITHDRAW",
                      "unexpected": "ignore me"
                    }
                """.trimIndent()

                val message: ConsentMessage = objectMapper.readValue(payload)

                message.type shouldBe "WITHDRAW"
                message.data.deleteEventType shouldBe "PFM_SERVICE_CLOSED_BY_USER"
                message.data.payAccountId shouldBe 46_123_695L
                message.data.isRemove shouldBe true
                message.data.isForce shouldBe false
            }
        }

        context("직렬화 시") {
            it("스네이크 케이스 필드를 생성한다") {
                val message = ConsentMessage(
                    data = ConsentData(
                        deleteEventType = "PFM_SERVICE_CLOSED_BY_USER",
                        payAccountId = 46_123_695L,
                        isRemove = true,
                        isForce = false
                    ),
                    type = "WITHDRAW"
                )

                val json = objectMapper.writeValueAsString(message)

                json shouldContain "\"delete_event_type\":"
                json shouldContain "\"pay_account_id\":"
                json shouldContain "\"is_remove\":true"
                json shouldContain "\"is_force\":false"
            }
        }
    }
})
