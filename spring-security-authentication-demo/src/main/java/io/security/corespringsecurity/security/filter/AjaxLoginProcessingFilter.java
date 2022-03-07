package io.security.corespringsecurity.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.security.corespringsecurity.domain.dto.AccountDto;
import io.security.corespringsecurity.security.token.AjaxAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AjaxLoginProcessingFilter extends AbstractAuthenticationProcessingFilter {

    // 요청의 Content-Type이 application-json 타입이기 때문에 객체로 매핑하기 위함
    private final ObjectMapper mapper = new ObjectMapper();


    public AjaxLoginProcessingFilter() {
        // 해당 URL로 요청을 할 때 이 필터가 작동할 수 있도록 설정한다.
        super(new AntPathRequestMatcher("/api/login"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        // ajax 요청이 아닌 경우 예외
        if (!isAjax(request)) {
            throw new IllegalStateException("Authentication is not supported");
        }

        // HTTP REQUEST BODY부를 읽어서 AccuontDto 타입으로 매핑
        AccountDto accountDto = mapper.readValue(request.getReader(), AccountDto.class);
        request.setAttribute("userId", accountDto.getUsername());

        if (!StringUtils.hasText(accountDto.getUsername()) || !StringUtils.hasText(accountDto.getPassword())) {
            throw new IllegalArgumentException("Username or Password is not empty");
        }

        // 클라이언트가 전달한 id, password 정보를 우선 Authentication에 할당
        AjaxAuthenticationToken authenticationToken = new AjaxAuthenticationToken(
                accountDto.getUsername(),
                accountDto.getPassword()
        );

        // 이제 이 만들어진 AuthenticationToken을 AuthenticationManager에게 전달
        // AuthenticationManager는 이 Token을 처리 가능한 AuthenticationProvider에게 전달하여 인증을 처리
        return getAuthenticationManager().authenticate(authenticationToken);

    }

    private boolean isAjax(HttpServletRequest request) {

        return request.getHeader("Content-Type").equals("application/json") ||
                request.getHeader("X-Requested-With").equals("XMLHttpRequest");

    }

}
