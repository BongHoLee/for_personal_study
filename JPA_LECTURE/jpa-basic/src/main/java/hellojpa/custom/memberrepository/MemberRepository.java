package hellojpa.custom.memberrepository;

import hellojpa.Member;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class MemberRepository {
    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

    private final MemberQuery<Long, Member> findById = (em, id) -> em.find(Member.class, id);
    private final MemberQuery<Member, Long> insert = (em, member) -> {
        em.persist(member);
        return member.getId();
    };
    private final MemberQuery<Long, Member> findAndUpdateData = (em, id) -> {
        Member findMember = findById.query(em, id);
        findMember.setData("data" + id);
        return findMember;
    };
    private final MemberQuery<Long, Member> findAndUpdateName = (em, id) ->  {
        Member findMember = findById.query(em, id);
        findMember.setName("leebongho" + id);
        return findMember;
    };

    public Member findById(Long id) {
        System.out.println("================ findById =================");
        return query(findById, id);
    }

    public Long insert(Member member) {
        System.out.println("================ insert =================");
        return query(insert, member);
    }

    public Member findAndUpdateData(Long id) {
        System.out.println("================ findAndUpdateData =================");
        return query(findAndUpdateData, id);
    }

    public Member findAndUpdateName(Long id) {
        System.out.println("================ findAndUpdateName =================");
        return query(findAndUpdateName, id);
    }

    private <T, R> R query(MemberQuery<T, R> memberQuery, T t) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();
        try {
            R query = memberQuery.query(em, t);
            tx.commit();
            return query;
        } catch (Exception e) {
            System.out.println("exception rollback");
            tx.rollback();
            return null;
        } finally {
            em.close();
        }

    }

}
