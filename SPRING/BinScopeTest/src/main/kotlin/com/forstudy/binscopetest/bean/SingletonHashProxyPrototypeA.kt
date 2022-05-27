package com.forstudy.binscopetest.bean

import org.springframework.stereotype.Component

@Component
class SingletonHashProxyPrototypeA(val prototypeBean: ProxyedPrototypeScope) {

    fun logic() : String = prototypeBean.uuid ?: ""
}