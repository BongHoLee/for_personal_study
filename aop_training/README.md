# 🎯 Spring AOP & 프록시 학습 프로젝트

> **목적**: Spring AOP와 프록시 패턴을 코드 레벨에서 직접 구현하며 깊이 있게 학습하는 실습 프로젝트

## 📋 프로젝트 개요

이 프로젝트는 Spring의 핵심 기능 중 하나인 **AOP(Aspect-Oriented Programming)** 를 이론과 실습을 통해 체계적으로 학습하기 위한 교육용 프로젝트입니다.

단순히 AOP 어노테이션을 사용하는 것을 넘어서, **프록시 패턴의 기본 원리부터 Spring AOP의 내부 동작 메커니즘**까지 단계별로 학습합니다.

## 🎯 학습 목표

### 핵심 학습 목표
- **프록시 패턴**의 기본 개념과 동작 원리 이해
- **JDK 동적 프록시**와 **CGLIB 프록시**의 차이점과 장단점 분석
- **Spring AOP**의 내부 동작 원리와 프록시 생성 과정 이해
- 실제 프로덕션 코드에서 AOP를 효과적으로 활용하는 방법 습득

### 세부 학습 내용
- 정적 프록시 vs 동적 프록시 구현 및 비교
- InvocationHandler와 MethodInterceptor 활용
- Aspect, Advice, Pointcut 개념과 실제 구현
- 횡단 관심사(Cross-cutting Concerns) 분리 기법
- AOP 성능 최적화 및 디버깅 기법

## 📚 학습 단계

### Phase 1: 프록시 기초 학습 🔰
- **정적 프록시** 직접 구현
- **JDK 동적 프록시** 구현 및 활용
- **CGLIB 프록시** 구현 및 활용
- 프록시 방식 비교 분석

### Phase 2: Spring AOP 기본 📖
- Spring AOP 어노테이션 활용
- Pointcut 표현식 작성
- Around, Before, After Advice 구현
- AOP 프록시 생성 과정 분석

### Phase 3: 고급 AOP 활용 🚀
- 복합적인 Aspect 설계
- AOP와 트랜잭션 연동
- 커스텀 어노테이션 기반 AOP
- 성능 모니터링 및 최적화

## 🛠 기술 스택

- **Language**: Kotlin
- **Framework**: Spring Boot
- **Build Tool**: Gradle
- **Testing**: JUnit 5, AssertJ
- **Libraries**: 
  - CGLIB (프록시 생성)
  - Spring AOP
  - SLF4J (로깅)

## 📁 프로젝트 구조

```
src/
├── main/kotlin/bong/training/
│   ├── static_proxy/          # 정적 프록시 구현
│   ├── phase1/                # Phase 1: 프록시 기초
│   │   ├── jdk/              # JDK 동적 프록시
│   │   ├── cglib/            # CGLIB 프록시
│   │   └── comparison/       # 프록시 방식 비교
│   ├── phase2/                # Phase 2: Spring AOP 기본
│   └── phase3/                # Phase 3: 고급 AOP
├── test/kotlin/bong/training/ # 테스트 코드
└── resources/                 # 설정 파일
```