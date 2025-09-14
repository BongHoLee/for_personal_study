package bong.training.phase1.jdk

import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

/**
 * JDK 동적 프록시 생성을 담당하는 팩토리 클래스
 * 
 * 이 클래스의 핵심 역할:
 * 1. 타겟 객체와 InvocationHandler를 받아서
 * 2. Proxy.newProxyInstance()를 사용해 동적으로 프록시 객체 생성
 * 3. 생성된 프록시를 클라이언트에게 반환
 */
class JdkProxyFactory {
    
    companion object {
        private val logger = LoggerFactory.getLogger(JdkProxyFactory::class.java)
        
        /**
         * JDK 동적 프록시 생성 (로깅 기능 포함)
         * 
         * @param target 실제 비즈니스 로직을 담은 타겟 객체
         * @param targetInterface 타겟이 구현한 인터페이스 (프록시가 구현할 인터페이스)
         * @return 프록시 객체 (인터페이스 타입으로 반환)
         */
        fun <T> createProxy(target: T, targetInterface: Class<T>): T {
            logger.info("=== JDK 동적 프록시 생성 시작 ===")
            logger.info("타겟 객체: {}", target!!.javaClass.name)
            logger.info("인터페이스: {}", targetInterface.name)
            
            // 1. 인터페이스 검증
            validateInterface(target, targetInterface)
            
            // 2. InvocationHandler 생성
            val invocationHandler = LoggingInvocationHandler(target)
            
            // 3. 프록시 생성을 위한 3가지 필수 요소 준비
            val classLoader = target.javaClass.classLoader  // 클래스 로더
            val interfaces = arrayOf(targetInterface)        // 구현할 인터페이스 배열
            // val handler = invocationHandler               // 호출 핸들러 (이미 준비됨)
            
            logger.debug("프록시 생성 파라미터:")
            logger.debug("  - ClassLoader: {}", classLoader)
            logger.debug("  - Interfaces: {}", interfaces.contentToString())
            logger.debug("  - Handler: {}", invocationHandler.javaClass.name)
            
            // 4. JDK의 Proxy.newProxyInstance()를 사용해 동적 프록시 생성
            // 이 메소드는 런타임에 다음과 같은 작업을 수행:
            // - 지정된 인터페이스를 구현하는 새로운 클래스를 동적으로 생성
            // - 이 클래스의 모든 메소드는 InvocationHandler.invoke()를 호출하도록 구현됨
            // - 생성된 클래스의 인스턴스를 반환
            @Suppress("UNCHECKED_CAST")
            val proxy = Proxy.newProxyInstance(
                classLoader,
                interfaces,
                invocationHandler
            ) as T
            
            logger.info("✅ 프록시 생성 완료: {}", proxy!!.javaClass.name)
            logger.info("   - 프록시인지 확인: {}", Proxy.isProxyClass(proxy.javaClass))
            logger.info("   - 구현된 인터페이스: {}", proxy.javaClass.interfaces.contentToString())
            
            return proxy
        }
        
        /**
         * 다중 인터페이스를 지원하는 프록시 생성
         * 
         * @param target 타겟 객체
         * @param interfaces 구현할 인터페이스들
         * @return 프록시 객체
         */
        fun <T> createProxy(target: T, vararg interfaces: Class<*>): T {
            logger.info("=== 다중 인터페이스 JDK 프록시 생성 ===")
            logger.info("타겟: {}, 인터페이스 수: {}", target!!.javaClass.name, interfaces.size)
            
            val invocationHandler = LoggingInvocationHandler(target)
            
            @Suppress("UNCHECKED_CAST")
            val proxy = Proxy.newProxyInstance(
                target.javaClass.classLoader,
                interfaces,
                invocationHandler
            ) as T
            
            logger.info("✅ 다중 인터페이스 프록시 생성 완료")
            return proxy
        }
        
        /**
         * 커스텀 InvocationHandler를 사용하는 프록시 생성
         */
        fun <T> createProxyWithCustomHandler(
            target: T,
            targetInterface: Class<T>,
            customHandler: InvocationHandler
        ): T {
            logger.info("=== 커스텀 핸들러 JDK 프록시 생성 ===")
            
            @Suppress("UNCHECKED_CAST")
            val proxy = Proxy.newProxyInstance(
                target!!.javaClass.classLoader,
                arrayOf(targetInterface),
                customHandler
            ) as T
            
            logger.info("✅ 커스텀 핸들러 프록시 생성 완료")
            return proxy
        }
        
        /**
         * 프록시 생성 전 인터페이스 유효성 검증
         */
        private fun <T> validateInterface(target: T, targetInterface: Class<T>) {
            // 1. 타겟이 null이 아닌지 확인
            requireNotNull(target) { "타겟 객체는 null일 수 없습니다." }
            
            // 2. 인터페이스인지 확인
            require(targetInterface.isInterface) {
                "${targetInterface.name}는 인터페이스가 아닙니다. JDK 프록시는 인터페이스가 필요합니다."
            }
            
            // 3. 타겟이 해당 인터페이스를 구현하는지 확인
            require(targetInterface.isAssignableFrom(target.javaClass)) {
                "타겟 객체 ${target.javaClass.name}는 ${targetInterface.name} 인터페이스를 구현하지 않습니다."
            }
            
            logger.debug("✅ 인터페이스 검증 통과: {}", targetInterface.name)
        }
        
        /**
         * 주어진 객체가 JDK 동적 프록시인지 확인
         */
        fun isJdkProxy(obj: Any): Boolean {
            return Proxy.isProxyClass(obj.javaClass)
        }
        
        /**
         * 프록시의 InvocationHandler를 반환
         */
        fun getInvocationHandler(proxy: Any): InvocationHandler? {
            return if (isJdkProxy(proxy)) {
                Proxy.getInvocationHandler(proxy)
            } else {
                null
            }
        }
        
        /**
         * 프록시 정보를 출력하는 유틸리티 메소드
         */
        fun printProxyInfo(obj: Any) {
            logger.info("=== 프록시 정보 ===")
            logger.info("객체 클래스: {}", obj.javaClass.name)
            logger.info("JDK 프록시 여부: {}", isJdkProxy(obj))
            
            if (isJdkProxy(obj)) {
                logger.info("구현된 인터페이스:")
                obj.javaClass.interfaces.forEach { intf ->
                    logger.info("  - {}", intf.name)
                }
                
                val handler = getInvocationHandler(obj)
                logger.info("InvocationHandler: {}", handler?.javaClass?.name ?: "없음")
            }
        }
    }
}