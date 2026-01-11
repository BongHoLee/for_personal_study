# Presentation Layer 리팩토링 제안서 (refactor_suggest_cc)

> 작성일: 2025-11-09
> 목적: 버전별 중복 코드 제거 및 클린 코드 지향 아키텍처 개선

---

## 1. 현재 문제점 분석

### 1.1 중복 코드 현황

현재 프로젝트는 V1/V2 API 버전을 지원하며, 버전 간 차이는 `UserIdentifier` 직렬화 방식에만 국한되어 있음에도 다음 컴포넌트들이 거의 동일한 로직으로 중복 구현되어 있습니다.

#### 중복 컴포넌트 목록

| 컴포넌트 | V1 | V2 | 코드 유사도 | 차이점 |
|---------|----|----|-----------|--------|
| **Controller** | `ApiV1Controller` | `ApiV2Controller` | 100% | 타입만 상이 (V1RequestBody vs V2RequestBody) |
| **ExceptionHandler** | `GlobalExceptionHandlerV1` | `GlobalExceptionHandlerV2` | 100% | 타입만 상이 (V1ResponseBody vs V2ResponseBody) |
| **ErrorFactory** | `V1ApiErrorResponseFactory` | `V2ApiErrorResponseFactory` | 100% | 타입만 상이 |

#### 중복 코드 예시

**ApiV1Controller.kt** (25-40행)
```kotlin
fun processV1(@RequestBody requestBody: V1RequestBody): V1ResponseBody {
    val serviceId = requestContext.serviceId ?: "unknown"
    logger.info("Processing V1 request - serviceId: $serviceId, requestId: ${requestBody.requestId}")

    requestContext.requestBody = requestBody

    val userId: UserId = userIdResolver.resolve(requestBody.userIdentifier, requestBody.requestId)
    logger.info("Resolved userId: $userId for userIdentifier: ${requestBody.userIdentifier.value}")

    return V1ResponseBody.from(
        request = requestBody,
        responseDateTime = Instant.now().toString(),
        statusCode = 200,
        data = "Processing completed for user: $userId"
    )
}
```

**ApiV2Controller.kt** (25-40행)
```kotlin
fun processV2(@RequestBody requestBody: V2RequestBody): V2ResponseBody {
    val serviceId = requestContext.serviceId ?: "unknown"
    logger.info("Processing V2 request - serviceId: $serviceId, requestId: ${requestBody.requestId}")

    requestContext.requestBody = requestBody

    val userId: UserId = userIdResolver.resolve(requestBody.userIdentifier, requestBody.requestId)
    logger.info("Resolved userId: $userId for userIdentifier: ${requestBody.userIdentifier.value}")

    return V2ResponseBody.from(
        request = requestBody,
        responseDateTime = Instant.now().toString(),
        statusCode = 200,
        data = "Processing completed for user: $userId"
    )
}
```

**차이점**: 타입 이름만 다르고 로직은 100% 동일

### 1.2 유지보수성 문제

1. **변경 전파**: 비즈니스 로직 변경 시 V1, V2 모두 수정 필요 (휴먼 에러 가능성)
2. **테스트 중복**: 동일한 테스트 케이스를 V1/V2 각각 작성
3. **확장성 저하**: V3 추가 시 또 다른 중복 발생
4. **DRY 원칙 위반**: Don't Repeat Yourself 원칙을 따르지 않음

---

## 2. 리팩토링 전략

### 2.1 핵심 아이디어

**제네릭을 활용한 추상화 + 버전별 구체화**

- 공통 로직은 `Generic Abstract Class`로 추출
- 버전별 차이(타입)는 제네릭 타입 파라미터로 처리
- Spring의 타입 안전성과 직렬화 요구사항을 만족

### 2.2 적용 패턴

1. **Generic Template Method Pattern**: 추상 클래스에 공통 로직, 하위 클래스는 타입만 제공
2. **Strategy Pattern**: ResponseBody 생성 전략을 함수로 주입
3. **Factory Pattern**: ErrorResponseFactory는 현재대로 유지하되 호출 로직 통합

---

## 3. 리팩토링 상세 설계

### 3.1 Controller 리팩토링

#### Before (현재)

```
ApiV1Controller (42줄)
  ├─ processV1() 메서드
  └─ 중복 로직 100%

ApiV2Controller (42줄)
  ├─ processV2() 메서드
  └─ 중복 로직 100%
```

