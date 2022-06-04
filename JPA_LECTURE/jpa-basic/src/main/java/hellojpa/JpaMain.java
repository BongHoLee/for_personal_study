package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {
    public static void main(String[] args) {
        // persistence.xml에 설정한 persistence-unit name을 입력
        // EntityManagerFactory는 어플리케이션 로딩 시점에 단 한개만 존재해야 한다.
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        // 실제 DB에 저장하는 등의 '트랜잭션 단위'를 할 때 마다 이 'EntityManager'를 꼭 만들어줘야 한다.
        EntityManager em = emf.createEntityManager();

        // JPA는 동작할 때 '트랜잭션 단위로 실행'되어야 하기 때문에 'EntityTransaction'으로부터 트랜잭션 시작과 커밋/롤백을 명시해야 한다.
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {

            Member member = em.find(Member.class, 1L);
            member.setName("HelloJPA");
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }
        finally {
            em.close();
        }

        emf.close();

    }
}
