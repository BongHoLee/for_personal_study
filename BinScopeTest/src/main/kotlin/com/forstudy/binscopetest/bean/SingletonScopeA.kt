package com.forstudy.binscopetest.bean

import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class SingletonScopeA(val prototypeBean: PrototypeScope) {

    @PostConstruct
    fun init() {
        println("Singleton Created : $this")
    }

    @PreDestroy
    fun close() {
        println("Singleton Destroyed : $this")
    }

    fun printPrototype() {
        print("$prototypeBean")
    }

}