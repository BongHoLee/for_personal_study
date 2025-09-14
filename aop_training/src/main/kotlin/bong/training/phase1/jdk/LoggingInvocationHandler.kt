package bong.training.phase1.jdk

import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * JDK 동적 프록시의 핵심: InvocationHandler 구현체
 * 
 * JDK 동적 프록시 동작 원리:
 * 1. 클라이언트가 프록시 객체의 메소드를 호출
 * 2. 모든 메소드 호출이 이 InvocationHandler.invoke()로 위임됨
 * 3. invoke() 메소드에서 부가 기능을 수행하고 실제 타겟 메소드 호출
 * 4. 결과를 클라이언트에게 반환
 */
class LoggingInvocationHandler(
    private val target: Any  // 실제 비즈니스 로직을 담은 타겟 객체
) : InvocationHandler {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * 프록시의 모든 메소드 호출이 이 메소드로 위임됨
     * 
     * @param proxy 프록시 객체 자체 (보통 사용하지 않음)
     * @param method 호출된 메소드 정보 (Method 객체)
     * @param args 메소드에 전달된 파라미터 배열
     * @return 메소드 실행 결과
     */
    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
        val methodName = method.name
        val className = target.javaClass.simpleName
        val argsString = args?.joinToString(", ") ?: "없음"
        
        // ============================================
        // 1단계: 메소드 실행 전 부가 기능 (Before Advice)
        // ============================================
        logger.info("🚀 [PROXY] 메소드 호출 시작")
        logger.info("   - 클래스: {}", className)
        logger.info("   - 메소드: {}", methodName)
        logger.info("   - 파라미터: [{}]", argsString)
        logger.info("   - 호출 시각: {}", System.currentTimeMillis())
        
        val startTime = System.nanoTime()
        
        try {
            // ============================================
            // 2단계: 실제 타겟 메소드 실행
            // ============================================
            logger.debug("   - 실제 타겟 메소드 {} 호출 중...", methodName)
            
            // 리플렉션을 사용해 타겟 객체의 실제 메소드를 호출
            // method.invoke(target, *args)는 다음과 같이 동작:
            // - target 객체의 method를 args 파라미터로 호출
            // - 예: target.findUser(1L) 형태로 실행됨
            val result = method.invoke(target, *(args ?: arrayOf()))
            
            // ============================================
            // 3단계: 메소드 정상 완료 후 부가 기능 (After Returning)
            // ============================================
            val endTime = System.nanoTime()
            val executionTime = (endTime - startTime) / 1_000_000.0  // 나노초 -> 밀리초
            
            logger.info("✅ [PROXY] 메소드 정상 완료")
            logger.info("   - 실행 시간: ${"%.2f".format(executionTime)}ms")
            logger.info("   - 반환 값: {}", result ?: "null")
            logger.info("   - 완료 시각: {}", System.currentTimeMillis())
            
            // 성능 모니터링: 느린 메소드 감지
            if (executionTime > 100.0) {
                logger.warn("⚠️ [PERFORMANCE] 느린 메소드 감지: {}ms > 100ms", executionTime)
            }
            
            return result
            
        } catch (exception: Exception) {
            // ============================================
            // 4단계: 예외 발생 시 부가 기능 (After Throwing)
            // ============================================
            val endTime = System.nanoTime()
            val executionTime = (endTime - startTime) / 1_000_000.0
            
            logger.error("❌ [PROXY] 메소드 실행 중 예외 발생")
            logger.error("   - 실행 시간: ${"%.2f".format(executionTime)}ms")
            logger.error("   - 예외 타입: {}", exception.javaClass.simpleName)
            logger.error("   - 예외 메시지: {}", exception.message)
            
            // 예외를 다시 던져서 클라이언트가 처리할 수 있도록 함
            // InvocationTargetException을 벗겨서 원본 예외만 던짐
            if (exception.cause != null) {
                throw exception.cause!!
            } else {
                throw exception
            }
            
        } finally {
            // ============================================
            // 5단계: 항상 실행되는 부가 기능 (Finally)
            // ============================================
            logger.debug("🏁 [PROXY] 메소드 {} 처리 완료 (성공/실패 무관)", methodName)
        }
    }
}