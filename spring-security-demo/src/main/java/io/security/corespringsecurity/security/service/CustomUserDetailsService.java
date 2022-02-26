package io.security.corespringsecurity.security.service;

import io.security.corespringsecurity.domain.Account;
import io.security.corespringsecurity.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// 스프링 시큐리티 설정에서 이 클래스를 사용하도록 설정해야 한다.
@Service("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // AuthenticationProvider가 호출
    // 유저의 인증, 인가와 관련된 정보를 UserDetails에 담아서 반환
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 데이터 계층으로부터 유저 정보를 가져온다.
        Account account = userRepository.findByUsername(username);

        if (account == null) {
            throw new UsernameNotFoundException("usernameNotFoundException");
        }

        // 권한 정보를 전달해야한다.
        List<GrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(account.getRole()));

        return new AccountContext(account, roles);
    }
}
