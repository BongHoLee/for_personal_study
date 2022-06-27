package hellojpa.custom.memberrepository;

import javax.persistence.EntityManager;

@FunctionalInterface
public interface MemberQuery<T, R> {
    R query(EntityManager em, T t);
}
