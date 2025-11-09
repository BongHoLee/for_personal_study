package bong.presentationlayer.service

import bong.presentationlayer.domain.UserId
import bong.presentationlayer.dto.common.UserIdentifier
import bong.presentationlayer.exception.UserNotFoundException

/**
 * UserIdentifier를 UserId로 변환하는 Resolver
 *
 * UserIdentifier(CI, DI, Email)를 기반으로 실제 시스템의 UserId를 조회합니다.
 * 매칭되는 UserId가 없으면 UserNotFoundException을 발생시킵니다.
 *
 * ## 주의사항
 * - resolve() 성공 시 RequestContext에 UserId를 저장합니다.
 * - 이는 TransactionHistory 로깅을 위함입니다.
 */
interface UserIdResolver {
    /**
     * UserIdentifier를 UserId로 변환
     *
     * @param userIdentifier 사용자 식별 정보
     * @param transactionId API 트랜잭션 ID (에러 응답 시 사용)
     * @param requestContext Request Context (UserId 저장용)
     * @return UserId
     * @throws UserNotFoundException 매칭되는 사용자가 없을 경우
     */
    fun resolve(
        userIdentifier: UserIdentifier,
        transactionId: String
    ): UserId
}
