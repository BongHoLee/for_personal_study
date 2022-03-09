package io.security.corespringsecurity.security.metadatasource;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

// 커스텀 SecurityMetadataSource
public class UrlFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {


    // 요청 리소스 : 권한 정보를 담는 Map
    private LinkedHashMap<RequestMatcher, List<ConfigAttribute>> requestMap = new LinkedHashMap<>();

    public UrlFilterInvocationSecurityMetadataSource(LinkedHashMap<RequestMatcher, List<ConfigAttribute>> requestMap) {
        this.requestMap = requestMap;
    }

    // 실제로 리소스 : 권한 매칭 정보를 가져오는, 구현이 필요한 메서드
    // FilterInvocation 타입을 지원하기 때문에 해당 타입의 파라미터가 들어온다.
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {

        HttpServletRequest request = ((FilterInvocation) object).getRequest();

        // [key(리소스) : value(리소스에 매칭되는 권한 정보 Collection)]
        for (Map.Entry<RequestMatcher, List<ConfigAttribute>> eachEntry : requestMap.entrySet()) {
            RequestMatcher matcher = eachEntry.getKey();

            // 클라이언트의 request가 리소스 정보와 같다면, 해당 리소스에 대한 권한 정보 Collection을 반환해준다.
            if (matcher.matches(request)) {
                return eachEntry.getValue();
            }
        }

        return null;
    }


    // 기본적으로 제공하는 구현체인 DefaultFilterInvocationSecurityMetadataSource 코드를 그대로 사용
    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        Set<ConfigAttribute> allAttributes = new HashSet<>();
        requestMap.values().forEach(allAttributes::addAll);
        return allAttributes;
    }


    // FilterInvocation 타입에 대해서 지원
    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
}
