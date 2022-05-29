package jpabook.jpashop.service;

import java.util.List;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)      // 기본적으로 데이터 변경은 트랜잭션 내에서!
//@AllArgsConstructor                  // 모든 필드에 대한 Constructor Parameter 정의
@RequiredArgsConstructor               // final로 선언된 필드에 대해 Constructor Parameter 정
public class MemberService {

    // 생성자가 하나일 경우 스프링이 자동으로 생성자 파라미터들에 대해 의존성 주입을 해준다!

    // 컴파일 시점에 확인을 위해 final을 붙여주는게 좋다.
    private final MemberRepository memberRepository;

    // 회원 가입
    @Transactional // 회원가입은 readOnly가 아니므로 재지정!
    public Long join(Member member) {
        memberRepository.save(member);

        // EntityManager에 save 하는 순간 영속성에 올려(?)놓기 때문에 getId 데이터가 존재함이 보장된다.
        return member.getId();
    }

    // 회원 전체 조회

    public List<Member> findMembers() {
        return memberRepository.findAll();
    }


    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }
}
