# Presentation Layer 구현 계획

## 프로젝트 개요
Kotlin + Spring Boot 기반의 Presentation Layer 구현 및 테스트 작성

## 목표
1. Request/Response DTO 구조 설계 및 구현
2. kotlinx.serialization 기반 직렬화 적용
3. RestController 구현
4. 통합 및 단위 테스트 작성

---

## Phase 1: 프로젝트 설정 및 의존성 추가

### 1.1 의존성 추가
- [ ] kotlinx.serialization 플러그인 및 라이브러리 추가
- [ ] Kotest 의존성 추가 (kotest-runner-junit5, kotest-assertions-core, kotest-spring)
- [ ] MockK 의존성 추가
- [ ] spring-boot-starter-test 확인 (이미 존재)
- [ ] MockMvc 설정 확인

**파일**: `build.gradle.kts`

```kotlin
plugins {
    // 기존 플러그인들...
    kotlin("plugin.serialization") version "1.9.25"
}

dependencies {
    // 기존 의존성들...

    // kotlinx.serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Kotest
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")

    // MockK
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}
```

---

## Phase 2: DTO 구조 설계 및 구현

### 2.1 BaseRequestBody 설계 및 구현
공통 프로퍼티를 가진 추상 클래스 또는 인터페이스 구현

**파일**: `src/main/kotlin/bong/presentationlayer/dto/BaseRequestBody.kt`

```kotlin
interface BaseRequestBody {
    val requestId: String  // 요청 ID
    val requestDateTime: String    // 요청 시간
}
```

### 2.2 V1, V2 RequestBody 구현
각 버전별 특정 프로퍼티를 가진 구현체 작성

**파일**:
- `src/main/kotlin/bong/presentationlayer/dto/V1RequestBody.kt`
- `src/main/kotlin/bong/presentationlayer/dto/V2RequestBody.kt`

```kotlin
// V1 예시
@Serializable
data class V1RequestBody(
    @SerialName("api_transaction_id")
    override val requestId: String,
    @SerialName("request_time")
    override val requestDateTime: String,
    @SerialName("ci")
    val ci: Ci  // V1 특화 필드
) : BaseRequestBody

// V2 예시
@Serializable
data class V2RequestBody(
    @SerialName("api_transaction_id")
    override val requestId: String,
    @SerialName("request_time")
    override val requestDateTime: String,
    @SerialName("user_identifier")
    val userIdentifier: UserIdentifier
) : BaseRequestBody
```

### 2.3 Response DTO 구현
Request와 쌍을 이루는 Response 구조 구현

**파일**:
- `src/main/kotlin/bong/presentationlayer/dto/BaseResponse.kt`
- `src/main/kotlin/bong/presentationlayer/dto/V1Response.kt`
- `src/main/kotlin/bong/presentationlayer/dto/V2Response.kt`

```kotlin
@Serializable
data class BaseResponse<T>(
    val statusCode: Int,
    val message: String,
    val data: T? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class V1Response(
    val result: String
)

@Serializable
data class V2Response(
    val results: List<String>,
    val count: Int
)
```

### 2.4 에러 응답 구조 구현

**파일**: `src/main/kotlin/bong/presentationlayer/dto/ErrorResponse.kt`

```kotlin
@Serializable
data class ErrorResponse(
    val errorCode: String,
    val errorMessage: String,
    val details: Map<String, String>? = null
)
```

---

## Phase 3: kotlinx.serialization 설정

### 3.1 HttpMessageConverter 설정
Spring Boot에서 kotlinx.serialization을 사용하기 위한 설정

**파일**: `src/main/kotlin/bong/presentationlayer/config/SerializationConfig.kt`

```kotlin
@Configuration
class SerializationConfig : WebMvcConfigurer {
    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        val jsonConverter = KotlinSerializationJsonHttpMessageConverter(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        })
        converters.add(0, jsonConverter)
    }
}
```

### 3.2 Custom Serializers (필요시)
특정 타입에 대한 커스텀 시리얼라이저 구현

---

## Phase 4: RestController 구현

### 4.1 V1 Controller 구현

**파일**: `src/main/kotlin/bong/presentationlayer/controller/V1Controller.kt`

