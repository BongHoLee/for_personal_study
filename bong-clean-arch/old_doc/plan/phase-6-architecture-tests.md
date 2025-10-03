# Phase 6: 아키텍처 테스트 및 검증

## 목표
- ArchUnit을 이용한 아키텍처 규칙 자동화
- 헥사고날 아키텍처 제약사항 검증
- 의존성 방향 규칙 강제
- 패키지 구조 규칙 검증

## 이론 학습 포인트

### ArchUnit의 가치
1. **아키텍처 규칙의 자동화**
   - 코드 리뷰에서 놓칠 수 있는 아키텍처 위반 감지
   - 시간이 지나면서 발생하는 아키텍처 부식 방지

2. **Living Documentation**
   - 코드로 표현되는 아키텍처 문서
   - 규칙이 변경되면 테스트도 함께 업데이트

3. **팀 내 아키텍처 일관성**
   - 모든 개발자가 동일한 규칙 준수
   - 새로운 팀원의 빠른 적응 지원

### 헥사고날 아키텍처 규칙
1. **계층별 의존성 방향**
2. **패키지별 접근 제한**
3. **어노테이션 사용 규칙**

## 실습 과제

### 1. ArchUnit 의존성 추가

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("com.tngtech.archunit:archunit-junit5:1.0.1")
    testImplementation("com.tngtech.archunit:archunit:1.0.1")
}
```

### 2. 기본 아키텍처 테스트

```kotlin
@AnalyzeClasses(packages = ["com.bong.buckpal"])
class DependencyRuleTests {

    @ArchTest
    val domainLayerDoesNotDependOnAnyOtherLayer = 
        classes()
            .that().resideInAPackage("..domain..")
            .should().onlyDependOnClassesInPackages(
                "..domain..",
                "java..",
                "kotlin..",
                "org.jetbrains.annotations.."
            )

    @ArchTest
    val applicationLayerOnlyDependsOnDomainLayer = 
        classes()
            .that().resideInAPackage("..application..")
            .should().onlyDependOnClassesInPackages(
                "..application..",
                "..domain..",
                "java..",
                "kotlin..",
                "org.springframework..",
                "org.jetbrains.annotations.."
            )

    @ArchTest
    val adapterLayerDoesNotAccessDomainLayerDirectly = 
        noClasses()
            .that().resideInAPackage("..adapter..")
            .should().dependOnClassesThat().resideInAPackage("..domain..")

    @ArchTest
    val webAdapterOnlyDependsOnApplicationLayer = 
        classes()
            .that().resideInAPackage("..adapter.web..")
            .should().onlyDependOnClassesInPackages(
                "..adapter.web..",
                "..application.provided..",
                "..domain..",  // DTO 변환을 위해 허용
                "java..",
                "kotlin..",
                "org.springframework..",
                "jakarta.validation..",
                "com.fasterxml.jackson..",
                "org.jetbrains.annotations.."
            )

    @ArchTest
    val persistenceAdapterOnlyDependsOnApplicationLayer = 
        classes()
            .that().resideInAPackage("..adapter.persistence..")
            .should().onlyDependOnClassesInPackages(
                "..adapter.persistence..",
                "..application.required..",
                "..domain..",  // 도메인 객체 매핑을 위해 허용
                "java..",
                "kotlin..",
                "org.springframework..",
                "jakarta.persistence..",
                "org.jetbrains.annotations.."
            )
}
```

### 3. 포트와 어댑터 규칙 테스트

```kotlin
@AnalyzeClasses(packages = ["com.bong.buckpal"])
class PortAndAdapterRuleTests {

    @ArchTest
    val providedPortsShouldBeInterfaces = 
        classes()
            .that().resideInAPackage("..application.provided..")
            .should().beInterfaces()

    @ArchTest
    val requiredPortsShouldBeInterfaces = 
        classes()
            .that().resideInAPackage("..application.required..")
            .should().beInterfaces()

    @ArchTest
    val useCasesShouldImplementProvidedPorts = 
        classes()
            .that().resideInAPackage("..application..")
            .and().haveSimpleNameEndingWith("Service")
            .should().implement(
                JavaClass.Predicates.resideInAPackage("..application.provided..")
            )

    @ArchTest
    val adaptersShouldImplementRequiredPorts = 
        classes()
            .that().resideInAPackage("..adapter..")
            .and().haveSimpleNameEndingWith("Adapter")
            .should().implement(
                JavaClass.Predicates.resideInAPackage("..application.required..")
            )

    @ArchTest
    val webAdaptersShouldBeAnnotatedWithRestController = 
        classes()
            .that().resideInAPackage("..adapter.web..")
            .and().haveSimpleNameEndingWith("Controller")
            .should().beAnnotatedWith(RestController::class.java)

    @ArchTest
    val persistenceAdaptersShouldBeAnnotatedWithComponent = 
        classes()
            .that().resideInAPackage("..adapter.persistence..")
            .and().haveSimpleNameEndingWith("Adapter")
            .should().beAnnotatedWith(Component::class.java)
}
```

### 4. 네이밍 컨벤션 테스트

```kotlin
@AnalyzeClasses(packages = ["com.bong.buckpal"])
class NamingConventionTests {

    @ArchTest
    val useCasesShouldHaveProperNaming = 
        classes()
            .that().resideInAPackage("..application..")
            .and().areNotInterfaces()
            .should().haveSimpleNameEndingWith("Service")
            .orShould().haveSimpleNameEndingWith("UseCase")

    @ArchTest
    val webAdaptersShouldHaveProperNaming = 
        classes()
            .that().resideInAPackage("..adapter.web..")
            .and().areAnnotatedWith(RestController::class.java)
            .should().haveSimpleNameEndingWith("Controller")

