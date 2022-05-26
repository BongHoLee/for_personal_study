package jpabook.jpashop;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class JpashopApplicationTests {

    @Autowired
    MemberRepository memberRepository;


    @Test
    @Transactional      // EntityManager에 대한 저장등의 변경은 항상 Transaction 내에서 이루어져야 한다.
                        // Transactional 어노테이션이 Test에 존재하면 테스트 수행 후 rollback을 한다. (옵션으로 변경이 가능하다.)
    public void testMember() {
        // given
        Member member = new Member();
        member.setUsername("memberA");

        // when
        Long saveId = memberRepository.save(member);
        Member findMember = memberRepository.find(saveId);

        // then
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());

    }


}
