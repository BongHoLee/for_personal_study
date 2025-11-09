# Feature Specification: Presentation Layer Response 구조 개선

## 현재 문제점

**잘못된 현재 구조:**
```json
{
  "serviceId": "test-service-001",    // ❌ body에 있으면 안됨
  "body": {                           // ❌ wrapper 불필요
    "transaction_id": "tx-123",
    "status_code": 200,
    "ci": "ci-value",
    ...
  }
}
```

**올바른 구조:**
- HTTP Header: `service-id: test-service-001`
- HTTP Body: ResponseBody 그 자체 (wrapper 없이)

```json
{
  "transaction_id": "tx-123",
  "status_code": 200,
  "ci": "ci-value",
  "api_transaction_id": "tx-123",
  "request_time": "2025-11-09T10:00:00",
  "response_time": "2025-11-09T10:00:01",
  "data": "success"
}
```

## 요구사항

### 1. Response 구조
- **HTTP Body**: `V1ResponseBody` 또는 `V2ResponseBody` 직접 반환 (wrapper 없이)
- **HTTP Header**: `service-id` 헤더로 전달
- RequestBody와 ResponseBody는 동일한 형태 (flatten)

### 2. service-id 전파 메커니즘
- Request Header `service-id` → Response Header `service-id`로 전파
- Request Scope Bean 또는 ThreadLocal 활용하여 컨텍스트 관리
- `ResponseBodyAdvice`를 통해 Response Header에 자동 추가

### 3. 정상 응답 vs 에러 응답
**공통:**
- 동일한 ResponseBody 타입 사용 (V1 → V1ResponseBody, V2 → V2ResponseBody)
- service-id는 항상 response header에 포함

**정상 응답:**
- 모든 필드 값 포함
- status_code: 200
- data: 비즈니스 데이터

**에러 응답 (UserNotFoundException):**
- 필수 필드만 값 포함: `transaction_id`, `status_code`, `userIdentifier`
- Nullable 필드는 null: `api_transaction_id`, `request_time`, `response_time`, `data`
- status_code: 404

### 4. 구현 전략

**Option A: Request Scope Bean + ResponseBodyAdvice (권장)**
```kotlin
@Component
@RequestScope
class RequestContext {
    var serviceId: String? = null
}

@ControllerAdvice
class ServiceIdResponseAdvice : ResponseBodyAdvice<Any> {
    // Response Header에 service-id 추가
}
```

**Option B: HandlerInterceptor + ThreadLocal**
- ThreadLocal에 service-id 저장
- Interceptor에서 response header 추가
- 요청 종료 시 ThreadLocal 정리 필수

**선택 기준:**
- Spring 컨텍스트 관리 신뢰성: Option A 우선
- 단순성과 명확성: Option A가 더 적합
- ThreadLocal 관리 부담 최소화: Option A

### 5. 변경 필요 파일

**제거:**
- `ServiceResponse` wrapper (더 이상 사용 안 함)
- `ServiceRequest` wrapper (더 이상 사용 안 함)

**수정:**
- `ApiController`: `ResponseEntity<String>` → `V1ResponseBody` / `V2ResponseBody` 직접 반환
- `GlobalExceptionHandler`: `ResponseEntity<String>` → ResponseBody 직접 반환

**추가:**
- `RequestContext` (Request Scope Bean): service-id 저장
- `RequestContextInterceptor` (옵션): service-id를 RequestContext에 저장
- `ServiceIdResponseAdvice` (ResponseBodyAdvice): Response Header에 service-id 추가

### 6. JSON 직렬화

**현재 문제:**
- Spring MVC의 기본 Jackson이 kotlinx.serialization `@Serializable`을 처리 못함
- 수동으로 `Json.encodeToString()` 사용 중

**해결 방법:**
- **Option A**: Spring에 kotlinx.serialization HttpMessageConverter 추가 (권장)
- **Option B**: 계속 수동 직렬화 (현재 방식)

**선택:** Option A 적용 (더 Spring-idiomatic)

### 7. 테스트 검증 사항

**정상 응답:**
- Response Body: V1ResponseBody/V2ResponseBody flatten 구조
- Response Header: `service-id` 포함
- HTTP Status: 200

**에러 응답:**
- Response Body: 동일한 ResponseBody 타입, 필수 필드만 값 존재
- Response Header: `service-id` 포함
- HTTP Status: 404

## 구현 순서

1. ✅ **RequestContext** (Request Scope Bean) 생성
2. ✅ **HandlerInterceptor** 또는 **Argument Resolver**로 service-id를 RequestContext에 저장
3. ✅ **ServiceIdResponseAdvice** (ResponseBodyAdvice) 구현
   - Response Header에 service-id 추가
4. ✅ **ApiController** 수정
   - ServiceResponse wrapper 제거
   - ResponseBody 직접 반환
   - RequestContext에서 service-id 조회하여 저장
5. ✅ **GlobalExceptionHandler** 수정
   - ServiceResponse wrapper 제거
   - ResponseBody 직접 반환
6. ✅ **kotlinx.serialization HttpMessageConverter** 추가 (선택)
7. ✅ **ServiceRequest/ServiceResponse** 클래스 삭제
8. ✅ **테스트** 수정 및 검증

## 예상 JSON 응답 구조

**V1 정상 응답:**
```
HTTP/1.1 200 OK
service-id: test-service-001

{
  "transaction_id": "tx-123",
  "status_code": 200,
  "ci": "ci-value-12345",
  "api_transaction_id": "tx-123",
  "request_time": "2025-11-09T10:00:00",
  "response_time": "2025-11-09T10:00:01",
  "data": "Processing completed"
}
```

**V1 에러 응답:**
```
HTTP/1.1 404 Not Found
service-id: test-service-002

{
  "transaction_id": "tx-456",
  "status_code": 404,
  "ci": "ci-unknown",
  "api_transaction_id": null,
  "request_time": null,
  "response_time": null,
  "data": null
}
```

**V2 에러 응답:**
```
HTTP/1.1 404 Not Found
service-id: test-service-003

{
  "transaction_id": "tx-789",
  "status_code": 404,
  "user_identifier": {
    "type": "EMAIL",
    "value": "unknown@example.com"
  },
  "api_transaction_id": null,
  "request_time": null,
  "response_time": null,
  "data": null
}
```
