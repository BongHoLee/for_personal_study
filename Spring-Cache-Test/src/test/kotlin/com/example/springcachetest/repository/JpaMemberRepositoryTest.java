package com.example.springcachetest.repository;

import com.example.springcachetest.domain.Member;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class JpaMemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("유저 저장 테스트")
    void save() {
        Member member = new Member();
        member.setId(101L);
        member.setName("leebongho2");

        Member saved = memberRepository.save(member);

        assertThat(saved).isEqualTo(member);
    }

    @Test
    @DisplayName("테이블에 저장된 유저 id 일치 여부 테스트")
    void findById() {
        Member member = memberRepository.findById(100L).get();

        assertThat(member.getId()).isEqualTo(100);
        assertThat(member.getName()).isEqualTo("leebongho");
    }

    @Test
    @DisplayName("유저 이름과 id 일치 여부 테스트")
    void findByName() {
        Member member = memberRepository.findByName("lee").get();

        assertThat(member.getId()).isEqualTo(102);
    }

    @Test
    @DisplayName("유저 취미와 ID 일치 여부 테스트")
    void findByHobby() {

        List<Member> members = memberRepository.findByHobby("soccer");
        assertThat(members).anyMatch(each -> each.getId() == 100);
    }

    @Test
    void findAll() {
    }
}