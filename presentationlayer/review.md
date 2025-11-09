# API 버전 분리로 인한 중복과 개선 방향 (Review)

## 문제 요약
- V1/V2는 비즈니스 로직이 동일하고, UserIdentifier의 직렬화/역직렬화(표현)만 다릅니다.
- 그럼에도 Controller(V1/V2), ExceptionHandler(V1/V2) 등 유사한 코드가 중복됩니다.
- 변경이 생기면 양쪽을 모두 수정해야 하므로 변경 비용과 리스크가 큽니다.

## 왜 중복이 생기는가
- 타입 차이: V1은 `ci: String`, V2는 `user_identifier: {type, value}`로 “와이어 스키마”가 다릅니다. 요청/응답 DTO의 타입 시그니처가 달라 Spring MVC 메서드 시그니처도 달라집니다.
- 메시지 컨버터의 전역성: Spring의 Kotlinx/Jackson 컨버터는 보통 전역 설정(단일 `Json`/ObjectMapper)로 동작합니다. 요청마다 다른 직렬화 규칙을 적용하기 어렵습니다.
- 핸들러 매핑 제약: Spring MVC는 컴파일타임 시그니처 기반으로 `@RequestBody` 바인딩을 결정합니다. “런타임에 V1/V2를 스위치”하는 유연성이 제한적입니다.
- 예외 응답까지 버전 의존: 에러 응답도 필드명이 달라(예: `ci` vs `user_identifier`) 핸들러 로직이 분기/중복됩니다.

## 런타임 유연성의 한계
- Kotlinx Serialization은 코드 생성 기반으로, 직렬화 모양을 런타임 요청 컨텍스트에 따라 바꾸려면 커스텀 직렬화기와 추가 인프라가 필요합니다.
- Spring의 메시지 컨버터는 요청 단위 컨텍스트를 기본적으로 모르며, 전역 설정에서만 분기하기 쉽습니다.

## 개선 옵션 (선택지)
- A) 컨텍스트 인지 직렬화기(권장 POC)
  - `@Contextual` + `KSerializer<UserIdentifier>`를 구현해, 현재 요청의 버전(V1/V2)을 `RequestContextHolder`/ThreadLocal에서 읽어 V1이면 문자열, V2면 객체로 직렬화/역직렬화.
  - 장점: 단일 DTO로 V1/V2 모두 처리 → Controller/ExceptionHandler 통합 가능.
  - 고려: 직렬화기에서 요청 컨텍스트 접근(쓰레드로컬)과 테스트 용이성, 성능/캐싱 검토.

- B) 프레젠터/어댑터 계층 도입
  - 컨트롤러는 버전 중립 `DomainCommand`/`DomainResult`만 다루고, `ResponseFactory`(V1/V2 전략)가 와이어 스키마를 생성.
  - 장점: 컨트롤러/예외처리 공통화, 버전별 차이는 팩토리로 국소화.
  - 고려: DTO 매퍼/팩토리 작성 필요, 여전히 요청 DTO는 버전별일 수 있음.

- C) 템플릿 메서드 패턴으로 중복 축소
  - `abstract BaseApiController`에 공통 플로우(컨텍스트 저장, resolve, 로깅)를 구현하고, V1/V2 클래스는 최소 오버라이드(입출력 매핑만).
  - ExceptionHandler도 `ErrorResponseFactory`(버전 전략)로 단일화 가능.

- D) 미디어 타입 버전닝 전환
  - `Accept: application/vnd.service.v1+json`/`v2` 식으로 버전 구분 후, 미디어 타입별 컨버터 또는 직렬화 모듈을 바인딩.
  - 장점: 경로 중복 감소, 컨버터 레벨에서 버전 분기 용이.
  - 고려: 클라이언트와의 합의/마이그레이션 비용.

## 제안하는 단계적 접근
- Step 1: 템플릿/전략으로 중복 최소화 (적은 리스크, 빠른 효과).
- Step 2: `ErrorResponseFactory`/`ResponseFactory`로 예외·응답 생성 단일화.
- Step 3: 컨텍스트 인지 `KSerializer<UserIdentifier>` POC → 단일 DTO 전환 검토.
- Step 4: 장기적으로 미디어 타입 버전닝 고려(필요시).

## 결론
- 현재 중복은 “와이어 스키마 차이”와 “프레임워크의 정적 바인딩/전역 컨버터” 때문에 발생합니다.
- 템플릿/전략으로 우선 중복을 줄이고, 가능하면 컨텍스트 인지 직렬화로 단일 DTO를 지향하면 유지보수성이 크게 향상됩니다.
