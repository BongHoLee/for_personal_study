# Presentation Layer 프로젝트 정책 · 스펙 (project_spec)

> 문서 상태: 본 문서는 Living Spec입니다. 예시는 참고용이며 구현 변경에 따라 수시로 업데이트됩니다. 컴포넌트 역할/경계와 클린 코드 원칙은 개발 과정에서 구체화/수정됩니다.

## 1) 목적과 범위
Spring Presentation Layer 컴포넌트(Controller, Interceptor, ResponseBodyAdvice, ExceptionHandler)를 활용해 V1/V2 API를 제공한다. 버전 간 차이는 UserIdentifier의 직렬화/역직렬화 방식에 한정하며, 요청과 응답은 동일한 정책을 따른다. 모든 트랜잭션(성공/실패)은 비동기적으로 이력에 기록한다.

## 2) API 버저닝 및 엔드포인트
- V1: `POST /api/v1/process`
- V2: `POST /api/v2/process`
- 규칙: V1 요청은 V1 응답, V2 요청은 V2 응답으로 대응한다.
- 공통 요구 헤더: `service-id` 

## 3) 요청/응답 공통 정책
- 공통 프로퍼티: `api_transaction_id`, `request_time`, `response_time`, `status_code`, `user_identifier(or ci)`, `data`.
- 정상/에러 동일 스키마 사용. 에러 시 다음만 값 유지: `api_transaction_id`, `status_code`, `user_identifier(or ci)`; 나머지는 `null`.
- 만약 request body 자체의 파싱 문제(클라이언트 오류)가 발생하면 status_code = 400(클라이언트 오류)로 정의하고 나머지 프로퍼티는 null로 응답

## 4) V1 스키마 (UserIdentifier = 단일 문자열 CI)
- Request (예):
```json
{
  "api_transaction_id": "tx-123",
  "request_time": "2025-11-09T10:00:00",
  "ci": "ci-12345"
}
```
- Success Response (예):
```json
{
  "api_transaction_id": "tx-123",
  "request_time": "2025-11-09T10:00:00",
  "response_time": "2025-11-09T10:00:01",
  "status_code": 200,
  "ci": "ci-12345",
  "data": "..."
}
```
- Error Response (예):
```json
{
  "api_transaction_id": "tx-123",
  "status_code": 404,
  "ci": "ci-12345",
  "request_time": null,
  "response_time": null,
  "data": null
}
```

## 5) V2 스키마 (UserIdentifier = 객체 {type, value})
- Request (예):
```json
{
  "api_transaction_id": "tx-123",
  "request_time": "2025-11-09T10:00:00",
  "user_identifier": { "type": "CI|DI|EMAIL", "value": "..." }
}
```
- Success Response (예):
```json
{
  "api_transaction_id": "tx-123",
  "request_time": "2025-11-09T10:00:00",
  "response_time": "2025-11-09T10:00:01",
  "status_code": 200,
  "user_identifier": { "type": "CI", "value": "ci-12345" },
  "data": "..."
}
```
- Error Response (예):
```json
{
  "status_code": 404,
  "user_identifier": { "type": "CI", "value": "ci-12345" },
  "api_transaction_id": null,
  "request_time": null,
  "response_time": null,
  "data": null
}
```

## 6) 컴포넌트 역할과 책임
- Interceptor(RequestContextInterceptor): 요청 헤더 `service-id`를 추출해 `RequestContext`에 저장.
- Controller(ApiController): 버전별 DTO를 받아 비즈니스 처리 후 버전별 ResponseBody 생성.
- ExceptionHandler(GlobalExceptionHandler): 경로 기준으로 V1/V2 에러 응답을 생성.
- ResponseBodyAdvice(ServiceIdResponseAdvice): 응답 헤더에 `service-id` 삽입.
- Resolver(UserIdResolver): `UserIdentifier -> UserId` 변환, 미가입자는 NotFound 예외.
- RequestContext: 요청 단위 컨텍스트(서비스 ID, 선택적 UserId, 요청 DTO 등) 저장.

## 7) 트랜잭션 히스토리 로깅
- 성공/실패와 무관하게 비동기(@Async) 로깅 수행.
- 기록 필드(예): `timestamp, serviceId, requestId(api_transaction_id), requestDateTime, responseDateTime, statusCode, userIdentifier, userId(옵션)`.

## 8) 클린 코드 지향 원칙
- Controller는 얇게 유지(검증/공통 부는 Interceptor/Advice/ExceptionHandler로 이동).
- DTO 직렬화 규칙은 버전별 DTO에서 캡슐화(V1: `ci` 문자열, V2: `user_identifier` 객체).
- 에러와 정상 응답은 동일 타입/구조를 재사용하여 분기 최소화.

## 9) 변경 관리 · 열린 결정
- 예시는 기준선 예시이며 실제 구현/테스트 상 발견되는 이슈에 맞춰 변경됩니다.
- 컴포넌트 역할과 책임, 경계 정의는 리팩토링 과정에서 수정 가능(설계 단순성/응집도 우선).
- 클린 코드 지향 원칙은 구현 패턴이 쌓이며 구체화됩니다.
- 스펙 변경 시 권장: `docs(spec): ...` 커밋 메시지와 함께 본 문서 갱신, 관련 테스트/샘플(JSON) 동기화.
- TODO(열린 항목): 최신 예시(JSON) 동기화, 에러 카테고리 표준화, 히스토리 스키마 확정.
