package com.bong.account.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class MoneyTest : FunSpec({

    context("Money는 불변 객체로써 생성할 수 있다.") {
        test("ZERO는 값이 0인 Money 객체를 생성한다.") {
            Money.ZERO shouldBe Money.of(0L)
        }

        test("minus(-) 연산은 값을 뺀 새로운 Money 객체를 생성한다.") {
            Money.of(1000) - Money.of(1000) shouldBe Money.of(0)
            Money.of(1000) - Money.of(500) shouldBe Money.of(500)

            Money.of(1000).minus(Money.of(500)) shouldBe Money.of(500)
        }

        test("add 연산은 값을 더한 새로운 Money 객체를 생성한다."){
            Money.ZERO + Money.of(100) shouldBe Money.of(100)
            Money.ZERO.plus( Money.of(100)) shouldBe Money.of(100)
            Money.of(1000) + Money.of(500) shouldBe Money.of(1500)
            Money.of(1000).plus(Money.of(500)) shouldBe Money.of(1500)
        }
    }
})
