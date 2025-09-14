# Phase 2: 스프링 AOP 핵심 개념

## 🎯 학습 목표
- 스프링 AOP의 핵심 개념 완전 이해
- @Aspect 기반 AOP 구현 능력 습득
- 다양한 Advice 타입과 Pointcut 표현식 활용
- 스프링이 Phase 1의 프록시를 어떻게 활용하는지 이해

## 📚 이론 학습 내용

### 1. AOP 핵심 개념
- **Aspect**: 관심사를 모듈화한 것
- **JoinPoint**: 메소드 실행 지점 등 어드바이스가 적용될 수 있는 위치
- **Pointcut**: JoinPoint를 선별하는 기준
- **Advice**: JoinPoint에서 실행되는 코드
- **Target**: 어드바이스를 받을 객체
- **Advisor**: Pointcut + Advice

### 2. Advice 타입별 특징
- **@Before**: 메소드 실행 전
- **@After**: 메소드 실행 후 (예외 발생과 무관)
- **@AfterReturning**: 메소드 정상 완료 후
- **@AfterThrowing**: 예외 발생 후
- **@Around**: 메소드 실행 전후 (가장 강력한 형태)

### 3. Pointcut 표현식
- execution(): 메소드 실행 조인 포인트
- within(): 특정 타입 내부
- args(): 특정 인수 타입
- @annotation(): 특정 어노테이션이 있는 메소드
- bean(): 스프링 빈 이름으로 매칭

## 🛠 실습 과제

### 과제 1: 기본 비즈니스 로직 구현
**파일 위치**: `src/main/kotlin/bong/training/phase2/services/`

#### 1-1. 도메인 객체
```kotlin
// User.kt
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

// Product.kt  
data class Product(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val category: String
)

// Order.kt
data class Order(
    val id: String = UUID.randomUUID().toString(),
    val userId: Long,
    val products: List<Product>,
    val totalAmount: BigDecimal,
    val status: OrderStatus = OrderStatus.PENDING
)

enum class OrderStatus { PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED }
```

#### 1-2. 서비스 클래스들
```kotlin
// UserService.kt
@Service
class UserService {
    private val users = mutableMapOf<Long, User>()
    
    @LogExecutionTime  // 커스텀 어노테이션
    fun createUser(name: String, email: String): User {
        val user = User(System.currentTimeMillis(), name, email)
        users[user.id] = user
        return user
    }
    
    @ValidateParams  // 커스텀 어노테이션
    fun findUser(id: Long): User {
        return users[id] ?: throw UserNotFoundException("User not found: $id")
    }
    
    @AuditAction("DELETE_USER")  // 커스텀 어노테이션
    fun deleteUser(id: Long): Boolean {
        return users.remove(id) != null
    }
}

// ProductService.kt
@Service  
class ProductService {
    private val products = mutableMapOf<Long, Product>()
    
    @CacheResult  // 커스텀 어노테이션
    fun findProduct(id: Long): Product {
        return products[id] ?: throw ProductNotFoundException("Product not found: $id")
    }
    
    @ValidateParams
    fun addProduct(name: String, price: BigDecimal, category: String): Product {
        val product = Product(System.currentTimeMillis(), name, price, category)
        products[product.id] = product
        return product
    }
}

// OrderService.kt
@Service
class OrderService(
    private val userService: UserService,
    private val productService: ProductService
) {
    private val orders = mutableMapOf<String, Order>()
    
    @Transactional  // 스프링 기본 어노테이션
    @LogExecutionTime
    @AuditAction("CREATE_ORDER")
    fun createOrder(userId: Long, productIds: List<Long>): Order {
        val user = userService.findUser(userId)
        val products = productIds.map { productService.findProduct(it) }
        val totalAmount = products.sumOf { it.price }
        
        val order = Order(userId = userId, products = products, totalAmount = totalAmount)
        orders[order.id] = order
        return order
    }
    
    @ValidateParams
    fun cancelOrder(orderId: String): Boolean {
        val order = orders[orderId] ?: return false
        orders[orderId] = order.copy(status = OrderStatus.CANCELLED)
        return true
    }
}
```

### 과제 2: 커스텀 어노테이션 정의
**파일 위치**: `src/main/kotlin/bong/training/phase2/annotations/`

