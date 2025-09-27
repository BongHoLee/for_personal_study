package com.bong.account.domain

@JvmInline
value class Money(
    val amount: Long
) {
    companion object {
        val ZERO = Money(0)

        fun of(amount: Long): Money = Money(amount)
        fun of(amount: Int): Money = Money(amount.toLong())

    }
}