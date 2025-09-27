# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소에서 코드 작업을 할 때 참고할 수 있는 가이드를 제공합니다.

## 프로젝트 개요

이 프로젝트의 주된 목적은 '개발자의 헥사고날 아키텍처 실습과 학습'을 돕는 것입니다.

"BuckPal"이라는 송금 서비스를 위한 헥사고날 아키텍처(포트와 어댑터)를 시연하는 Spring Boot 애플리케이션입니다.
이 프로젝트는 "Get Your Hands Dirty on Clean Architecture" 책을 기반으로 한 동반 구현 예제입니다.
원본 구현의 링크는 https://github.com/wikibook/clean-architecture 입니다.

## 주요 명령어

### 빌드 및 테스트
- **빌드**: `./gradlew build` - 애플리케이션을 컴파일, 테스트하고 패키징합니다
- **테스트**: `./gradlew test` - ArchUnit 테스트를 포함한 모든 단위 및 통합 테스트를 실행합니다
- **실행**: `./gradlew bootRun` - Spring Boot 애플리케이션을 시작합니다

### 개발
- **정리**: `./gradlew clean` - 빌드 아티팩트를 제거합니다
- **검사**: `./gradlew check` - 모든 검증 작업(테스트, 코드 품질)을 실행합니다

## 아키텍처

- 프로젝트는 명확한 관심사 분리를 통한 헥사고날 아키텍처 원칙을 따릅니다:
- 주요하게 '도메인' 또는 '바운디드 컨텍스트' 우선의 패키지 구조를 채택합니다.
- 바운디드 컨텍스트 패키지에는 domain, application, adapter 패키지가 포함됩니다. 
- provided port interface는 application/provided에, required port interface는 application/required에 위치합니다.
- 전반적으로 in/out과 같은 방향성 용어 대신 'provided/required'를 사용하여 포트의 역할을 명확히 합니다.

### 아키텍처 규칙

프로젝트는 `DependencyRuleTests.java`에서 ArchUnit 테스트를 사용하여 아키텍처 경계를 강제합니다. 주요 규칙:
- 도메인 계층은 다른 계층에 대한 의존성이 없음
- 애플리케이션 계층은 도메인에만 의존
- 어댑터는 포트를 통해 애플리케이션과 도메인에 의존

## 기술 스택

- **Java 21** (필수)
- **Kotlin 1.9** - 주 프로그래밍 언어
- **Spring Boot 3.x** - 웹 프레임워크 및 의존성 주입
- **Spring Data JPA** - 영속성 계층
- **H2 Database** - 개발/테스트용 인메모리 데이터베이스
- **kotest** - 테스트 프레임워크
- **MockK** - 모킹 프레임워크
- **ArchUnit** - 아키텍처 테스트

## 개발 참고사항

- 애플리케이션은 기본적으로 H2 데이터베이스(인메모리)를 사용합니다
- 설정은 `application.yml`에 있으며 송금 임계값 설정이 포함되어 있습니다
- 프로젝트에는 포괄적인 단위 테스트와 아키텍처 테스트가 포함되어 있습니다

## 테스트 전략

- **단위 테스트**: 개별 컴포넌트를 격리하여 테스트
- **통합 테스트**: 완전한 유스케이스 플로우 테스트 
- **아키텍처 테스트**: ArchUnit을 사용하여 헥사고날 아키텍처 제약사항 검증
- **테스트 데이터 빌더**: 테스트 설정을 위해 `common/` 패키지에 위치