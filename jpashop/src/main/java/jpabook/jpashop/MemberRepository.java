package jpabook.jpashop;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {

    // 해당 어노테이션이 있으면 Spring Boot가 EntityManager를 주입을 해준다.
    // EntityManager를 생성하는 코드 역시 Spring Boot가 이미 다 만들어 놨기 때문에 그냥 사용만 하면 된다.
    @PersistenceContext
    private EntityManager em;

    // CQRS에 따라 return값은 원래 없는게 좋지만, ID정도는 반환하는것도 나쁘지 않다.
    public Long save(Member member) {
        em.persist(member);
        return member.getId();
    }

    public Member find(Long memberId) {
        return em.find(Member.class, memberId);
    }
}
