package bong.presentationlayer.service

import bong.presentationlayer.domain.UserId
import bong.presentationlayer.dto.common.UserIdentifier
import bong.presentationlayer.exception.UserNotFoundException
import org.springframework.stereotype.Service

/**
 * Mock UserIdResolver 구현체
 *
 * 실제 환경에서는 DB 조회 등을 통해 UserIdentifier → UserId 매핑을 수행하지만,
 * 여기서는 간단한 메모리 기반 구현을 제공합니다.
 *
 * ## Mock 데이터
 * - CI "ci-12345" → UserId "user-001"
 * - DI "di-67890" → UserId "user-002"
 * - Email "test@example.com" → UserId "user-003"
 * - 그 외는 UserNotFoundException 발생
 */
@Service
class MockUserIdResolver : UserIdResolver {

    private val userMap = mapOf(
        "ci-12345" to UserId("user-001"),
        "di-67890" to UserId("user-002"),
        "test@example.com" to UserId("user-003")
    )

    override fun resolve(userIdentifier: UserIdentifier, transactionId: String): UserId {
        return userMap[userIdentifier.value]
            ?: throw UserNotFoundException(userIdentifier, transactionId)
    }
}
