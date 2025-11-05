package bong.presentationlayer.dto

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UserIdentifierTest : FunSpec({

    val json = Json { prettyPrint = false }

    context("UserIdentifier.CI") {
        test("올바른 값을 가진다") {
            // given
            val ci = UserIdentifier.CI(value = "ci-12345")

            // then
            ci.value shouldBe "ci-12345"
        }

        test("JSON으로 정상적으로 직렬화된다") {
            // given
            val ci = UserIdentifier.CI(value = "ci-67890")

            // when
            val jsonString = json.encodeToString<UserIdentifier>(ci)

            // then
            val decoded = json.decodeFromString<UserIdentifier>(jsonString)
            decoded.shouldBeInstanceOf<UserIdentifier.CI>()
            decoded.value shouldBe "ci-67890"
        }
    }

    context("UserIdentifier.DI") {
        test("올바른 값을 가진다") {
            // given
            val di = UserIdentifier.DI(value = "di-54321")

            // then
            di.value shouldBe "di-54321"
        }

        test("JSON으로 정상적으로 직렬화된다") {
            // given
            val di = UserIdentifier.DI(value = "di-98765")

            // when
            val jsonString = json.encodeToString<UserIdentifier>(di)

            // then
            val decoded = json.decodeFromString<UserIdentifier>(jsonString)
            decoded.shouldBeInstanceOf<UserIdentifier.DI>()
            decoded.value shouldBe "di-98765"
        }
    }

    context("UserIdentifier.Email") {
        test("올바른 값을 가진다") {
            // given
            val email = UserIdentifier.Email(value = "test@example.com")

            // then
            email.value shouldBe "test@example.com"
        }

        test("JSON으로 정상적으로 직렬화된다") {
            // given
            val email = UserIdentifier.Email(value = "user@test.org")

            // when
            val jsonString = json.encodeToString<UserIdentifier>(email)

            // then
            val decoded = json.decodeFromString<UserIdentifier>(jsonString)
            decoded.shouldBeInstanceOf<UserIdentifier.Email>()
            decoded.value shouldBe "user@test.org"
        }
    }

    test("type 필드를 기반으로 다형성 역직렬화가 정상적으로 동작한다") {
        // given
        val ciJson = """{"type":"CI","value":"ci-test"}"""
        val diJson = """{"type":"DI","value":"di-test"}"""
        val emailJson = """{"type":"EMAIL","value":"email@test.com"}"""

        // when
        val ciDecoded = json.decodeFromString<UserIdentifier>(ciJson)
        val diDecoded = json.decodeFromString<UserIdentifier>(diJson)
        val emailDecoded = json.decodeFromString<UserIdentifier>(emailJson)

        // then
        ciDecoded.shouldBeInstanceOf<UserIdentifier.CI>()
        ciDecoded.value shouldBe "ci-test"

        diDecoded.shouldBeInstanceOf<UserIdentifier.DI>()
        diDecoded.value shouldBe "di-test"

        emailDecoded.shouldBeInstanceOf<UserIdentifier.Email>()
        emailDecoded.value shouldBe "email@test.com"
    }
})
