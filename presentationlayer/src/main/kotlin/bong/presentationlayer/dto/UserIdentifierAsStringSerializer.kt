package bong.presentationlayer.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * UserIdentifier.CI를 단순 문자열로 직렬화/역직렬화하는 Serializer
 * V1 API에서 사용
 *
 * ## 용도
 * V1 API에서는 CI를 객체가 아닌 단순 문자열로 표현해야 함
 * - V1 JSON: `"ci": "ci-value-12345"`
 * - V2 JSON: `"user_identifier": {"type": "CI", "value": "ci-value-12345"}`
 *
 * ## 동작 방식
 * - 직렬화: UserIdentifier.CI(value = "ci-12345") → "ci-12345"
 * - 역직렬화: "ci-12345" → UserIdentifier.CI(value = "ci-12345")
 *
 * ## 사용 예시
 * ```kotlin
 * @Serializable
 * data class V1RequestBody(
 *     @SerialName("ci")
 *     @Serializable(with = UserIdentifierAsStringSerializer::class)
 *     val ci: UserIdentifier.CI
 * )
 * ```
 */
object UserIdentifierAsStringSerializer : KSerializer<UserIdentifier.CI> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UserIdentifier.CI", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UserIdentifier.CI) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): UserIdentifier.CI {
        return UserIdentifier.CI(value = decoder.decodeString())
    }
}
