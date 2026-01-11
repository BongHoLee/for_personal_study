## Presentation Layer 리팩토링 제안 (예시 포함)

이 문서는 현재 프로젝트 구조에서
 a) `ApiV1Controller`/`ApiV2Controller`,
 b) `GlobalExceptionHandlerV1`/`V2`,
 c) 버전별 DTO 중복을 줄이기 위한 구체적인 리팩토링 절차와 예시 코드를 담고 있습니다.

---

### 1. 문제 요약

- **중복 컨트롤러/핸들러**: 버전별 Controller·ExceptionHandler가 거의 동일한 로직을 반복하며 타입만 다릅니다.
- **DTO 확산**: `V1ResponseBody`와 `V2ResponseBody`가 필드명만 다른데도 전체 클래스를 별도 유지해야 합니다.
- **에러 응답 팩토리 분산**: `V1ApiErrorResponseFactory`, `V2ApiErrorResponseFactory`가 사실상 같은 책임을 수행합니다.

결과적으로 기능 확장 시 V1/V2 모두를 수정해야 하므로 유지보수 비용이 커집니다.

---

### 2. 리팩토링 방향 개요

| 단계 | 목표 | 핵심 아이디어 |
| --- | --- | --- |
| ① 버전 해석 공통화 | 모든 요청에 `ApiVersion`을 주입 | `RequestContext`에 `apiVersion` 필드를 추가하고 `HandlerInterceptor`에서 채움 |
| ② Canonical DTO 도입 | 컨트롤러/서비스에서 단일 모델 사용 | `ProcessRequest`, `ProcessResult` 등 내부 전용 DTO 정의 |
| ③ Version Adapter 계층 | JSON ↔ Canonical 변환 전담 | `VersionRequestAdapter`, `VersionResponseAdapter` 인터페이스 + V1/V2 구현 |
| ④ 공통 Controller/Exception | 한 구현체만 유지 | Adapter/Factory를 전략으로 주입해 버전별 DTO 생성 |
| ⑤ 직렬화 전략 선택 | Kotlinx/Jackson 중 택 1 또는 혼용 | Jackson `MappingJacksonValue` 기반 뷰 전환 or 어댑터가 DTO 직접 생성 |

---

### 3. 예시: Canonical 흐름 + Adapter

#### 3.1 Canonical DTO

```kotlin
data class ProcessRequest(
    val requestId: String,
    val requestDateTime: String,
    val userIdentifier: UserIdentifier
)

data class ProcessResult(
    val statusCode: Int,
    val requestId: String?,
    val requestDateTime: String?,
    val responseDateTime: String?,
    val userIdentifier: UserIdentifier?,
    val data: String?
)
```

#### 3.2 Adapter 인터페이스

```kotlin
interface VersionRequestAdapter {
    val version: ApiVersion
    fun supports(bodyClass: Class<*>): Boolean
    fun toCanonical(rawBody: BaseRequestBody): ProcessRequest
}

interface VersionResponseAdapter {
    val version: ApiVersion
    fun fromCanonical(result: ProcessResult): BaseResponseBody
}
```

#### 3.3 구현체 예시 (V1)

```kotlin
@Component
class V1ProcessAdapter : VersionRequestAdapter, VersionResponseAdapter {
    override val version = ApiVersion.V1

    override fun supports(bodyClass: Class<*>) = bodyClass == V1RequestBody::class.java

    override fun toCanonical(rawBody: BaseRequestBody): ProcessRequest {
        val body = rawBody as V1RequestBody
        return ProcessRequest(
            requestId = body.requestId,
            requestDateTime = body.requestDateTime,
            userIdentifier = body.userIdentifier
        )
    }

    override fun fromCanonical(result: ProcessResult): BaseResponseBody {
        return V1ResponseBody(
            statusCode = result.statusCode,
            userIdentifier = result.userIdentifier as? UserIdentifier.CI,
            requestId = result.requestId,
            requestDateTime = result.requestDateTime,
            responseDateTime = result.responseDateTime,
            data = result.data
        )
    }
}
```

V2 역시 동일한 패턴으로 구현합니다. 이때 `UserIdentifierAsStringSerializer`는 그대로 활용하고, V2는 기존 JSON 구조를 유지합니다.

#### 3.4 공통 Controller 예시