```kotlin
// LogExecutionTime.kt
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class LogExecutionTime

// ValidateParams.kt
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidateParams

// AuditAction.kt
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AuditAction(val value: String)

// CacheResult.kt
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheResult(val ttlMinutes: Int = 5)
```

### 과제 3: Aspect 구현
**파일 위치**: `src/main/kotlin/bong/training/phase2/aspects/`

#### 3-1. 로깅 Aspect
```kotlin
// LoggingAspect.kt
@Aspect
@Component
class LoggingAspect {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    // 모든 서비스 메소드 실행 로그
    @Before("execution(* bong.training.phase2.services.*.*(..))")
    fun logMethodEntry(joinPoint: JoinPoint) {
        // 메소드 이름, 파라미터 로깅
    }
    
    @AfterReturning(
        pointcut = "execution(* bong.training.phase2.services.*.*(..))",
        returning = "result"
    )
    fun logMethodExit(joinPoint: JoinPoint, result: Any?) {
        // 메소드 실행 완료 및 반환값 로깅
    }
    
    @AfterThrowing(
        pointcut = "execution(* bong.training.phase2.services.*.*(..))",
        throwing = "exception"
    )
    fun logException(joinPoint: JoinPoint, exception: Throwable) {
        // 예외 발생 로깅
    }
}
```

#### 3-2. 실행 시간 측정 Aspect
```kotlin
// PerformanceAspect.kt
@Aspect
@Component
class PerformanceAspect {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    @Around("@annotation(bong.training.phase2.annotations.LogExecutionTime)")
    fun measureExecutionTime(proceedingJoinPoint: ProceedingJoinPoint): Any? {
        val startTime = System.currentTimeMillis()
        
        return try {
            val result = proceedingJoinPoint.proceed()
            val endTime = System.currentTimeMillis()
            logger.info("Method {} executed in {} ms", 
                proceedingJoinPoint.signature.name, 
                endTime - startTime)
            result
        } catch (ex: Exception) {
            val endTime = System.currentTimeMillis()
            logger.error("Method {} failed after {} ms: {}", 
                proceedingJoinPoint.signature.name, 
                endTime - startTime, 
                ex.message)
            throw ex
        }
    }
}
```

#### 3-3. 파라미터 검증 Aspect
```kotlin
// ValidationAspect.kt
@Aspect
@Component  
class ValidationAspect {
    @Before("@annotation(bong.training.phase2.annotations.ValidateParams)")
    fun validateParameters(joinPoint: JoinPoint) {
        val args = joinPoint.args
        args.forEach { arg ->
            when (arg) {
                null -> throw IllegalArgumentException("Parameter cannot be null")
                is String -> if (arg.isBlank()) throw IllegalArgumentException("String parameter cannot be blank")
                is Number -> if (arg.toDouble() < 0) throw IllegalArgumentException("Number parameter cannot be negative")
            }
        }
    }
}
```

#### 3-4. 감사(Audit) Aspect
```kotlin
// AuditAspect.kt
@Aspect
@Component
class AuditAspect {
    private val auditLog = mutableListOf<AuditRecord>()
    
    @Around("@annotation(auditAction)")
    fun audit(proceedingJoinPoint: ProceedingJoinPoint, auditAction: AuditAction): Any? {
        val startTime = System.currentTimeMillis()
        val methodName = proceedingJoinPoint.signature.name
        val args = proceedingJoinPoint.args
        
        return try {
            val result = proceedingJoinPoint.proceed()
            
            val record = AuditRecord(
                action = auditAction.value,
                method = methodName,
                parameters = args.contentToString(),
                success = true,
                timestamp = LocalDateTime.now(),
                executionTimeMs = System.currentTimeMillis() - startTime
            )
            auditLog.add(record)
            
            result
        } catch (ex: Exception) {
            val record = AuditRecord(
                action = auditAction.value,
                method = methodName,
                parameters = args.contentToString(),
                success = false,
                errorMessage = ex.message,
                timestamp = LocalDateTime.now(),
                executionTimeMs = System.currentTimeMillis() - startTime
            )
            auditLog.add(record)
            throw ex
        }
    }
    
    fun getAuditRecords(): List<AuditRecord> = auditLog.toList()
}

data class AuditRecord(
    val action: String,
    val method: String,
    val parameters: String,
    val success: Boolean,
    val errorMessage: String? = null,
    val timestamp: LocalDateTime,
    val executionTimeMs: Long
)
```

