package bong.training.real

import bong.training.static_proxy.LogInvocator
import bong.training.static_proxy.UserLogServiceProxy
import bong.training.static_proxy.UserService
import bong.training.static_proxy.UserServiceImpl
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class UserServiceTest : FunSpec({

    val realSubject: UserService = UserServiceImpl()

    test("saveUser should save a user and return its id") {
        // Given: LogProxy를 모킹
        val mockLogInvocator = mockk<LogInvocator>()
        every { mockLogInvocator.executeWithLog<Long>(any()) } answers {
            firstArg<() -> Long>().invoke()
        }

        val logProxyUser: UserService = UserLogServiceProxy(realSubject, mockLogInvocator)

        // When: saveUser 메서드 호출
        val result = logProxyUser.saveUser("test user")

        // Then: 결과 검증 및 LogProxy 호출 검증
        result shouldBe 1L
        verify(exactly = 1) { mockLogInvocator.executeWithLog<Long>(any()) }
    }

    test("findUser should find a user and verify proxy call") {
        // Given: LogProxy를 모킹
        val mockLogInvocator = mockk<LogInvocator>()
        every { mockLogInvocator.executeWithLog<String>(any()) } answers {
            firstArg<() -> String>().invoke()
        }

        val logProxyUser: UserService = UserLogServiceProxy(realSubject, mockLogInvocator)

        // 먼저 사용자를 저장
        realSubject.saveUser("test user")

        // When: findUser 메서드 호출
        val result = logProxyUser.findUser(1L)

        // Then: 결과 검증 및 LogProxy 호출 검증
        result shouldBe "test user"
        verify(exactly = 1) { mockLogInvocator.executeWithLog<String>(any()) }
    }

    test("deleteUser should delete a user and verify proxy call") {
        // Given: LogProxy를 모킹
        val mockLogInvocator = mockk<LogInvocator>()
        every { mockLogInvocator.executeWithLog<Boolean>(any()) } answers {
            firstArg<() -> Boolean>().invoke()
        }

        val logProxyUser: UserService = UserLogServiceProxy(realSubject, mockLogInvocator)

        // 먼저 사용자를 저장
        realSubject.saveUser("test user")

        // When: deleteUser 메서드 호출
        val result = logProxyUser.deleteUser(1L)

        // Then: 결과 검증 및 LogProxy 호출 검증
        result shouldBe true
        verify(exactly = 1) { mockLogInvocator.executeWithLog<Boolean>(any()) }
    }

    // 모킹 없는 행위 테스트들 추가
    test("saveUser should call LogProxy without mocking") {
        // Given: 실제 LogProxy 인스턴스 사용
        val userService = UserServiceImpl() // 새로운 인스턴스 생성
        val logInvocator = LogInvocator()
        logInvocator.resetCallCount() // 카운터 초기화
        val logProxyUser: UserService = UserLogServiceProxy(userService, logInvocator)

        // When: saveUser 메서드 호출
        val result = logProxyUser.saveUser("test user")

        // Then: 결과 검증 및 LogProxy 호출 횟수 검증
        result shouldBe 1L
        logInvocator.getCallCount() shouldBe 1
    }

    test("findUser should call LogProxy without mocking") {
        // Given: 실제 LogProxy 인스턴스 사용
        val userService = UserServiceImpl() // 새로운 인스턴스 생성
        val logInvocator = LogInvocator()
        logInvocator.resetCallCount()
        val logProxyUser: UserService = UserLogServiceProxy(userService, logInvocator)

        // 먼저 사용자를 저장 (같은 logProxyUser 인스턴스 사용)
        logProxyUser.saveUser("test user")
        logInvocator.resetCallCount() // 저장 후 카운터 리셋

        // When: findUser 메서드 호출
        val result = logProxyUser.findUser(1L)

        // Then: 결과 검증 및 LogProxy 호출 횟수 검증
        result shouldBe "test user"
        logInvocator.getCallCount() shouldBe 1
    }

    test("multiple operations should accumulate LogProxy calls") {
        // Given: 실제 LogProxy 인스턴스 사용
        val userService = UserServiceImpl() // 새로운 인스턴스 생성
        val logInvocator = LogInvocator()
        logInvocator.resetCallCount()
        val logProxyUser: UserService = UserLogServiceProxy(userService, logInvocator)

        // When: 여러 메서드 호출
        logProxyUser.saveUser("user1")
        logProxyUser.saveUser("user2")
        logProxyUser.findUser(1L)

        // Then: LogProxy가 총 3번 호출되었는지 검증
        logInvocator.getCallCount() shouldBe 3
    }

})
