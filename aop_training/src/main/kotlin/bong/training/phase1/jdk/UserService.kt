package bong.training.phase1.jdk

/**
 * JDK 동적 프록시 실습을 위한 사용자 서비스 인터페이스
 * 
 * JDK 동적 프록시의 핵심 특징:
 * 1. 반드시 인터페이스가 있어야 함 (인터페이스 기반 프록시)
 * 2. 프록시와 실제 구현체 모두 이 인터페이스를 구현
 * 3. 클라이언트는 인터페이스 타입으로 프록시를 사용
 */
interface UserService {
    
    /**
     * 사용자 조회 메소드
     * @param id 사용자 ID
     * @return 사용자 정보 문자열
     */
    fun findUser(id: Long): String
    
    /**
     * 사용자 저장 메소드
     * @param name 사용자 이름
     * @return 생성된 사용자 ID
     */
    fun saveUser(name: String): Long
    
    /**
     * 사용자 삭제 메소드
     * @param id 삭제할 사용자 ID
     * @return 삭제 성공 여부
     */
    fun deleteUser(id: Long): Boolean
    
    /**
     * 예외 발생 테스트를 위한 메소드
     * @param shouldThrow 예외 발생 여부
     * @return 결과 메시지
     */
    fun testException(shouldThrow: Boolean): String
}