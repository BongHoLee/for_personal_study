package jpabook.jpashop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class JpashopApplicationTests {



    @Test
    @Transactional      // EntityManager에 대한 저장등의 변경은 항상 Transaction 내에서 이루어져야 한다.     // Transactional 어노테이션이 Test에 존재하면 테스트 수행 후 rollback을 한다. (옵션으로 변경이 가능하다.)
    public void testMember() {

    }
}