### 과제 4: 설정 클래스
**파일 위치**: `src/main/kotlin/bong/training/phase2/config/`

```kotlin
// AopConfig.kt
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)  // CGLIB 강제 사용
class AopConfig {
    
    @Bean
    @ConditionalOnProperty(name = "app.aop.logging.enabled", havingValue = "true", matchIfMissing = true)
    fun loggingAspect(): LoggingAspect {
        return LoggingAspect()
    }
    
    @Bean  
    @ConditionalOnProperty(name = "app.aop.performance.enabled", havingValue = "true", matchIfMissing = true)
    fun performanceAspect(): PerformanceAspect {
        return PerformanceAspect()
    }
}
```

## 🧪 테스트 계획

### 테스트 파일 위치
- `src/test/kotlin/bong/training/phase2/`

### 테스트 시나리오

#### 1. 서비스 로직 테스트
```kotlin
// UserServiceTest.kt
@SpringBootTest
class UserServiceTest {
    @Autowired
    lateinit var userService: UserService
    
    @Test
    fun `사용자 생성 시 로깅 Aspect 동작 확인`()
    
    @Test  
    fun `잘못된 파라미터로 사용자 조회 시 검증 Aspect 동작 확인`()
    
    @Test
    fun `사용자 삭제 시 감사 Aspect 동작 확인`()
}
```

#### 2. Aspect 동작 테스트
```kotlin
// AspectIntegrationTest.kt
@SpringBootTest
class AspectIntegrationTest {
    @Autowired
    lateinit var orderService: OrderService
    
    @Autowired
    lateinit var auditAspect: AuditAspect
    
    @Test
    fun `주문 생성 시 모든 Aspect 동작 확인`() {
        // @Transactional, @LogExecutionTime, @AuditAction 모두 적용
        // 실행 전후 상태 확인
        // 감사 로그 생성 확인
        // 성능 로그 생성 확인
    }
    
    @Test
    fun `예외 발생 시 Aspect 동작 확인`()
    
    @Test  
    fun `Around Advice의 proceed() 전후 동작 확인`()
}
```

#### 3. 프록시 객체 확인 테스트
```kotlin
// ProxyAnalysisTest.kt
@SpringBootTest
class ProxyAnalysisTest {
    @Autowired
    lateinit var userService: UserService
    
    @Test
    fun `서비스 객체가 프록시인지 확인`() {
        // AopUtils.isAopProxy() 사용
        // 프록시 타입 확인 (JDK vs CGLIB)
    }
    
    @Test
    fun `프록시 대상 객체 접근`() {
        // AopTestUtils.getTargetObject() 사용
    }
}
```

## 📊 학습 결과 분석

### 분석 항목
1. **Aspect 실행 순서**
   - 여러 Aspect가 적용될 때의 실행 순서
   - @Order 어노테이션을 통한 순서 제어

2. **프록시 타입 분석**
   - 인터페이스가 있을 때와 없을 때
   - proxyTargetClass 설정의 영향

3. **성능 영향도**
   - Aspect 적용 전후 성능 비교
   - Around vs 다른 Advice 타입의 성능 차이

4. **실제 프록시 객체 구조**
   - 디버거로 프록시 객체 내부 구조 관찰
   - Phase 1에서 만든 프록시와의 연결점 확인

## 🎓 학습 완료 기준

### 체크리스트
- [ ] 모든 Advice 타입 구현 완료
- [ ] 다양한 Pointcut 표현식 활용
- [ ] 커스텀 어노테이션 기반 AOP 구현
- [ ] 복합 Aspect 동작 확인
- [ ] 프록시 객체 구조 이해
- [ ] 실행 순서 및 성능 분석 완료

### 다음 단계 연결
- 스프링이 어떻게 이러한 Aspect를 프록시로 변환하는지
- 빈 후처리기가 언제, 어떻게 개입하는지
- ProxyFactory의 역할과 설정 방법

---
*예상 소요 시간: 4-5일*
*난이도: ⭐⭐⭐☆☆*