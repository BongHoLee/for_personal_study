package com.demo.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class Member {

    public Member() {}
    private Long id;
    private String name;
}
