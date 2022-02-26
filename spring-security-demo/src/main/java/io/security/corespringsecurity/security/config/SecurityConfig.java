package io.security.corespringsecurity.security.config;

import io.security.corespringsecurity.security.filter.AjaxLoginProcessingFilter;
import io.security.corespringsecurity.security.handler.CustomAccessDeniedHandler;
import io.security.corespringsecurity.security.provider.CustomAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity          // 오버라이딩에서 넘어오는 AuthenticationManagerBuilder가 global(bean)으로 넘어오게끔 하기 위함
                            // 이 AuthenticationManagerBuilder가 global(Bean) AuthenticationManager를 만듦
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> formAuthenticationDetailsSource;

    @Autowired
    private AuthenticationSuccessHandler successHandler;

    @Autowired
    private AuthenticationFailureHandler failureHandler;


    // 직접 구현한 userDetailsService 구현체를 인증에 사용하도록 설정
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }



    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



    @Bean
    public AuthenticationProvider authenticationProvider() {

        // 직접 구현한 CustomAuthenticationProvider를 Bean으로 생성.
        return new CustomAuthenticationProvider(userDetailsService, passwordEncoder());
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        CustomAccessDeniedHandler accessDeniedHandler = new CustomAccessDeniedHandler();
        accessDeniedHandler.setErrorPage("/denied");

        return accessDeniedHandler;
    }


    // Filter를 등록하면서 이 필터가 사용할 AuthenticationManager를 등록해줘야 한다.
    @Bean
    public AjaxLoginProcessingFilter ajaxLoginProcessingFilter() throws Exception {
        AjaxLoginProcessingFilter ajaxLoginProcessingFilter = new AjaxLoginProcessingFilter();
        ajaxLoginProcessingFilter.setAuthenticationManager(this.authenticationManagerBean());
        return ajaxLoginProcessingFilter;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 우선 각 페이지와 페이지에 접근할 수 있는 권한을 설정
        http
                .authorizeRequests()
                .antMatchers("/", "/users", "user/login/**", "/login*", "/denied*").permitAll()
                .antMatchers("/mypage").hasRole("USER")
                .antMatchers("/messages").hasRole("MANAGER")
                .antMatchers("/config").hasRole("ADMIN")
                .anyRequest().authenticated()
        .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login_proc")
                .authenticationDetailsSource(formAuthenticationDetailsSource)
                .defaultSuccessUrl("/")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
        .and()
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler())
                .accessDeniedPage("/denied")
                .accessDeniedHandler(accessDeniedHandler())
        .and()
                .addFilterBefore(ajaxLoginProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                ;
        http.csrf().disable();  // 기본적으로는 켜져있다.

    }
}
