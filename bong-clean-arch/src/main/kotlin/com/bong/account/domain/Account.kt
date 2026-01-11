package com.bong.account.domain

import com.bong.shared.BaseEntity
import jakarta.persistence.AttributeOverride
import jakarta.persistence.Column
import jakarta.persistence.Entity
import java.util.UUID

@Entity(name = "account")
@AttributeOverride(name = "id", column = Column(name = "account_id"))
class Account(
    status: AccountStatus,
    accountNumber: String,
    balance: Money,
) : BaseEntity() {

    @Column(name = "status")
    var status: AccountStatus = status
        protected set

    @Column(name = "account_number", unique = true)
    var accountNumber: String = accountNumber
        protected set

    @Column(name = "balance")
    var balance: Money = balance
        protected set

    companion object {
        fun create(): Account {
            return Account(
                status = AccountStatus.PENDING_APPROVAL,
                accountNumber = UUID.randomUUID().toString(),
                balance = Money.ZERO
            )
        }
    }
}`