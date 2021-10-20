package com.forstudy.binscopetest.bean

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
class ProxyedPrototypeScope {
    var uuid: String? = ""


    @PostConstruct
    fun init() {
        uuid = UUID.randomUUID().toString()
        println("PrototypeBean created : $this uuid : $uuid")
    }

    @PreDestroy
    fun close() {
        println("Prototype Bean Destroyed : $this")
    }
}