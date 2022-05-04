package jpabook.jpashop.domain.item;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.domain.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
public abstract class Item {

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;

    private Long price;

    private Long stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    // ==== 비즈니스 로직 ==== //

    /**
     * 재고 수량 증가
     * @param quantity
     */
    public void addStock(Long quantity) {
        this.stockQuantity += quantity;
    }

    /**
     * 재고 수량 감소
     * @param quantity
     */
    public void removeStock(Long quantity) {
        Long removed = this.stockQuantity - quantity;
        checkStockValidation(removed);
        this.stockQuantity = removed;
    }

    private void checkStockValidation(Long stock) {
        if (stock < 0) {
            throw new NotEnoughStockException("need more stock");
        }
    }
}