```kotlin
@RestController
@RequestMapping("/api")
class ProcessController(
    private val userIdResolver: UserIdResolver,
    private val requestContext: RequestContext,
    adapters: List<VersionResponseAdapter>
) {
    private val adapterMap = adapters.associateBy { it.version }

    @PostMapping("/v1/process")
    fun processV1(@RequestBody body: V1RequestBody) = handle(body)

    @PostMapping("/v2/process")
    fun processV2(@RequestBody body: V2RequestBody) = handle(body)

    private fun handle(body: BaseRequestBody): BaseResponseBody {
        requestContext.requestBody = body

        val canonical = ProcessRequest(
            requestId = body.requestId,
            requestDateTime = body.requestDateTime,
            userIdentifier = body.userIdentifier
        )

        val userId = userIdResolver.resolve(canonical.userIdentifier, canonical.requestId)

        val result = ProcessResult(
            statusCode = 200,
            requestId = canonical.requestId,
            requestDateTime = canonical.requestDateTime,
            responseDateTime = Instant.now().toString(),
            userIdentifier = canonical.userIdentifier,
            data = "Processing completed for user: $userId"
        )

        val version = requestContext.apiVersion ?: ApiVersion.UNKNOWN
        val adapter = adapterMap[version]
            ?: error("Unsupported version: $version")

        return adapter.fromCanonical(result)
    }
}
```

> `RequestContextInterceptor`에서 `requestContext.apiVersion = apiVersionResolver.resolve(request.requestURI)`를 먼저 세팅했다고 가정합니다.

#### 3.5 ExceptionHandler 통합

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler(
    private val requestContext: RequestContext,
    factories: List<ApiErrorResponseFactory>
) {
    private val factoryMap = factories.associateBy { it.version }

    private fun resolveFactory() =
        factoryMap[requestContext.apiVersion] ?: factoryMap[ApiVersion.V1]!!

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(
        ex: UserNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<BaseResponseBody> {
        val body = resolveFactory().userNotFound(ex.userIdentifier, ex.transactionId)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .header("service-id", request.getHeader("service-id") ?: "unknown")
            .body(body)
    }

    // BAD_REQUEST, INTERNAL_ERROR도 동일 로직
}
```

이렇게 하면 Factory는 그대로 재사용하면서 Handler는 하나만 유지할 수 있습니다.

---

### 4. 직렬화 전략 비교

| 전략 | 장점 | 단점 | 적용 위치 |
| --- | --- | --- | --- |
| Adapter → DTO 반환 (현재 예시) | 기존 Jackson 설정 그대로 활용, 구조 단순 | 버전마다 DTO는 여전히 존재 | Controller/ExceptionHandler |
| Jackson `@JsonView` | DTO 한 개로 다양한 뷰 표현 | 필드 단위 뷰 관리가 번거롭고 Kotlinx 혼합 시 복잡 | ResponseBodyAdvice + MappingJacksonValue |
| Custom `HttpMessageConverter` | JSON 필드까지 완전 맞춤 제어 | 구현 난이도/테스트 비용 높음 | 전역 Converter |

현 시점에서는 “Canonical DTO + Adapter” 패턴이 구현 난이도 대비 이득이 가장 큽니다. 필요 시 Jackson 뷰로 확장하면 DTO 수를 줄일 수 있습니다.

---

### 5. 단계별 적용 순서 (추천)

1. **`RequestContext` 확장**: `var apiVersion: ApiVersion? = null` 추가, Interceptor에서 설정.
2. **Canonical DTO 도입**: `ProcessRequest`, `ProcessResult` 등 내부 모델 작성.
3. **Adapter 구현**: `VersionRequestAdapter`, `VersionResponseAdapter` 정의 후 V1/V2 구현.
4. **Controller 통합**: 기존 `ApiV1Controller`, `ApiV2Controller` 코드를 `ProcessController`로 합치고 Adapter 사용.
5. **ExceptionHandler 통합**: `GlobalExceptionHandlerV1/V2`를 하나로 합치고 Factory 전략선택 로직 추가.
6. **테스트 정비**: 버전별 JSON 스냅샷 테스트로 회귀 검증.

---

### 6. 추가 고려사항

- **TransactionHistory 로깅**: 기존 `RequestContext.requestBody`는 버전 DTO가 저장되므로, canonical 값을 함께 저장해두면 후속 기능에서 재사용이 수월합니다.
- **UserIdentifier 확장**: V3 이상에서 필드명이 또 달라지더라도 Adapter만 추가하면 되므로 확장성이 확보됩니다.
- **kotlinx 혼용**: 향후 gRPC 등 다른 포맷을 도입할 때를 대비해 canonical DTO는 순수 Kotlin 데이터 클래스로 유지하는 것이 좋습니다.

---

필요하면 위 예시 코드를 토대로 실제 리팩토링 브랜치를 생성하거나 테스트 케이스까지 확장해드릴 수 있습니다.

---

### 00. 검토

- `ExceptionHandler`, 