```kotlin
@RestController
@RequestMapping("/api/v1")
class V1Controller {

    @PostMapping("/process")
    fun processV1Request(@RequestBody request: V1RequestBody): ResponseEntity<BaseResponse<V1Response>> {
        // 비즈니스 로직 처리
        val response = V1Response(result = "Processed: ${request.data}")
        return ResponseEntity.ok(
            BaseResponse(
                statusCode = 200,
                message = "Success",
                data = response
            )
        )
    }

    @GetMapping("/health")
    fun health(): ResponseEntity<BaseResponse<String>> {
        return ResponseEntity.ok(
            BaseResponse(
                statusCode = 200,
                message = "V1 API is healthy",
                data = "OK"
            )
        )
    }
}
```

### 4.2 V2 Controller 구현

**파일**: `src/main/kotlin/bong/presentationlayer/controller/V2Controller.kt`

```kotlin
@RestController
@RequestMapping("/api/v2")
class V2Controller {

    @PostMapping("/process")
    fun processV2Request(@RequestBody request: V2RequestBody): ResponseEntity<BaseResponse<V2Response>> {
        // 비즈니스 로직 처리
        val response = V2Response(
            results = request.payload.values.toList(),
            count = request.payload.size
        )
        return ResponseEntity.ok(
            BaseResponse(
                statusCode = 200,
                message = "Success",
                data = response
            )
        )
    }
}
```

### 4.3 GlobalExceptionHandler 구현

**파일**: `src/main/kotlin/bong/presentationlayer/exception/GlobalExceptionHandler.kt`

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<BaseResponse<ErrorResponse>> {
        val errorResponse = ErrorResponse(
            errorCode = "INTERNAL_ERROR",
            errorMessage = ex.message ?: "Unknown error"
        )
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                BaseResponse(
                    statusCode = 500,
                    message = "Error",
                    data = errorResponse
                )
            )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<BaseResponse<ErrorResponse>> {
        val errorResponse = ErrorResponse(
            errorCode = "BAD_REQUEST",
            errorMessage = ex.message ?: "Invalid argument"
        )
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                BaseResponse(
                    statusCode = 400,
                    message = "Bad Request",
                    data = errorResponse
                )
            )
    }
}
```

---

## Phase 5: 통합 테스트 구현

### 5.1 MockMvc 기반 통합 테스트 설정

**파일**: `src/test/kotlin/bong/presentationlayer/controller/V1ControllerIntegrationTest.kt`

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class V1ControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `POST v1 process should return success response`() {
        val requestBody = """
            {
                "requestId": "test-123",
                "timestamp": 1234567890,
                "version": "v1",
                "data": "test data"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/v1/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.message").value("Success"))
            .andExpect(jsonPath("$.data.result").exists())
    }

    @Test
    fun `GET v1 health should return healthy status`() {
        mockMvc.perform(get("/api/v1/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.data").value("OK"))
    }
}
```

### 5.2 V2 통합 테스트

**파일**: `src/test/kotlin/bong/presentationlayer/controller/V2ControllerIntegrationTest.kt`

---

## Phase 6: 단위 테스트 구현 (Kotest + MockK)

### 6.1 DTO 단위 테스트

**파일**: `src/test/kotlin/bong/presentationlayer/dto/V1RequestBodyTest.kt`

```kotlin
class V1RequestBodyTest : FunSpec({

    test("V1RequestBody should serialize correctly") {
        val request = V1RequestBody(
            requestId = "test-123",
            timestamp = 1234567890L,
            version = "v1",
            data = "test data"
        )

        val json = Json.encodeToString(request)

        json shouldContain "test-123"
        json shouldContain "test data"
    }

    test("V1RequestBody should deserialize correctly") {
        val json = """
            {
                "requestId": "test-123",
                "timestamp": 1234567890,
                "version": "v1",
                "data": "test data"
            }
        """.trimIndent()

        val request = Json.decodeFromString<V1RequestBody>(json)

        request.requestId shouldBe "test-123"
        request.data shouldBe "test data"
    }

    test("V1RequestBody version should default to v1") {
        val request = V1RequestBody(
            requestId = "test",
            timestamp = 123L,
            data = "data"
        )

        request.version shouldBe "v1"
    }
})
```

### 6.2 Controller 단위 테스트 (MockK)

**파일**: `src/test/kotlin/bong/presentationlayer/controller/V1ControllerTest.kt`

