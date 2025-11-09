package bong.presentationlayer.domain

/**
 * 사용자 ID
 *
 * UserIdentifier(CI, DI, Email)를 기반으로 실제 시스템에서 사용하는 사용자 ID로 변환된 값
 */
@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "UserId cannot be blank" }
    }

    override fun toString(): String = value
}