#### After (제안)

```
GenericApiController<REQ, RES> (추상 클래스)
  ├─ processRequest() 공통 로직
  └─ 제네릭으로 타입 안전성 확보

ApiV1Controller : GenericApiController<V1RequestBody, V1ResponseBody>
  └─ processV1() → processRequest() 위임 (5줄)

ApiV2Controller : GenericApiController<V2RequestBody, V2ResponseBody>
  └─ processV2() → processRequest() 위임 (5줄)
```

#### 구현 코드

**GenericApiController.kt** (새로운 파일)
```kotlin
package bong.presentationlayer.controller

import bong.presentationlayer.context.RequestContext
import bong.presentationlayer.domain.UserId
import bong.presentationlayer.dto.request.BaseRequestBody
import bong.presentationlayer.dto.response.BaseResponseBody
import bong.presentationlayer.service.UserIdResolver
import org.slf4j.LoggerFactory
import java.time.Instant

/**
 * 버전별 Controller의 공통 로직을 추출한 추상 클래스
 *
 * 제네릭을 활용해 타입 안전성을 유지하면서도 코드 중복을 제거
 *
 * @param REQ 요청 DTO 타입 (V1RequestBody or V2RequestBody)
 * @param RES 응답 DTO 타입 (V1ResponseBody or V2ResponseBody)
 */
abstract class GenericApiController<REQ : BaseRequestBody, RES : BaseResponseBody>(
    protected val userIdResolver: UserIdResolver,
    protected val requestContext: RequestContext
) {
    protected val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 공통 요청 처리 로직
     *
     * @param requestBody 요청 본문
     * @param createResponse ResponseBody 생성 팩토리 함수 (Strategy Pattern)
     * @return 버전별 ResponseBody
     */
    protected fun processRequest(
        requestBody: REQ,
        createResponse: (REQ, String, Int, String?) -> RES
    ): RES {
        val serviceId = requestContext.serviceId ?: "unknown"
        logger.info("Processing request - serviceId: $serviceId, requestId: ${requestBody.requestId}")

        // RequestContext에 요청 본문 저장
        requestContext.requestBody = requestBody

        // UserIdentifier → UserId 변환
        val userId: UserId = userIdResolver.resolve(
            requestBody.userIdentifier,
            requestBody.requestId
        )
        logger.info("Resolved userId: $userId for userIdentifier: ${requestBody.userIdentifier.value}")

        // 응답 생성 (버전별 팩토리 함수 호출)
        return createResponse(
            requestBody,
            Instant.now().toString(),
            200,
            "Processing completed for user: $userId"
        )
    }
}
```

**ApiV1Controller.kt** (리팩토링 후)
```kotlin
package bong.presentationlayer.controller

import bong.presentationlayer.context.RequestContext
import bong.presentationlayer.dto.request.V1RequestBody
import bong.presentationlayer.dto.response.V1ResponseBody
import bong.presentationlayer.service.UserIdResolver
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class ApiV1Controller(
    userIdResolver: UserIdResolver,
    requestContext: RequestContext
) : GenericApiController<V1RequestBody, V1ResponseBody>(userIdResolver, requestContext) {

    @PostMapping("/process", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun processV1(@RequestBody requestBody: V1RequestBody): V1ResponseBody =
        processRequest(requestBody, V1ResponseBody::from)
}
```

**ApiV2Controller.kt** (리팩토링 후)
```kotlin
package bong.presentationlayer.controller

import bong.presentationlayer.context.RequestContext
import bong.presentationlayer.dto.request.V2RequestBody
import bong.presentationlayer.dto.response.V2ResponseBody
import bong.presentationlayer.service.UserIdResolver
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2")
class ApiV2Controller(
    userIdResolver: UserIdResolver,
    requestContext: RequestContext
) : GenericApiController<V2RequestBody, V2ResponseBody>(userIdResolver, requestContext) {

    @PostMapping("/process", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun processV2(@RequestBody requestBody: V2RequestBody): V2ResponseBody =
        processRequest(requestBody, V2ResponseBody::from)
}
```

#### 코드 절감 효과

| 구분 | Before | After | 절감율 |
|-----|--------|-------|--------|
| **총 코드 라인** | 84줄 (42 × 2) | ~80줄 (추상 50 + V1 15 + V2 15) | 5% |
| **중복 로직** | 100% 중복 | 0% 중복 | **100%** |
| **V3 추가 시** | +42줄 | +15줄 | **64% 절감** |

