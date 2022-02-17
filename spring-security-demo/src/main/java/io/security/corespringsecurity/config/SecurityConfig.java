package io.security.corespringsecurity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity          // 오버라이딩에서 넘어오는 AuthenticationManagerBuilder가 global(bean)으로 넘어오게끔 하기 위함
                            // 이 AuthenticationManagerBuilder가 global(Bean) AuthenticationManager를 만듦
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        // 패스워드는 암호화된 방식으로 해야하는데 이를 제공하는 API가 PasswordEncoder
        String password = passwordEncoder().encode("1111");

        // In Memory 방식으로 사용자 생성한다.
        // 테스트 할 때 좋을 듯?
        // 각 유저에게 롤을 할당한다.
        auth.inMemoryAuthentication().withUser("user").password(password).roles("USER");
        auth.inMemoryAuthentication().withUser("manager").password(password).roles("USER", "MANAGER");
        auth.inMemoryAuthentication().withUser("admin").password(password).roles("USER", "MANAGER", "ADMIN");
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 우선 각 페이지와 페이지에 접근할 수 있는 권한을 설정
        http
                .authorizeRequests()

                .antMatchers("/").permitAll()
                .antMatchers("/mypage").hasRole("USER")
                .antMatchers("/messages").hasRole("MANAGER")
                .antMatchers("/config").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin();
    }
}
