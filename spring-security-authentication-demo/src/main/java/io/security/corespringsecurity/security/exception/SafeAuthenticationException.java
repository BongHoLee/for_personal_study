package io.security.corespringsecurity.security.exception;

import org.springframework.security.core.AuthenticationException;

public class SafeAuthenticationException extends AuthenticationException {

    public SafeAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SafeAuthenticationException(String msg) {
        super(msg);
    }
}