---

### 3.2 ExceptionHandler 리팩토링

#### Before (현재)

```
GlobalExceptionHandlerV1 (61줄)
  ├─ handleUserNotFoundException()
  ├─ handleNotReadable()
  └─ handleException()

GlobalExceptionHandlerV2 (61줄)
  ├─ handleUserNotFoundException()
  ├─ handleNotReadable()
  └─ handleException()
```

**중복 로직**: 100% 동일 (타입만 다름)

#### After (제안)

```
GenericExceptionHandler<RES> (추상 클래스)
  ├─ handleUserNotFoundInternal() 공통 로직
  ├─ handleBadRequestInternal() 공통 로직
  ├─ handleInternalErrorInternal() 공통 로직
  └─ abstract factory: ApiErrorResponseFactory 제공

GlobalExceptionHandlerV1 : GenericExceptionHandler<V1ResponseBody>
  └─ V1ApiErrorResponseFactory 주입 (10줄)

GlobalExceptionHandlerV2 : GenericExceptionHandler<V2ResponseBody>
  └─ V2ApiErrorResponseFactory 주입 (10줄)
```

#### 구현 코드

**GenericExceptionHandler.kt** (새로운 파일)
```kotlin
package bong.presentationlayer.controller

import bong.presentationlayer.context.RequestContext
import bong.presentationlayer.dto.response.BaseResponseBody
import bong.presentationlayer.exception.UserNotFoundException
import bong.presentationlayer.exception.factory.ApiErrorResponseFactory
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity

/**
 * 버전별 ExceptionHandler의 공통 로직을 추출한 추상 클래스
 *
 * @param RES 응답 DTO 타입 (V1ResponseBody or V2ResponseBody)
 */
abstract class GenericExceptionHandler<RES : BaseResponseBody>(
    protected val requestContext: RequestContext,
    protected val factory: ApiErrorResponseFactory
) {
    /**
     * service-id 헤더 추출 공통 로직
     */
    protected fun serviceIdHeader(request: HttpServletRequest): String =
        request.getHeader("service-id") ?: requestContext.serviceId ?: "unknown"

    /**
     * UserNotFoundException 처리 공통 로직
     */
    protected fun handleUserNotFoundInternal(
        ex: UserNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<RES> {
        @Suppress("UNCHECKED_CAST")
        val body = factory.userNotFound(ex.userIdentifier, ex.transactionId) as RES

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .header("service-id", serviceIdHeader(request))
            .body(body)
    }

    /**
     * 잘못된 요청 처리 공통 로직
     */
    protected fun handleBadRequestInternal(
        ex: org.springframework.http.converter.HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<RES> {
        @Suppress("UNCHECKED_CAST")
        val body = factory.badRequest() as RES

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .header("service-id", serviceIdHeader(request))
            .body(body)
    }

    /**
     * 일반 예외 처리 공통 로직
     */
    protected fun handleInternalErrorInternal(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<RES> {
        @Suppress("UNCHECKED_CAST")
        val body = factory.internalError() as RES

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .header("service-id", serviceIdHeader(request))
            .body(body)
    }
}
```

**GlobalExceptionHandlerV1.kt** (리팩토링 후)
```kotlin
package bong.presentationlayer.controller

import bong.presentationlayer.context.RequestContext
import bong.presentationlayer.dto.response.V1ResponseBody
import bong.presentationlayer.exception.UserNotFoundException
import bong.presentationlayer.exception.factory.V1ApiErrorResponseFactory
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [ApiV1Controller::class])
@Order(Ordered.HIGHEST_PRECEDENCE)
class GlobalExceptionHandlerV1(
    requestContext: RequestContext,
    factory: V1ApiErrorResponseFactory
) : GenericExceptionHandler<V1ResponseBody>(requestContext, factory) {

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(
        ex: UserNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<V1ResponseBody> = handleUserNotFoundInternal(ex, request)

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException::class)
    fun handleNotReadable(
        ex: org.springframework.http.converter.HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<V1ResponseBody> = handleBadRequestInternal(ex, request)

    @ExceptionHandler(Exception::class)
    fun handleException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<V1ResponseBody> = handleInternalErrorInternal(ex, request)
}
```

