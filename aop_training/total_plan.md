# 스프링 AOP 메커니즘 학습 전체 계획

## 📚 학습 목표
스프링 AOP의 내부 동작 원리를 코드 레벨에서 이해하고, 빈 후처리기와 프록시 생성 메커니즘을 체계적으로 학습한다.

## 🎯 최종 목표
- 스프링이 AOP를 처리하는 전체적인 메커니즘 이해
- 빈 후처리기(BeanPostProcessor)의 역할과 동작 원리
- 프록시 객체 생성 과정과 내부 구현
- JDK 동적 프록시와 CGLIB 프록시의 차이점과 사용 시점

## 📋 학습 단계별 계획

### Phase 1: 프록시 기초 학습 (Week 1)
**목표**: Java/Kotlin에서 프록시 패턴과 동적 프록시 이해
- **파일**: `plan_proxy_basics.md`
- **내용**:
  - JDK 동적 프록시 구현 및 테스트
  - CGLIB 프록시 구현 및 테스트
  - 두 방식의 차이점 비교 분석

### Phase 2: 스프링 AOP 핵심 개념 (Week 2)
**목표**: 스프링 AOP의 기본 개념과 구성 요소 이해
- **파일**: `plan_spring_aop_concepts.md`
- **내용**:
  - Aspect, Advice, Pointcut, JoinPoint 개념
  - @Aspect 어노테이션 기반 AOP 구현
  - 다양한 Advice 타입 실습

### Phase 3: 빈 후처리기와 프록시 생성 (Week 3)
**목표**: 스프링의 빈 생명주기와 프록시 생성 과정 이해
- **파일**: `plan_bean_postprocessor.md`
- **내용**:
  - BeanPostProcessor 인터페이스 구현
  - DefaultAdvisorAutoProxyCreator 분석
  - 프록시 생성 시점과 과정 추적

### Phase 4: ProxyFactory와 고급 설정 (Week 4)
**목표**: 스프링의 ProxyFactory를 통한 고급 프록시 설정
- **파일**: `plan_proxy_factory.md`
- **내용**:
  - ProxyFactory 직접 사용
  - ProxyFactoryBean 설정
  - 프록시 설정 최적화

### Phase 5: 실제 스프링 소스 코드 분석 (Week 5)
**목표**: 스프링 소스 코드를 통한 AOP 메커니즘 완전 이해
- **파일**: `plan_spring_source_analysis.md`
- **내용**:
  - AbstractAutoProxyCreator 소스 분석
  - AspectJAutoProxyCreator 동작 원리
  - 실제 스프링 애플리케이션에서의 프록시 생성 과정

## 🛠 프로젝트 구조 계획

```
src/
├── main/kotlin/bong/training/
│   ├── AopTrainingApplication.kt (기존)
│   ├── phase1/ (프록시 기초)
│   │   ├── jdk/
│   │   └── cglib/
│   ├── phase2/ (스프링 AOP 기본)
│   │   ├── aspects/
│   │   └── services/
│   ├── phase3/ (빈 후처리기)
│   │   ├── postprocessors/
│   │   └── config/
│   ├── phase4/ (ProxyFactory)
│   │   ├── factory/
│   │   └── advanced/
│   └── phase5/ (소스 분석)
│       └── analysis/
└── test/kotlin/bong/training/
    ├── phase1/
    ├── phase2/
    ├── phase3/
    ├── phase4/
    └── phase5/
```

## 📦 필요한 의존성 추가 계획

```kotlin
dependencies {
    // 기존 의존성 유지
    
    // AOP 관련
    implementation("org.springframework.boot:spring-boot-starter-aop")
    
    // CGLIB (스프링 부트에 포함되지만 명시적 추가)
    implementation("cglib:cglib:3.3.0")
    
    // AspectJ (고급 AOP 기능)
    implementation("org.aspectj:aspectjweaver")
    
    // 테스트 관련
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
}
```

## 📈 학습 진행 방식

1. **이론 → 실습 → 분석** 순서로 진행
2. 각 단계마다 **테스트 코드** 작성으로 이해도 검증
3. **디버깅**을 통한 실제 동작 과정 추적
4. **로그 분석**을 통한 프록시 생성 과정 관찰

## 🎉 예상 성과

- 스프링 AOP의 완전한 이해
- 프록시 패턴의 실제 활용 능력
- 스프링 프레임워크의 내부 동작 원리 습득
- 성능 최적화를 위한 AOP 설정 능력

---
*이 계획은 학습 진행에 따라 조정될 수 있습니다.*