package com.bong.account.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MoneyTest : FunSpec({

    context("Money는 불변 객체로써 생성할 수 있다.") {
        test("ZERO는 값이 0인 Money 객체를 생성한다.") {
            Money.ZERO shouldBe Money(0L)
        }

        test("subtract 연산은 값을 뺀 새로운 Money 객체를 생성한다.") {

        }

        test("add 연산은 값을 더한 새로운 Money 객체를 생성한다."){

        }
    }
})