**GlobalExceptionHandlerV2.kt** (리팩토링 후)
```kotlin
package bong.presentationlayer.controller

import bong.presentationlayer.context.RequestContext
import bong.presentationlayer.dto.response.V2ResponseBody
import bong.presentationlayer.exception.UserNotFoundException
import bong.presentationlayer.exception.factory.V2ApiErrorResponseFactory
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [ApiV2Controller::class])
@Order(Ordered.HIGHEST_PRECEDENCE)
class GlobalExceptionHandlerV2(
    requestContext: RequestContext,
    factory: V2ApiErrorResponseFactory
) : GenericExceptionHandler<V2ResponseBody>(requestContext, factory) {

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(
        ex: UserNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<V2ResponseBody> = handleUserNotFoundInternal(ex, request)

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException::class)
    fun handleNotReadable(
        ex: org.springframework.http.converter.HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<V2ResponseBody> = handleBadRequestInternal(ex, request)

    @ExceptionHandler(Exception::class)
    fun handleException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<V2ResponseBody> = handleInternalErrorInternal(ex, request)
}
```

#### 코드 절감 효과

| 구분 | Before | After | 절감율 |
|-----|--------|-------|--------|
| **총 코드 라인** | 122줄 (61 × 2) | ~115줄 (추상 75 + V1 20 + V2 20) | 6% |
| **중복 로직** | 100% 중복 | 0% 중복 | **100%** |
| **V3 추가 시** | +61줄 | +20줄 | **67% 절감** |

---

### 3.3 ErrorFactory 리팩토링 (선택사항)

현재 `V1ApiErrorResponseFactory`와 `V2ApiErrorResponseFactory`도 중복이 있지만, 다음 이유로 **현재대로 유지**를 권장합니다:

**유지 권장 이유:**
1. 코드가 간결함 (각 40줄 미만)
2. 각 버전별로 명확한 캡슐화
3. V1은 `userIdentifier as UserIdentifier.CI` 타입 캐스팅이 필요 (V2와 로직 차이)
4. 추상화 시 얻는 이득 < 복잡도 증가

**대안 (원한다면):**
- Generic Abstract Factory 패턴 적용 가능
- 하지만 타입 캐스팅 문제로 코드 복잡도 증가

**판단**: 현재대로 유지하되, 로직이 복잡해지면 그때 리팩토링

---

## 4. 리팩토링 실행 계획

### 4.1 단계별 실행 (TDD 기반)

#### Phase 1: GenericApiController 도입

```
1. GenericApiController 추상 클래스 작성
2. 기존 ApiControllerTest를 그대로 두고 모든 테스트 통과 확인
3. ApiV1Controller 리팩토링
4. 테스트 실행 → 모두 통과 확인
5. ApiV2Controller 리팩토링
6. 테스트 실행 → 모두 통과 확인
```

**예상 소요 시간**: 30분

#### Phase 2: GenericExceptionHandler 도입

```
1. GenericExceptionHandler 추상 클래스 작성
2. GlobalExceptionHandlerV1 리팩토링
3. 테스트 실행 (404, 400 에러 케이스)
4. GlobalExceptionHandlerV2 리팩토링
5. 테스트 실행 (404, 400 에러 케이스)
```

**예상 소요 시간**: 30분

#### Phase 3: 통합 테스트 및 문서화

```
1. 전체 테스트 스위트 실행
2. project_spec.md 업데이트 (컴포넌트 역할 및 책임 섹션)
3. 리팩토링 전/후 비교 문서 작성
```

**예상 소요 시간**: 20분

**총 소요 시간**: 약 1시간 20분

### 4.2 테스트 전략

#### 기존 테스트 활용

현재 `ApiControllerTest`에는 다음 시나리오가 이미 구현되어 있습니다:

- ✅ V1 정상 요청 처리
- ✅ V1 404 에러 (회원 미존재)
- ✅ V1 400 에러 (잘못된 JSON)
- ✅ V2 정상 요청 처리 (DI, Email)
- ✅ V2 404 에러
- ✅ V2 400 에러

**리팩토링 후에도 모든 테스트는 변경 없이 그대로 통과해야 합니다.**

#### 추가 테스트 (선택)

```kotlin
test("GenericApiController - processRequest 공통 로직 검증") {
    // GenericApiController의 processRequest 메서드를 직접 테스트
    // Mock 기반으로 공통 로직만 검증
}
```

