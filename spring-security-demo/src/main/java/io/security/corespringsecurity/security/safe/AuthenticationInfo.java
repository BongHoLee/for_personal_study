package io.security.corespringsecurity.security.safe;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class AuthenticationInfo {
    private Boolean succeed = false;
    private AccountErrTypeCode errCode = AccountErrTypeCode.OTHER;
    private Map<String, Object> data = new HashMap<>();
}
