package jpabook.jpashop.domain;


import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Member {

    // GeneratedValue는 말 그대로 값 생성이 된다는 의미? sequence 값 등
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    // Value type인 Embedded라고 선언
    @Embedded
    private Address address;

    // Member와 Order는 1:N 관계
    // 양방향 연관관계로 설정되었지만, F.K가 없는 '1'쪽 이므로 연관관계의 주인이 아님
    // 따라서 mappedby로 선언 (Order 필드에 있는 member의 거울일 뿐이야 -> 읽기 전용, 이걸 변경한다고 Order의 F.K가 변경되지 않는다.)
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();


}
