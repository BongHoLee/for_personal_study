package bong.presentationlayer.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * V2에서 사용되는 유저 식별 타입 계층 구조
 * value를 가지며, CI, DI, Email 세 가지 타입을 지원
 *
 * ## JSON 직렬화 형식
 * ```json
 * {
 *   "type": "CI",
 *   "value": "user-value"
 * }
 * ```
 *
 * ## sealed interface vs sealed class
 *
 * **sealed interface를 사용하는 이유:**
 * - data class는 이미 Any를 상속하므로, sealed class를 상속받으면 다중 상속 제약
 * - sealed interface는 다중 구현이 가능하여 더 유연함
 * - Kotlin 1.5+ 에서 sealed interface 지원
 * - 구현체가 data class일 때 더 자연스러운 구조
 *
 * ## kotlinx.serialization의 Sealed 타입 처리 방식
 *
 * 1. **Discriminator 필드 자동 생성**
 *    - kotlinx.serialization은 sealed 타입을 직렬화할 때 자동으로 "type" 필드를 추가
 *    - 이 "type" 필드는 어떤 구현체인지 구분하는 discriminator 역할
 *    - sealed class와 sealed interface 모두 동일하게 동작
 *
 * 2. **@SerialName의 역할**
 *    - @SerialName("CI")는 JSON의 "type" 필드에 들어갈 값을 지정
 *    - 직렬화 시: UserIdentifier.CI -> {"type": "CI", ...}
 *    - 역직렬화 시: {"type": "CI", ...} -> UserIdentifier.CI로 복원
 *
 * 3. **코드에 type 프로퍼티가 없는 이유**
 *    - kotlinx.serialization이 내부적으로 "type" 필드를 자동 처리
 *    - 코드에 type 프로퍼티를 추가하면 오히려 충돌 발생
 *      (에러: "property name that conflicts with JSON class discriminator 'type'")
 *    - 비즈니스 로직에서 타입 구분이 필요하면 `when (userIdentifier) { is CI -> ... }` 사용
 *
 * ## 사용 예시
 * ```kotlin
 * // 객체 생성 - type 파라미터 없음
 * val ci = UserIdentifier.CI(value = "ci-12345")
 *
 * // JSON 직렬화 - type 필드 자동 추가
 * json.encodeToString(ci)  // {"type":"CI","value":"ci-12345"}
 *
 * // JSON 역직렬화 - type 필드로 하위 타입 자동 판별
 * val decoded = json.decodeFromString<UserIdentifier>("""{"type":"CI","value":"ci-12345"}""")
 * // decoded는 UserIdentifier.CI 타입
 * ```
 */
@Serializable
sealed interface UserIdentifier {
    val value: String
    fun toUserKey()

    @Serializable
    @SerialName("CI")
    data class CI(
        override val value: String
    ) : UserIdentifier {
        override fun toUserKey() {
            TODO("Not yet implemented")
        }
    }

    @Serializable
    @SerialName("DI")
    data class DI(
        override val value: String
    ) : UserIdentifier {
        override fun toUserKey() {
            TODO("Not yet implemented")
        }
    }

    @Serializable
    @SerialName("EMAIL")
    data class Email(
        override val value: String
    ) : UserIdentifier {
        override fun toUserKey() {
            TODO("Not yet implemented")
        }
    }
}
