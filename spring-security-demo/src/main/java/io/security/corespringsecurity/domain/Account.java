package io.security.corespringsecurity.domain;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

// DB에 저장하기 위한 엔티티
@Entity
@Data       // lombok에서 지원하는 어노테이션. getter setter가 컴파일 시점에 생성이 된다.
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    private String username;

    private String password;

    private String email;

    private String age;

    private String role;
}
