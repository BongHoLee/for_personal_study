package bong.training.phase1.jdk

/**
 * JDK 동적 프록시 데모 실행 클래스
 * 
 * 이 클래스를 통해 JDK 동적 프록시의 동작을 직관적으로 확인할 수 있습니다.
 * 테스트와 달리 실제 실행 가능한 메인 함수를 제공하여 
 * 콘솔에서 프록시 동작 과정을 step-by-step으로 관찰할 수 있습니다.
 */
class JdkProxyDemo {
    
    companion object {
        
        /**
         * JDK 동적 프록시 데모 실행
         */
        @JvmStatic
        fun main(args: Array<String>) {
            println("🚀 JDK 동적 프록시 데모 시작!")
            println("=".repeat(50))
            
            // 1단계: 타겟 객체 생성
            demonstrateTargetCreation()
            
            // 2단계: 프록시 생성
            val proxy = demonstrateProxyCreation()
            
            // 3단계: 프록시를 통한 메소드 호출
            demonstrateProxyMethodCalls(proxy)
            
            // 4단계: 예외 상황 처리
            demonstrateExceptionHandling(proxy)
            
            // 5단계: 프록시 분석
            demonstrateProxyAnalysis(proxy)
            
            // 6단계: 성능 비교
            demonstratePerformanceComparison()
            
            println("=".repeat(50))
            println("✅ JDK 동적 프록시 데모 완료!")
        }
        
        private fun demonstrateTargetCreation() {
            println("\n📌 1단계: 타겟 객체 생성")
            println("-".repeat(30))
            
            val target = UserServiceImpl()
            println("✓ 타겟 객체 생성: ${target.javaClass.name}")
            println("✓ 구현된 인터페이스: ${target.javaClass.interfaces.contentToString()}")
            
            // 타겟 직접 호출 테스트
            println("\n[타겟 직접 호출 테스트]")
            val directResult = target.findUser(999L)
            println("직접 호출 결과: $directResult")
        }
        
        private fun demonstrateProxyCreation(): UserService {
            println("\n📌 2단계: JDK 동적 프록시 생성")
            println("-".repeat(30))
            
            val target = UserServiceImpl()
            println("타겟 준비 완료: ${target.javaClass.simpleName}")
            
            println("\nProxy.newProxyInstance() 호출...")
            val proxy = JdkProxyFactory.createProxy(target, UserService::class.java)
            
            println("프록시 생성 결과:")
            println("  - 프록시 클래스: ${proxy.javaClass.name}")
            println("  - JDK 프록시 여부: ${JdkProxyFactory.isJdkProxy(proxy)}")
            println("  - 인터페이스 타입 체크: ${proxy is UserService}")
            
            return proxy
        }
        
        private fun demonstrateProxyMethodCalls(proxy: UserService) {
            println("\n📌 3단계: 프록시를 통한 메소드 호출")
            println("-".repeat(30))
            
            println("\n[1] saveUser() 호출:")
            val userId = proxy.saveUser("JDK프록시테스트")
            println("→ 반환된 사용자 ID: $userId")
            
            println("\n[2] findUser() 호출:")
            val foundUser = proxy.findUser(userId)
            println("→ 조회 결과: $foundUser")
            
            println("\n[3] deleteUser() 호출:")
            val deleted = proxy.deleteUser(userId)
            println("→ 삭제 결과: $deleted")
            
            println("\n💡 위의 모든 호출에서 LoggingInvocationHandler.invoke()가 실행되어")
            println("   실행 시간 측정, 로깅 등의 부가 기능이 자동으로 적용되었습니다!")
        }
        
        private fun demonstrateExceptionHandling(proxy: UserService) {
            println("\n📌 4단계: 예외 상황 처리")
            println("-".repeat(30))
            
            println("\n[정상 실행 케이스]")
            try {
                val result = proxy.testException(false)
                println("→ 결과: $result")
            } catch (e: Exception) {
                println("→ 예외: ${e.message}")
            }
            
            println("\n[예외 발생 케이스]")
            try {
                val result = proxy.testException(true)
                println("→ 결과: $result")
            } catch (e: Exception) {
                println("→ 예외 발생: ${e.message}")
                println("💡 InvocationHandler에서 예외를 적절히 처리하여 클라이언트에게 전달했습니다.")
            }
        }
        
        private fun demonstrateProxyAnalysis(proxy: UserService) {
            println("\n📌 5단계: 프록시 내부 구조 분석")
            println("-".repeat(30))
            
            JdkProxyFactory.printProxyInfo(proxy)
            
            // InvocationHandler 분석
            val handler = JdkProxyFactory.getInvocationHandler(proxy)
            println("\nInvocationHandler 분석:")
            println("  - 핸들러 타입: ${handler?.javaClass?.name}")
            println("  - 핸들러가 LoggingInvocationHandler인가: ${handler is LoggingInvocationHandler}")
        }
        
        private fun demonstratePerformanceComparison() {
            println("\n📌 6단계: 성능 비교")
            println("-".repeat(30))
            
            val target = UserServiceImpl()
            val proxy = JdkProxyFactory.createProxy(target, UserService::class.java)
            val iterations = 100000
            
            println("테스트 조건: $iterations 회 반복 호출")
            
            // 직접 호출 성능 측정
            print("직접 호출 측정 중...")
            val directTime = measureNanoTime {
                repeat(iterations) {
                    target.findUser(1L)
                }
            }
            println(" 완료!")
            
            // 프록시 호출 성능 측정  
            print("프록시 호출 측정 중...")
            val proxyTime = measureNanoTime {
                repeat(iterations) {
                    proxy.findUser(1L)
                }
            }
            println(" 완료!")
            
            val directTimeMs = directTime / 1_000_000.0
            val proxyTimeMs = proxyTime / 1_000_000.0
            val overhead = proxyTimeMs - directTimeMs
            val ratio = proxyTimeMs / directTimeMs
            
            println("\n성능 측정 결과:")
            println("  - 직접 호출: ${"%.2f".format(directTimeMs)}ms")
            println("  - 프록시 호출: ${"%.2f".format(proxyTimeMs)}ms")
            println("  - 오버헤드: ${"%.2f".format(overhead)}ms")
            println("  - 속도 비율: ${"%.2f".format(ratio)}x")
            
            println("\n💡 프록시는 추가적인 처리(로깅, 시간측정 등)로 인해")
            println("   당연히 직접 호출보다 느립니다. 하지만 얻는 이점이 더 큽니다!")
        }
        
        private inline fun measureNanoTime(block: () -> Unit): Long {
            val start = System.nanoTime()
            block()
            return System.nanoTime() - start
        }
    }
}