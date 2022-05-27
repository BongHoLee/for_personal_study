package com.example.session.home.config;

import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

/**
 * Class desc.
 *
 * @author o118014_D
 * @since 2022-03-31
 */

public class ServletContainerInitializer extends AbstractHttpSessionApplicationInitializer {
    public ServletContainerInitializer() {
        super(HttpSessionConfig.class);
    }
}