---

## 5. 리팩토링 효과 분석

### 5.1 정량적 효과

#### 코드 중복 제거

| 항목 | Before | After | 개선 |
|-----|--------|-------|------|
| **Controller 중복** | 100% | 0% | ✅ 완전 제거 |
| **ExceptionHandler 중복** | 100% | 0% | ✅ 완전 제거 |
| **총 라인 수** | 206줄 | ~195줄 | 5% 감소 |

#### V3 추가 시 필요한 코드량

| 컴포넌트 | Before | After | 절감 |
|---------|--------|-------|------|
| Controller | 42줄 | 15줄 | **64%** |
| ExceptionHandler | 61줄 | 20줄 | **67%** |
| ErrorFactory | 40줄 | 40줄 | - |
| **합계** | **143줄** | **75줄** | **47%** |

### 5.2 정성적 효과

#### 유지보수성 향상

1. **단일 책임 원칙 (SRP)**
   - 공통 로직은 추상 클래스에 응집
   - 버전별 구현은 타입 제공에만 집중

2. **개방-폐쇄 원칙 (OCP)**
   - 새로운 버전 추가 시 기존 코드 수정 불필요
   - 추상 클래스 상속으로 확장

3. **DRY 원칙 준수**
   - 중복 로직 완전 제거
   - 수정 사항이 자동으로 모든 버전에 반영

#### 테스트 용이성

1. **공통 로직 단위 테스트**
   - `GenericApiController.processRequest()` 단독 테스트 가능
   - Mock 기반으로 격리된 테스트

2. **버전별 통합 테스트**
   - 기존 테스트 그대로 유지
   - 리팩토링 후에도 100% 통과

#### 가독성

1. **의도 명확화**
   - "공통 로직"과 "버전별 차이"가 코드 레벨에서 명확히 구분
   - 새로운 개발자도 구조 이해 용이

2. **코드 탐색 개선**
   - 공통 로직: `GenericApiController`만 확인
   - 버전별 차이: 각 V1/V2 Controller 확인

---

## 6. 위험 요소 및 대응 방안

### 6.1 잠재적 위험

#### 위험 1: 타입 안전성 약화

**우려 사항:**
- `as RES` 타입 캐스팅 사용 (`GenericExceptionHandler`에서)
- 런타임 에러 가능성

**대응 방안:**
```kotlin
// 컴파일 타임에 타입 체크
inline fun <reified RES : BaseResponseBody> createTypeSafeResponse(
    factory: ApiErrorResponseFactory,
    creator: (ApiErrorResponseFactory) -> BaseResponseBody
): RES {
    val response = creator(factory)
    require(response is RES) { "Invalid response type" }
    return response
}
```

하지만 현재 구조에서는 `factory`가 반드시 올바른 타입을 반환하도록 설계되어 있어 **실질적 위험도는 낮음**

#### 위험 2: 제네릭 복잡도

**우려 사항:**
- 제네릭 문법이 낯선 개발자에게는 진입장벽

**대응 방안:**
- 충분한 문서화 (KDoc 주석)
- 예제 코드 제공 (V1, V2 구현 참고)

### 6.2 롤백 전략

리팩토링이 문제가 될 경우:

```bash
# Git으로 쉽게 롤백 가능
git revert <commit-hash>
```

모든 단계마다 커밋하여 부분 롤백도 가능

---

## 7. 대안 비교

### 7.1 대안 1: 현재 구조 유지

**장점:**
- 변경 없음 (위험 0)
- 각 버전이 완전히 독립적

**단점:**
- 중복 코드 지속
- V3 추가 시 143줄 추가
- 휴먼 에러 위험

**평가:** ❌ 기술 부채 누적

### 7.2 대안 2: Unified Controller (단일 컨트롤러)

```kotlin
@RestController
class UnifiedController {
    @PostMapping("/api/v1/process")
    fun v1(@RequestBody body: V1RequestBody) = process(body) { ... }

    @PostMapping("/api/v2/process")
    fun v2(@RequestBody body: V2RequestBody) = process(body) { ... }
}
```

**장점:**
- 하나의 클래스로 통합

**단점:**
- 버전별 책임 경계 모호
- `@RestControllerAdvice(assignableTypes)` 사용 불가
- 단일 클래스가 비대해짐

**평가:** ⚠️ 구조적으로 덜 명확

