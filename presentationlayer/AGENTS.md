# Repository Guidelines

## 프로젝트 구조 & 모듈 구성
- `src/main/kotlin/bong/presentationlayer`: 애플리케이션 코드
  - `controller`, `service`, `dto`, `advice`, `interceptor`, `config`, `domain`, `exception`, `logging`.
- `src/test/kotlin/bong/presentationlayer`: 테스트 (JUnit 5, Kotest, MockMvc, MockK)
  - 예: `controller/ApiControllerTest.kt` ↔ `controller/ApiController.kt` 대응
- `src/main/resources/application.yml`: Spring Boot 설정
- 빌드: Gradle(Kotlin DSL), Java 21 툴체인
- 문서: `feature_spec.md`, `DTO_SERIALIZATION_GUIDE.md`

## 빌드·테스트·개발 명령
- `./gradlew clean build`: 컴파일 + 모든 테스트 실행
- `./gradlew test`: JUnit/Kotest 테스트 실행
- `./gradlew bootRun`: 로컬 실행 (`:8080`)
- `./gradlew test --tests "bong.presentationlayer.controller.ApiControllerTest"`: 특정 테스트만 실행
- 예시 요청(필수 헤더 `service-id`):
  ```bash
  curl -X POST localhost:8080/api/v1/process \
    -H 'Content-Type: application/json' -H 'service-id: local-dev' \
    -d '{"requestId":"tx-1","requestDateTime":"2025-01-01T00:00:00","userIdentifier":{"type":"CI","value":"ci-123"}}'
  ```

## 코딩 스타일 & 네이밍 규칙
- Kotlin 공식 스타일, 4칸 들여쓰기, 패키지 소문자(`bong.presentationlayer.*`).
- 클래스/인터페이스: PascalCase, 함수/변수: camelCase, 상수: UPPER_SNAKE_CASE.
- 식별자는 value class(`UserId`) 선호, 구분자는 sealed 타입(`UserIdentifier`).
- DTO는 `kotlinx.serialization` 사용. JSON 형식 변경 시 `DTO_SERIALIZATION_GUIDE.md` 준수 및 호환성 유지.
- 횡단 관심사는 `advice`/`interceptor`에 배치하여 컨트롤러 비대화 방지.

## 테스트 가이드
- Kotest(FunSpec) 또는 JUnit 5 사용, 파일명은 `*Test.kt`, 패키지 구조 미러링.
- 정상/에러(예: 미회원→404) 모두 검증.
- 컨트롤러 테스트는 MockMvc, 단위 수준 모킹은 MockK 권장.
- 푸시 전 `./gradlew test` 통과 필수.

## 커밋 & PR 가이드
- 커밋 메시지: Conventional Commits 권장 `type(scope): summary`
  - 예: `refactor(presentationlayer): RequestContext 인터셉터 분리`
- PR은 다음을 포함:
  - 변경 설명과 연관 이슈 링크
  - API/DTO 변경 시 샘플 요청/응답
  - 갱신된 문서/테스트, CI 그린(`./gradlew test`)
  - 변경 범위는 작고 명확하게 유지

## 보안 & 설정 팁
- 비밀값은 커밋 금지. 환경변수와 `application.yml` 오버라이드 사용.
- 대부분의 엔드포인트는 `service-id` 헤더를 기대하며, `RequestContext`와 `ResponseBodyAdvice`를 통해 전파됨.
