package jpabook.jpashop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional     // 테스트에서는 이게 있어야 롤백이 된다.
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void 회원가입() {
        // given
        Member member = new Member();
        member.setName("Lee");

        // when
        Long saveId = memberService.join(member);

        // then
        assertThat(member).isEqualTo(memberRepository.findOne(saveId));
    }

    @Test
    public void 회원_이름중복_예외() {
        // given
        Member member1 = new Member();
        member1.setName("lee");
        Member member2 = new Member();
        member2.setName("lee");

        // when
        memberService.join(member1);

        // then
        // @Repository 어노테이션 내에서 발생한 Exception이기 때문에 DataAccessException을 발생시켜준다.
        assertThrows(DataAccessException.class, () -> {
           memberService.join(member2);
        });
    }

}