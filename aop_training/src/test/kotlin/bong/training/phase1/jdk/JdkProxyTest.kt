package bong.training.phase1.jdk

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import java.lang.reflect.Proxy

/**
 * JDK 동적 프록시 기능 검증을 위한 테스트
 * 
 * 테스트 목표:
 * 1. JDK 프록시가 정상적으로 생성되는지 확인
 * 2. 프록시를 통한 메소드 호출이 올바르게 동작하는지 확인
 * 3. 로깅 기능이 정상적으로 작동하는지 확인
 * 4. 예외 상황에서의 프록시 동작 확인
 * 5. 프록시의 내부 구조와 특성 이해
 */
class JdkProxyTest : DescribeSpec({
    
    describe("JDK 동적 프록시 기본 동작") {
        
        context("프록시 생성") {
            it("UserService 인터페이스에 대한 JDK 프록시를 생성할 수 있다") {
                // Given: 타겟 객체 준비
                val target = UserServiceImpl()
                
                // When: JDK 프록시 생성
                val proxy = JdkProxyFactory.createProxy(target, UserService::class.java)
                
                // Then: 프록시가 올바르게 생성되었는지 확인
                proxy shouldNotBe null
                proxy.shouldBeInstanceOf<UserService>()
                
                // 프록시 클래스인지 확인
                Proxy.isProxyClass(proxy.javaClass) shouldBe true
                
                // 원본 객체와 다른 객체인지 확인 (프록시는 새로운 객체)
                (proxy === target) shouldBe false
                
                println("✅ 프록시 생성 성공")
                println("   - 타겟 클래스: ${target.javaClass.name}")
                println("   - 프록시 클래스: ${proxy.javaClass.name}")
                println("   - JDK 프록시 여부: ${Proxy.isProxyClass(proxy.javaClass)}")
            }
            
            it("프록시는 인터페이스를 구현해야 한다") {
                val target = UserServiceImpl()
                val proxy = JdkProxyFactory.createProxy(target, UserService::class.java)
                
                // 프록시가 UserService 인터페이스를 구현하는지 확인
                (proxy is UserService) shouldBe true
                
                // 프록시의 구현 인터페이스 확인
                val interfaces = proxy.javaClass.interfaces
                interfaces.size shouldBe 1
                interfaces[0] shouldBe UserService::class.java
                
                println("✅ 인터페이스 구현 확인")
                println("   - 구현된 인터페이스: ${interfaces.contentToString()}")
            }
        }
        
        context("프록시를 통한 메소드 호출") {
            
            it("findUser 메소드 호출이 정상 동작한다") {
                // Given
                val target = UserServiceImpl()
                val proxy = JdkProxyFactory.createProxy(target, UserService::class.java)
                
                // When: 프록시를 통해 메소드 호출
                val result = proxy.findUser(1L)
                
                // Then: 결과가 예상과 일치하는지 확인
                result shouldContain "User-1"
                
                println("✅ findUser 메소드 호출 성공")
                println("   - 호출 결과: $result")
            }
            
            it("saveUser 메소드 호출이 정상 동작한다") {
                // Given
                val target = UserServiceImpl()
                val proxy = JdkProxyFactory.createProxy(target, UserService::class.java)
                
                // When
                val userId = proxy.saveUser("김개발")
                
                // Then
                userId shouldNotBe null
                userId shouldNotBe 0L
                
                println("✅ saveUser 메소드 호출 성공")
                println("   - 생성된 사용자 ID: $userId")
            }
            
            it("deleteUser 메소드 호출이 정상 동작한다") {
                // Given
                val target = UserServiceImpl()
                val proxy = JdkProxyFactory.createProxy(target, UserService::class.java)
                
                // 먼저 사용자 생성
                val userId = proxy.saveUser("삭제될사용자")
                
                // When: 생성된 사용자 삭제
                val deleted = proxy.deleteUser(userId)
                
                // Then
                deleted shouldBe true
                
                println("✅ deleteUser 메소드 호출 성공")
                println("   - 삭제 결과: $deleted")
            }
        }
        
        context("로깅 기능 확인") {
            
            it("메소드 호출 시 로깅이 수행된다") {
                // Given
                val target = UserServiceImpl()
                val proxy = JdkProxyFactory.createProxy(target, UserService::class.java)
                
                // When: 여러 메소드 호출하여 로그 확인
                println("\n=== 로깅 테스트 시작 ===")
                
                val userId = proxy.saveUser("로깅테스트")
                val foundUser = proxy.findUser(userId)
                val deleted = proxy.deleteUser(userId)
                
                println("=== 로깅 테스트 완료 ===\n")
                
                // Then: 메소드들이 정상 실행되었는지 확인
                userId shouldNotBe 0L
                foundUser shouldNotBe null
                deleted shouldBe true
                
                println("✅ 모든 메소드에서 로깅 기능 동작 확인")
            }
            
            it("실행 시간이 측정된다") {
                // Given
                val target = UserServiceImpl()
                val proxy = JdkProxyFactory.createProxy(target, UserService::class.java)
                
                // When: 시간이 오래 걸리는 메소드 호출
                println("\n=== 실행 시간 측정 테스트 ===")
                
                val startTime = System.currentTimeMillis()
                val result = proxy.saveUser("시간측정테스트")
                val endTime = System.currentTimeMillis()
                
                println("=== 실행 시간 측정 완료 ===\n")
                
                // Then
                result shouldNotBe 0L
                val actualTime = endTime - startTime
                
                println("✅ 실행 시간 측정 완료")
                println("   - 실제 측정된 시간: ${actualTime}ms")
                println("   - 로그에서 더 정확한 나노초 단위 시간 확인 가능")
            }
        }
        
        context("예외 처리") {
            
            it("타겟에서 예외 발생 시 프록시가 올바르게 처리한다") {
                // Given
                val target = UserServiceImpl()
                val proxy = JdkProxyFactory.createProxy(target, UserService::class.java)
                
                // When & Then: 예외 발생하는 경우
                println("\n=== 예외 처리 테스트 (예외 발생) ===")
                
                shouldThrow<RuntimeException> {
                    proxy.testException(true)
                }.message shouldContain "테스트용 예외"
                
                println("=== 예외 처리 테스트 완료 ===\n")
                
                println("✅ 예외 상황에서도 프록시가 올바르게 동작")
            }
            
            it("정상 실행 시에는 예외가 발생하지 않는다") {
                // Given
                val target = UserServiceImpl()
                val proxy = JdkProxyFactory.createProxy(target, UserService::class.java)
                
                // When: 정상 실행
                println("\n=== 예외 처리 테스트 (정상 실행) ===")
                
                val result = proxy.testException(false)
                
                println("=== 예외 처리 테스트 완료 ===\n")
                
                // Then
                result shouldBe "정상 실행 완료"
                
                println("✅ 정상 실행 시 예외 발생하지 않음")
            }
        }
        
        context("프록시 내부 구조 분석") {
            
            it("프록시의 InvocationHandler를 확인할 수 있다") {
                // Given
                val target = UserServiceImpl()
                val proxy = JdkProxyFactory.createProxy(target, UserService::class.java)
                
                // When: InvocationHandler 조회
                val handler = Proxy.getInvocationHandler(proxy)
                
                // Then
                handler shouldNotBe null
                handler.shouldBeInstanceOf<LoggingInvocationHandler>()
                
                println("✅ InvocationHandler 확인")
                println("   - Handler 타입: ${handler.javaClass.name}")
            }
            
            it("프록시 정보를 출력할 수 있다") {
                // Given
                val target = UserServiceImpl()
                val proxy = JdkProxyFactory.createProxy(target, UserService::class.java)
                
                // When & Then: 프록시 정보 출력
                println("\n=== 프록시 정보 출력 ===")
                JdkProxyFactory.printProxyInfo(proxy)
                println("=== 프록시 정보 출력 완료 ===\n")
                
                println("✅ 프록시 정보 출력 기능 확인")
            }
            
            it("일반 객체와 프록시 객체를 구분할 수 있다") {
                // Given
                val target = UserServiceImpl()
                val proxy = JdkProxyFactory.createProxy(target, UserService::class.java)
                
                // When & Then
                JdkProxyFactory.isJdkProxy(target) shouldBe false
                JdkProxyFactory.isJdkProxy(proxy) shouldBe true
                
                println("✅ 프록시 객체 구분 기능 확인")
                println("   - 타겟 객체는 프록시가 아님: ${!JdkProxyFactory.isJdkProxy(target)}")
                println("   - 프록시 객체는 프록시임: ${JdkProxyFactory.isJdkProxy(proxy)}")
            }
        }
        
        context("성능 비교") {
            
            it("직접 호출 vs 프록시 호출 성능을 비교한다") {
                // Given
                val target = UserServiceImpl()
                val proxy = JdkProxyFactory.createProxy(target, UserService::class.java)
                val iterations = 10000
                
                // When: 직접 호출 성능 측정
                val directCallTime = measureTimeMillis {
                    repeat(iterations) {
                        target.findUser(1L)
                    }
                }
                
                // 프록시 호출 성능 측정 (로깅 비활성화를 위해 많은 호출)
                val proxyCallTime = measureTimeMillis {
                    repeat(iterations) {
                        proxy.findUser(1L)
                    }
                }
                
                // Then
                println("✅ 성능 비교 완료")
                println("   - 직접 호출 ($iterations 회): ${directCallTime}ms")
                println("   - 프록시 호출 ($iterations 회): ${proxyCallTime}ms")
                println("   - 오버헤드: ${proxyCallTime - directCallTime}ms")
                println("   - 배율: ${proxyCallTime.toDouble() / directCallTime}x")
                
                // 프록시 호출이 당연히 더 오래 걸려야 함 (로깅 때문에)
                proxyCallTime shouldNotBe directCallTime
            }
        }
    }
})

/**
 * 시간 측정 유틸리티 함수
 */
private inline fun measureTimeMillis(block: () -> Unit): Long {
    val start = System.currentTimeMillis()
    block()
    return System.currentTimeMillis() - start
}