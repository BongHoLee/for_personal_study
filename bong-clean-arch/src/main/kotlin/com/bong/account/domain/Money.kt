package com.bong.account.domain

import java.math.BigDecimal

@JvmInline
value class Money(
    val amount: BigDecimal
) {
    companion object {
        val ZERO = Money(BigDecimal.ZERO)

        fun of(amount: BigDecimal) = Money(amount)
        fun of(amount: Number) = Money(BigDecimal(amount.toString()))
    }

    operator fun plus(other: Money) = Money.of(this.amount.plus(other.amount))
    operator fun minus(other: Money) = Money.of(this.amount.minus(other.amount))
}