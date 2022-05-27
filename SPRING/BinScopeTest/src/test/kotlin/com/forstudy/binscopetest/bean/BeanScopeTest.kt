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
        assertThat(singleton1.logic()).isEqualTo(singleton2.logic())
    }

    @Test
    @DisplayName("프록시 프로토타입 빈 주입된 싱글톤 요청 때 마다 프로토타입 변경 여부 확인")
    fun 같은싱글톤_dependencyInjected_프록시프로토타입_비교() {
        val singleton1 = ac.getBean(SingletonHashProxyPrototypeA::class.java)
        val singleton2 = ac.getBean(SingletonHashProxyPrototypeA::class.java)

        // logic 요청 시 마다 싱글톤 빈이 생성되어 교체되는걸 확인할 수 있다!
        println("singleton1's prototypeBean : ${singleton1.prototypeBean}")
        println("singleton2's prototypeBean : ${singleton2.prototypeBean}")


        assertThat(singleton1).isSameAs(singleton2)
        val prototypeBean1 = singleton1.prototypeBean
        singleton1.logic()
        val prototypeBean2 = singleton1.prototypeBean

        // 근데 왜 객체 prototypeBean1, prototypeBean2를 객체 비교하면 같다고 나올까? 분명 다른 객체인데?!
        // 실제 prototypeBean1 자체는 "프록시 객체"이기 때문에 객체 비교를 하면 당연히 "같은 프록시 객체"이므로 같다고 나오는것 같다.
        // 하지만 엄밀히 따지면(UUID로 확인 가능) 프로토타입에 메시지를 보낼 때 마다 프로토타입 프록시가 새로운 프로토타입을 생성한다!!
        // assertThat(prototypeBean1).isNotSameAs(prototypeBean2)

        assertThat(singleton1.logic()).isNotEqualTo(singleton1.logic())
    }

}

@Configuration
@ComponentScan(basePackages = ["com.forstudy.binscopetest.bean"])
class AppConfig {

}
