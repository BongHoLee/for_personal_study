package com.bong.account.domain

enum class AccountStatus(
    val description: String
) {
    PENDING_APPROVAL("개설 신청"),
    ACTIVE("활성"),
    REJECTED("개설 거절"),
    SUSPENDED("정지"),
    CLOSED("해지")
}