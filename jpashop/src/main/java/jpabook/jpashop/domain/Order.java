package jpabook.jpashop.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    // Order와 Member는 N:1 관계
    // N쪽이 Orders 이므로 F.K가 있다.
    // 양방향 관계라면 F.K가 있는 쪽이 "연관관계의 주인"
    @ManyToOne
    @JoinColumn(name = "member_id")     // F.K 명이 member_id
    private Member member;
}