    @ArchTest
    val persistenceAdaptersShouldHaveProperNaming = 
        classes()
            .that().resideInAPackage("..adapter.persistence..")
            .and().areNotInterfaces()
            .should().haveSimpleNameEndingWith("Adapter")
            .orShould().haveSimpleNameEndingWith("Repository")

    @ArchTest
    val domainEntitiesShouldNotHaveSuffix = 
        classes()
            .that().resideInAPackage("..domain..")
            .and().areNotInterfaces()
            .should().notHaveSimpleNameEndingWith("Entity")
            .andShould().notHaveSimpleNameEndingWith("Model")
            .andShould().notHaveSimpleNameEndingWith("DTO")

    @ArchTest
    val valueObjectsShouldNotBeMutable = 
        classes()
            .that().resideInAPackage("..domain..")
            .and().haveSimpleNameEndingWith("Id")
            .or().haveSimpleName("Money")
            .should().haveOnlyFinalFields()
}
```

### 5. 도메인 순수성 테스트

```kotlin
@AnalyzeClasses(packages = ["com.bong.buckpal"])
class DomainPurityTests {

    @ArchTest
    val domainShouldNotDependOnSpring = 
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework..",
                "org.springframework.boot.."
            )

    @ArchTest
    val domainShouldNotDependOnJPA = 
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "jakarta.persistence..",
                "org.hibernate..",
                "org.springframework.data.."
            )

    @ArchTest
    val domainShouldNotDependOnWeb = 
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework.web..",
                "jakarta.servlet..",
                "org.springframework.http.."
            )

    @ArchTest
    val domainShouldNotDependOnJson = 
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.fasterxml.jackson..",
                "com.google.gson.."
            )

    @ArchTest
    val domainShouldNotUseSystemOut = 
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().callMethod(System::class.java, "out")

    @ArchTest
    val domainShouldNotUseLogging = 
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.slf4j..",
                "java.util.logging..",
                "org.apache.logging.."
            )
}
```

### 6. 순환 의존성 테스트

```kotlin
@AnalyzeClasses(packages = ["com.bong.buckpal"])
class CyclicDependencyTests {

    @ArchTest
    val noClassCycles = slices()
        .matching("com.bong.buckpal.(*)..")
        .should().beFreeOfCycles()

    @ArchTest
    val noPackageCycles = slices()
        .matching("com.bong.buckpal.account.(*)..")
        .should().beFreeOfCycles()
}
```

### 7. 보안 관련 테스트

```kotlin
@AnalyzeClasses(packages = ["com.bong.buckpal"])
class SecurityRuleTests {

    @ArchTest
    val noClassesShouldLogPasswords = 
        noClasses()
            .should().callMethod(Logger::class.java, "info", String::class.java)
            .andShould().accessField(String::class.java, "password")

    @ArchTest
    val noClassesShouldUseReflection = 
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "java.lang.reflect..",
                "kotlin.reflect.."
            )

    @ArchTest
    val webControllersShouldValidateInput = 
        classes()
            .that().resideInAPackage("..adapter.web..")
            .and().areAnnotatedWith(RestController::class.java)
            .should().beAnnotatedWith(Validated::class.java)
}
```

## 챌린지 과제

### 1. 커스텀 아키텍처 규칙 작성
- 프로젝트 특화된 규칙들을 어떻게 정의할 것인가?
- 비즈니스 규칙을 아키텍처 테스트로 표현할 수 있는가?

### 2. 테스트 성능 최적화
- 대규모 코드베이스에서 ArchUnit 테스트 성능은?
- 불필요한 테스트 실행을 어떻게 피할 것인가?

### 3. 예외 사항 관리
- 어떤 경우에 아키텍처 규칙의 예외를 허용할 것인가?
- 기술적 제약으로 인한 규칙 위반을 어떻게 처리할 것인가?

### 4. 진화하는 아키텍처
- 아키텍처가 변경될 때 테스트는 어떻게 관리할 것인가?
- 레거시 코드의 점진적 개선을 어떻게 지원할 것인가?

## 실습 단계

1. **기본 의존성 규칙 테스트 작성**
2. **포트와 어댑터 규칙 테스트 작성**
3. **네이밍 컨벤션 테스트 작성**
4. **도메인 순수성 테스트 작성**
5. **순환 의존성 테스트 작성**
6. **보안 관련 테스트 작성**
7. **커스텀 규칙 테스트 작성**

## 테스트 시나리오

### 아키텍처 규칙 검증
- [ ] 계층별 의존성 방향 준수
- [ ] 패키지별 접근 제한 준수
- [ ] 포트와 어댑터 규칙 준수
- [ ] 네이밍 컨벤션 준수

### 도메인 순수성 검증
- [ ] 외부 프레임워크 의존성 없음
- [ ] 기술적 관심사 분리
- [ ] 비즈니스 로직 순수성

### 보안 및 품질 검증
- [ ] 보안 취약점 패턴 없음
- [ ] 코딩 표준 준수
- [ ] 순환 의존성 없음

## 완료 체크리스트
- [ ] 모든 아키텍처 테스트 작성 완료
- [ ] CI/CD 파이프라인에 아키텍처 테스트 통합
- [ ] 팀 내 아키텍처 규칙 문서화
- [ ] 아키텍처 위반 시 대응 가이드 작성
- [ ] 아키텍처 테스트 유지보수 프로세스 구축

## 다음 Phase 미리보기
Phase 7에서는 통합 테스트와 엔드투엔드 테스트를 구현하여 전체 시스템의 동작을 검증할 예정입니다.