### 7.3 대안 3: 제안된 Generic Abstract Class (추천)

**장점:**
- ✅ 중복 제거
- ✅ 타입 안전성 유지
- ✅ 버전별 명확한 책임 분리
- ✅ 확장 용이

**단점:**
- 제네릭 문법 (학습 곡선 존재)

**평가:** ✅ **최적의 균형점**

---

## 8. 실행 체크리스트

### Phase 1: GenericApiController 도입

- [ ] `GenericApiController.kt` 작성
- [ ] `ApiV1Controller.kt` 리팩토링
- [ ] V1 관련 테스트 모두 통과 확인
- [ ] `ApiV2Controller.kt` 리팩토링
- [ ] V2 관련 테스트 모두 통과 확인
- [ ] 커밋: `refactor(controller): introduce GenericApiController to eliminate duplication`

### Phase 2: GenericExceptionHandler 도입

- [ ] `GenericExceptionHandler.kt` 작성
- [ ] `GlobalExceptionHandlerV1.kt` 리팩토링
- [ ] V1 에러 케이스 테스트 통과 확인
- [ ] `GlobalExceptionHandlerV2.kt` 리팩토링
- [ ] V2 에러 케이스 테스트 통과 확인
- [ ] 커밋: `refactor(exception): introduce GenericExceptionHandler to eliminate duplication`

### Phase 3: 문서화 및 검증

- [ ] 전체 테스트 스위트 실행 (통과율 100%)
- [ ] `project_spec.md` 업데이트
- [ ] 리팩토링 전/후 비교 문서 작성
- [ ] 커밋: `docs(spec): update component architecture after refactoring`

---

## 9. 예상 Q&A

### Q1: 제네릭을 쓰면 성능이 떨어지나요?

**A:** 아니오. Kotlin의 제네릭은 **Type Erasure** 방식으로 컴파일되어 런타임 오버헤드가 없습니다. 오히려 인라인 함수와 결합하면 성능 향상도 가능합니다.

### Q2: V1과 V2의 로직이 달라지면 어떻게 하나요?

**A:** 다음과 같이 처리 가능합니다:

```kotlin
abstract class GenericApiController<REQ, RES> {
    protected open fun additionalProcessing(requestBody: REQ) {
        // 기본 구현: 아무것도 안 함
    }
}

class ApiV2Controller : GenericApiController<V2RequestBody, V2ResponseBody>() {
    override fun additionalProcessing(requestBody: V2RequestBody) {
        // V2만의 특별한 로직
    }
}
```

Template Method Pattern으로 확장 포인트 제공

### Q3: 타입 캐스팅이 안전한가요?

**A:** 현재 구조에서는 안전합니다:

```kotlin
// V1ApiErrorResponseFactory는 항상 V1ResponseBody 반환
// V2ApiErrorResponseFactory는 항상 V2ResponseBody 반환
// 생성자에서 올바른 factory가 주입되므로 타입 불일치 불가능
```

컴파일 타임 타입 체크 + 의존성 주입으로 보장

### Q4: 기존 테스트를 수정해야 하나요?

**A:** 아니요. 리팩토링은 **내부 구현**만 변경하고 **공개 API**는 동일합니다. 모든 테스트는 수정 없이 통과해야 합니다.

---

## 10. 결론

### 리팩토링 필요성

현재 프로젝트는 V1/V2 간 **100% 중복된 로직**을 가지고 있어:
- ❌ 유지보수성 저하
- ❌ 휴먼 에러 위험
- ❌ 확장성 제약

### 제안된 솔루션

**Generic Abstract Class 패턴** 적용으로:
- ✅ 중복 코드 100% 제거
- ✅ 타입 안전성 유지
- ✅ V3 추가 시 코드량 47% 절감
- ✅ 테스트 용이성 향상

### 실행 권장

- **복잡도**: 낮음 (제네릭 기본 지식만 필요)
- **위험도**: 낮음 (테스트로 안전성 보장)
- **효과**: 높음 (즉시 체감 가능한 개선)
- **소요 시간**: 약 1시간 20분

### Next Steps

1. 본 제안서 리뷰 및 승인
2. Phase 1 실행 (Controller 리팩토링)
3. Phase 2 실행 (ExceptionHandler 리팩토링)
4. 문서화 및 공유

---

**문서 상태**: 제안 단계
**작성자**: Claude Code
**검토 필요**: 프로젝트 오너
