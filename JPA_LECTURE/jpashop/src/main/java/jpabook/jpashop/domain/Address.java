package jpabook.jpashop.domain;

import javax.persistence.Embeddable;
import lombok.Getter;

// JPA의 내장 타입
// DDD에서의 Value type
@Embeddable
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    // JPA는 내부적으로 리플렉션을 사용해서 인스턴스를 생성하기 때문에 기본 생성자가 필요.
    // 외부에서 실수로 생성하는 일 없게 안전하게 protected 접근 지정자로 두자.
    protected Address() {}

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
