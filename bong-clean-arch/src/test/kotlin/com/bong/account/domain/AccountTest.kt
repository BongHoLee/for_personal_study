package com.bong.account.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AccountTest : FunSpec({

    test("계좌 개설 시에는 '정상' 상태이어야 한다.") {
        Account.create()
    }

})
