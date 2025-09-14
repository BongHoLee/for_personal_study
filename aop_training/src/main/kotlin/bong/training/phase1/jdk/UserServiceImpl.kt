package bong.training.phase1.jdk

import org.slf4j.LoggerFactory

/**
 * UserService 인터페이스의 실제 구현체
 * 
 * 실제 비즈니스 로직을 담고 있는 타겟 객체(Target Object)
 * JDK 동적 프록시에서는 이 객체를 감싸는 프록시가 생성됨
 */
class UserServiceImpl : UserService {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    // 간단한 메모리 저장소 (실습용)
    private val users = mutableMapOf<Long, String>()
    private var nextId = 1L
    
    /**
     * 사용자 조회 실제 구현
     * 
     * 이 메소드가 프록시를 통해 호출될 때:
     * 1. 클라이언트가 proxy.findUser(id) 호출
     * 2. InvocationHandler.invoke() 메소드가 호출됨
     * 3. InvocationHandler에서 이 메소드를 method.invoke(target, args)로 호출
     */
    override fun findUser(id: Long): String {
        logger.info("[TARGET] UserServiceImpl.findUser() 실행 - id: {}", id)
        
        // 실제 비즈니스 로직
        return users[id] ?: "User-$id (not found)"
    }
    
    /**
     * 사용자 저장 실제 구현
     */
    override fun saveUser(name: String): Long {
        logger.info("[TARGET] UserServiceImpl.saveUser() 실행 - name: {}", name)
        
        val id = nextId++
        users[id] = name
        
        // 실제 데이터베이스 저장을 시뮬레이션하기 위한 약간의 지연
        Thread.sleep(10)
        
        return id
    }
    
    /**
     * 사용자 삭제 실제 구현
     */
    override fun deleteUser(id: Long): Boolean {
        logger.info("[TARGET] UserServiceImpl.deleteUser() 실행 - id: {}", id)
        
        return users.remove(id) != null
    }
    
    /**
     * 예외 발생 테스트 메소드
     * 프록시가 예외 상황을 어떻게 처리하는지 확인하기 위함
     */
    override fun testException(shouldThrow: Boolean): String {
        logger.info("[TARGET] UserServiceImpl.testException() 실행 - shouldThrow: {}", shouldThrow)
        
        return if (shouldThrow) {
            throw RuntimeException("테스트용 예외 발생!")
        } else {
            "정상 실행 완료"
        }
    }
    
    /**
     * 현재 저장된 사용자 수 반환 (디버깅용)
     */
    fun getUserCount(): Int = users.size
}