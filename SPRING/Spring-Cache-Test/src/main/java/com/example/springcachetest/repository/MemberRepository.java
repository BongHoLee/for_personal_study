package com.example.springcachetest.repository;

import com.example.springcachetest.domain.Member;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    Member save(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByName(String name);
    List<Member> findByHobby(String hobby);
    List<Member> findAll();
}
