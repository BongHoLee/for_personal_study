package com.forstudy.binscopetest.bean


import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.assertj.core.api.Assertions.assertThat

class SingletonScopeTest {

    val ac = AnnotationConfigApplicationContext(AppConfig::class.java)

    @Test
    @DisplayName("컴포넌트 스캔을 이용한 빈 등록 확인")
    fun componentScanTest() {
        val beanDefinitionNames = ac.beanDefinitionNames
        for (beanDefinitionName in beanDefinitionNames) {

            val beanDefinition = ac.getBeanDefinition(beanDefinitionName)
            if (beanDefinition.role == BeanDefinition.ROLE_APPLICATION) {
                val bean = ac.getBean(beanDefinitionName)
                println("name = $beanDefinitionName object = $bean")
            }
        }
    }

    @Test
    @DisplayName("프로토타입 빈을 의존관계 주입 받는 서로 다른 싱글톤들의 프로토타입 동일 여부 비교")
    fun 서로다른싱글톤_dependencyInjected_프로토타입_비교() {
        val singletonA = ac.getBean(SingletonScopeA::class.java)
        val singletonB = ac.getBean(SingletonScopeB::class.java)

        println("singletonA's prototypeBean : ${singletonA.prototypeBean}")
        println("singletonB's prototypeBean : ${singletonB.prototypeBean}")
        assertThat(singletonA.prototypeBean).isNotSameAs(singletonB.prototypeBean)
    }

    @Test
    @DisplayName("프로토타입 빈을 의존관계 주입 받는 같은 싱글톤들의 프로토타입 동일 여부 비교")
    fun 같은싱글톤_dependencyInjected_프로토타입_비교() {
        val singleton1 = ac.getBean(SingletonScopeA::class.java)
        val singleton2 = ac.getBean(SingletonScopeA::class.java)

        println("singleton1's prototypeBean : ${singleton1.prototypeBean}")
        println("singleton2's prototypeBean : ${singleton2.prototypeBean}")
        assertThat(singleton1).isSameAs(singleton2)
        assertThat(singleton1.prototypeBean).isSameAs(singleton2.prototypeBean)
    }

}

@Configuration
@ComponentScan(basePackages = ["com.forstudy.binscopetest.bean"])
class AppConfig {

}