```kotlin
class V1ControllerTest : FunSpec({

    lateinit var controller: V1Controller

    beforeTest {
        controller = V1Controller()
    }

    test("processV1Request should return success response") {
        val request = V1RequestBody(
            requestId = "test-123",
            timestamp = 1234567890L,
            data = "test data"
        )

        val response = controller.processV1Request(request)

        response.statusCode shouldBe HttpStatus.OK
        response.body?.statusCode shouldBe 200
        response.body?.message shouldBe "Success"
        response.body?.data?.result shouldContain "test data"
    }

    test("health endpoint should return healthy status") {
        val response = controller.health()

        response.statusCode shouldBe HttpStatus.OK
        response.body?.statusCode shouldBe 200
        response.body?.data shouldBe "OK"
    }
})
```

### 6.3 Exception Handler 단위 테스트

**파일**: `src/test/kotlin/bong/presentationlayer/exception/GlobalExceptionHandlerTest.kt`

---

## Phase 7: 문서화 및 검증

### 7.1 API 문서 작성
- [ ] README.md에 API 엔드포인트 문서화
- [ ] Request/Response 예시 추가

### 7.2 테스트 커버리지 확인
- [ ] 전체 테스트 실행 및 성공 확인
- [ ] 커버리지 측정 (JaCoCo 등)

### 7.3 코드 리뷰 체크리스트
- [ ] 모든 DTO에 @Serializable 어노테이션 적용 확인
- [ ] Request/Response 쌍 일치 확인
- [ ] 에러 응답이 status code로 정확히 응답되는지 확인
- [ ] BaseRequestBody 상속 구조 검증
- [ ] 단위 테스트와 통합 테스트 분리 확인

---

## 구현 순서 요약

1. **의존성 설정** (Phase 1)
   - kotlinx.serialization, Kotest, MockK 추가

2. **DTO 구현** (Phase 2)
   - BaseRequestBody → V1/V2 RequestBody
   - BaseResponse → V1/V2 Response
   - ErrorResponse

3. **직렬화 설정** (Phase 3)
   - SerializationConfig 구현

4. **Controller 구현** (Phase 4)
   - V1/V2 Controller
   - GlobalExceptionHandler

5. **통합 테스트** (Phase 5)
   - MockMvc 기반 테스트

6. **단위 테스트** (Phase 6)
   - Kotest + MockK 기반 테스트

7. **문서화 및 검증** (Phase 7)

---

## 참고 사항

### kotlinx.serialization vs Jackson
- 현재 프로젝트는 `jackson-module-kotlin`이 포함되어 있음
- kotlinx.serialization을 사용하려면 HttpMessageConverter 커스터마이징 필요
- 또는 Jackson과 병행 사용 가능 (어노테이션으로 구분)

### 테스트 전략
- **통합 테스트**: 전체 Spring Context 로드, 실제 HTTP 요청/응답 검증
- **단위 테스트**: Controller 로직만 독립적으로 테스트, 빠른 피드백

### 디렉토리 구조 (권장)
```
src/main/kotlin/bong/presentationlayer/
├── PresentationlayerApplication.kt
├── config/
│   └── SerializationConfig.kt
├── controller/
│   ├── V1Controller.kt
│   └── V2Controller.kt
├── dto/
│   ├── BaseRequestBody.kt
│   ├── BaseResponse.kt
│   ├── ErrorResponse.kt
│   ├── V1RequestBody.kt
│   ├── V1Response.kt
│   ├── V2RequestBody.kt
│   └── V2Response.kt
└── exception/
    └── GlobalExceptionHandler.kt

src/test/kotlin/bong/presentationlayer/
├── PresentationlayerApplicationTests.kt
├── controller/
│   ├── V1ControllerTest.kt
│   ├── V1ControllerIntegrationTest.kt
│   ├── V2ControllerTest.kt
│   └── V2ControllerIntegrationTest.kt
├── dto/
│   ├── V1RequestBodyTest.kt
│   └── V2RequestBodyTest.kt
└── exception/
    └── GlobalExceptionHandlerTest.kt
```

---

## 다음 단계

이 계획서를 기반으로:
1. Phase 1부터 순차적으로 구현 시작
2. 각 Phase 완료 후 테스트 실행 및 검증
3. 필요시 계획 수정 및 보완

구현을 시작할 준비가 되면 알려주세요!
