package io.security.corespringsecurity.security.provider;

import io.security.corespringsecurity.security.common.FormWebAuthenticationDetails;
import io.security.corespringsecurity.security.service.AccountContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    // authenticate 메서드는 AuthenticationManager가 호출
    // 그리고 호출할 떄 Authentication 객체를 함꼐 전달.
    // 이 Authentication 객체에는 사용자가 입력한 ID, PASSWORD  정보가 담겨있기 때문에 추출이 가능함.
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 인증과 관련된 검증을 위한 구현을 하자
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        // UserDetailsService로부터 AccountContext를 가져온다.
        // UserDetailsService는 비즈니스 도메인 이하 계층으로부터 UserDetails를 만들어서 반환하는 일종의 어댑터 역할을 하는 듯 보인다.
        AccountContext accountContext = (AccountContext) userDetailsService.loadUserByUsername(username);

        // 패스워드 일치 여부에 대한 검증을 한다.
        if (!passwordEncoder.matches(password, accountContext.getAccount().getPassword())) {
            throw new BadCredentialsException("BadCredentialsException");
        }

        // 클라이언트가 전달한 디테일 정보를 가져와서 검증 처리
        FormWebAuthenticationDetails details = (FormWebAuthenticationDetails) authentication.getDetails();

        String secretKey = details.getSecretKey();
        if (secretKey == null || !secretKey.equals("secret")) {
            throw  new InsufficientAuthenticationException("InSufficientAuthenticationException");
        }

        // 인증에 성공하게 되면 Token을 만들어서 반환.
        // 비밀번호는 민감 정보이기 때문에 null로 전달
        return new UsernamePasswordAuthenticationToken(
                accountContext.getAccount(),
                null,
                accountContext.getAuthorities());

    }

    @Override
    public boolean supports(Class<?> authentication) {
        //. 파라미터로 전달되는 authentication의 타입과 우리가 구현한 CustomAuthenticationProvider가 사용할 토큰의 타입이 일치하는지 여부
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
