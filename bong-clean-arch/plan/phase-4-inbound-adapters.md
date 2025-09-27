# Phase 4: 인바운드 어댑터 (웹 컨트롤러) 구현

## 목표
- REST API 컨트롤러 구현
- 웹 계층과 애플리케이션 계층 분리
- 입력 검증 및 에러 처리
- API 문서화

## 이론 학습 포인트

### 인바운드 어댑터 역할
1. **외부 요청을 내부 형식으로 변환**
   - HTTP 요청 → Command/Query 객체
   - JSON → 도메인 객체

2. **프로토콜 특화 로직 처리**
   - HTTP 상태 코드
   - 헤더 처리
   - 인증/인가

3. **입력 검증**
   - 데이터 형식 검증
   - 비즈니스 규칙 검증은 애플리케이션/도메인에서

## 실습 과제

### 1. 웹 모델 정의

```kotlin
// 송금 요청 DTO
data class SendMoneyRequest(
    @field:NotNull
    val sourceAccountId: Long,
    
    @field:NotNull
    val targetAccountId: Long,
    
    @field:NotNull
    @field:Positive
    val amount: BigDecimal
)

// 송금 응답 DTO
data class SendMoneyResponse(
    val success: Boolean,
    val message: String? = null
)

// 계좌 잔액 응답 DTO
data class AccountBalanceResponse(
    val accountId: Long,
    val balance: BigDecimal
)

// 거래 내역 응답 DTO
data class ActivityResponse(
    val id: Long?,
    val timestamp: LocalDateTime,
    val sourceAccountId: Long,
    val targetAccountId: Long,
    val amount: BigDecimal
)
```

### 2. REST 컨트롤러 구현

```kotlin
@RestController
@RequestMapping("/accounts")
@Validated
class AccountController(
    private val sendMoneyUseCase: SendMoneyUseCase,
    private val getAccountBalanceQuery: GetAccountBalanceQuery,
    private val getAccountActivityQuery: GetAccountActivityQuery
) {

    @PostMapping("/{sourceAccountId}/send")
    fun sendMoney(
        @PathVariable sourceAccountId: Long,
        @Valid @RequestBody request: SendMoneyRequest
    ): ResponseEntity<SendMoneyResponse> {
        
        require(sourceAccountId == request.sourceAccountId) {
            "Path variable sourceAccountId must match request body"
        }

        val command = SendMoneyCommand(
            sourceAccountId = AccountId(request.sourceAccountId),
            targetAccountId = AccountId(request.targetAccountId),
            money = Money.of(request.amount)
        )

        val success = sendMoneyUseCase.sendMoney(command)
        
        return if (success) {
            ResponseEntity.ok(SendMoneyResponse(success = true))
        } else {
            ResponseEntity.badRequest()
                .body(SendMoneyResponse(success = false, message = "Transfer failed"))
        }
    }

    @GetMapping("/{accountId}/balance")
    fun getAccountBalance(
        @PathVariable accountId: Long
    ): ResponseEntity<AccountBalanceResponse> {
        
        val query = GetAccountBalanceQuery(AccountId(accountId))
        val balance = getAccountBalanceQuery.getAccountBalance(query)
        
        return ResponseEntity.ok(
            AccountBalanceResponse(
                accountId = accountId,
                balance = balance.amount
            )
        )
    }

    @GetMapping("/{accountId}/activities")
    fun getAccountActivities(
        @PathVariable accountId: Long,
        @RequestParam since: @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime,
        @RequestParam until: @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime
    ): ResponseEntity<List<ActivityResponse>> {
        
        val query = GetAccountActivityQuery(
            accountId = AccountId(accountId),
            since = since,
            until = until
        )
        
        val activities = getAccountActivityQuery.getAccountActivity(query)
        
        val response = activities.map { activity ->
            ActivityResponse(
                id = activity.id?.value,
                timestamp = activity.timestamp,
                sourceAccountId = activity.sourceAccountId.value,
                targetAccountId = activity.targetAccountId.value,
                amount = activity.money.amount
            )
        }
        
        return ResponseEntity.ok(response)
    }
}
```

