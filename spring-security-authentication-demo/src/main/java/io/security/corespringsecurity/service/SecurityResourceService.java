package io.security.corespringsecurity.service;

import io.security.corespringsecurity.domain.entity.Resources;
import io.security.corespringsecurity.repository.ResourcesRepository;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SecurityResourceService {

    private ResourcesRepository resourcesRepository;

    public SecurityResourceService(ResourcesRepository resourcesRepository) {
        this.resourcesRepository = resourcesRepository;
    }

    public LinkedHashMap<RequestMatcher, List<ConfigAttribute>> getResourceList() {

        LinkedHashMap<RequestMatcher, List<ConfigAttribute>> result = new LinkedHashMap<>();
        List<Resources> resourcesList = resourcesRepository.findAllResources();

        // 하나의 리소스에 접근 가능한 권한 리스트를 담는다.
        resourcesList.forEach(eachResource -> {
            List<ConfigAttribute> configAttributeList = new ArrayList<>();
            eachResource.getRoleSet().forEach(eachRole -> {
                configAttributeList.add(new SecurityConfig(eachRole.getRoleName()));
            });

            result.put(new AntPathRequestMatcher(eachResource.getResourceName()), configAttributeList);
        });

        return result;

    }
}
