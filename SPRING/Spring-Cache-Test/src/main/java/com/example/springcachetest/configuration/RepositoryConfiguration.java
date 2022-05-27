package com.example.springcachetest.configuration;

import com.example.springcachetest.repository.JpaMemberRepository;
import com.example.springcachetest.repository.MemberRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

@Configuration
public class RepositoryConfiguration {

    private final EntityManager em;

    public RepositoryConfiguration(EntityManager em) {
        this.em = em;
    }

    @Bean
    public MemberRepository memberRepository() {
        return new JpaMemberRepository(em);
    }
}