### 3. 글로벌 예외 처리

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException::class)
    fun handleAccountNotFound(ex: AccountNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(
                code = "ACCOUNT_NOT_FOUND",
                message = "Account not found: ${ex.accountId}",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(ThresholdExceededException::class)
    fun handleThresholdExceeded(ex: ThresholdExceededException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                code = "THRESHOLD_EXCEEDED",
                message = "Transfer amount exceeds threshold: ${ex.threshold}",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                code = "VALIDATION_ERROR",
                message = "Validation failed: ${errors.joinToString(", ")}",
                timestamp = LocalDateTime.now()
            ))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(
                code = "INVALID_REQUEST",
                message = ex.message ?: "Invalid request",
                timestamp = LocalDateTime.now()
            ))
    }
}

data class ErrorResponse(
    val code: String,
    val message: String,
    val timestamp: LocalDateTime
)
```

### 4. 웹 모델 매퍼

```kotlin
@Component
class WebModelMapper {

    fun mapToSendMoneyCommand(
        sourceAccountId: Long,
        request: SendMoneyRequest
    ): SendMoneyCommand {
        return SendMoneyCommand(
            sourceAccountId = AccountId(sourceAccountId),
            targetAccountId = AccountId(request.targetAccountId),
            money = Money.of(request.amount)
        )
    }

    fun mapToActivityResponse(activity: Activity): ActivityResponse {
        return ActivityResponse(
            id = activity.id?.value,
            timestamp = activity.timestamp,
            sourceAccountId = activity.sourceAccountId.value,
            targetAccountId = activity.targetAccountId.value,
            amount = activity.money.amount
        )
    }
}
```

## 챌린지 과제

### 1. API 설계 원칙
- RESTful API 설계 원칙을 어떻게 적용할 것인가?
- 자원 중심 vs 행위 중심 URL 설계의 트레이드오프는?
- API 버전 관리 전략은?

### 2. 입력 검증 전략
- 컨트롤러에서 할 검증 vs 애플리케이션에서 할 검증 구분은?
- Bean Validation을 어떻게 활용할 것인가?
- 커스텀 검증 로직은 어디에 위치시킬 것인가?

### 3. 에러 응답 설계
- 클라이언트가 처리하기 쉬운 에러 형식은?
- 보안을 고려한 에러 메시지 노출 정도는?
- 로깅과 모니터링을 위한 정보는?

### 4. 성능 최적화
- 응답 캐싱 전략은?
- 페이지네이션 구현은?
- 압축 및 최적화 방안은?

## 실습 단계

1. **웹 모델 DTO 클래스 정의**
2. **AccountController 구현**
3. **글로벌 예외 처리기 구현**
4. **입력 검증 로직 추가**
5. **웹 모델 매퍼 구현**
6. **통합 테스트 작성**

## 테스트 시나리오

### API 테스트
- [ ] 송금 API 정상 요청/응답
- [ ] 잔액 조회 API 테스트
- [ ] 거래 내역 조회 API 테스트
- [ ] 잘못된 경로 변수 처리
- [ ] 잘못된 요청 데이터 처리

### 에러 처리 테스트
- [ ] 존재하지 않는 계좌 요청
- [ ] 유효성 검증 실패
- [ ] 비즈니스 규칙 위반
- [ ] 서버 에러 처리

### 통합 테스트
- [ ] MockMvc를 이용한 전체 플로우 테스트
- [ ] JSON 직렬화/역직렬화 검증
- [ ] HTTP 상태 코드 검증

## 완료 체크리스트
- [ ] REST API 컨트롤러 구현 완료
- [ ] 전역 예외 처리 구현
- [ ] 입력 검증 로직 구현
- [ ] 웹 모델 매퍼 구현
- [ ] 포괄적인 웹 계층 테스트 작성
- [ ] API 문서화 (Swagger/OpenAPI)

## 다음 Phase 미리보기
Phase 5에서는 아웃바운드 어댑터를 구현하여 데이터베이스와의 영속성 계층을 구축할 예정입니다.