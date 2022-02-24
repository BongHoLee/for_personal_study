package io.security.corespringsecurity.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    // 이 객체는 클라이언트가 인증(로그인)을 성공하기 전에 거쳐왔던 정보(http 헤더, 요청 URL 등)를 담는 SavedRequest 객체를 session에 담는 역할을 한다.
    private final RequestCache requestCache = new HttpSessionRequestCache();

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        setDefaultTargetUrl("/");

        // savedRequest 객체에는 클라이언트가 인증(로그인)을 성공하기 전에 거쳐왔던 정보(http 헤더, 요청 URL 등)를 담고 세션에 저장된다.
        // 이 메서드에서는 인증에 성공한 뒤이기 때문에 "이전에 접근하려 했던 자원이 있으면 savedRequest로부터 해당 url로 redirect 하게 해준다."
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            redirectStrategy.sendRedirect(request, response, targetUrl);
        } else {
            // 인증 전 요청했던 자원이 없다면 기본 URL로 전달
            redirectStrategy.sendRedirect(request, response, getDefaultTargetUrl());
        }
    }
}
