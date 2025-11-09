package bong.presentationlayer.exception

import bong.presentationlayer.dto.common.UserIdentifier

/**
 * 사용자를 찾을 수 없을 때 발생하는 예외
 *
 * UserIdentifier로 UserId를 조회했지만 매칭되는 사용자가 없을 경우 발생합니다.
 */
class UserNotFoundException(
    val userIdentifier: UserIdentifier,
    message: String = "User not found for identifier: ${userIdentifier.value}"
) : RuntimeException(message)
