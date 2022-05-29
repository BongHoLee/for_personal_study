package jpabook.jpashop.repository;

import java.util.List;
import javax.persistence.EntityManager;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    public void save(Item item) {

        // item은 JPA에 저장되기 전 까지 id 값이 없다.(@GeneratedValue임)
        if (item.getId() == null) {
            // null이라면 완전히 새롭게 생성하는 녀석임
            em.persist(item);
        } else {
            // 이미 등록된 경우는 update와 비슷한 프로세
            em.merge(item);
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select item from Item item", Item.class).getResultList();
    }
}
