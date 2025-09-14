# Phase 3: 빈 후처리기와 프록시 생성

## 🎯 학습 목표
- BeanPostProcessor의 역할과 동작 시점 완전 이해
- 스프링이 Phase 1의 프록시 기술을 Phase 2의 AOP와 연결하는 메커니즘 파악
- AbstractAutoProxyCreator와 그 구현체들의 동작 원리 이해
- 직접 BeanPostProcessor를 구현하여 프록시 생성 과정 체험

## 📚 이론 학습 내용

### 1. 빈 생명주기와 BeanPostProcessor
- 스프링 빈의 생명주기 전체 과정
- BeanPostProcessor의 두 가지 메소드:
  - `postProcessBeforeInitialization()`
  - `postProcessAfterInitialization()`
- 빈 생성 → BeanPostProcessor → 프록시 생성의 흐름

### 2. 자동 프록시 생성기들
- **AbstractAutoProxyCreator**: 기본 추상 클래스
- **DefaultAdvisorAutoProxyCreator**: Advisor 기반 프록시 생성
- **BeanNameAutoProxyCreator**: 빈 이름 기반 프록시 생성
- **AspectJAwareAdvisorAutoProxyCreator**: AspectJ와 통합
- **AnnotationAwareAspectJAutoProxyCreator**: @Aspect 지원

### 3. 프록시 생성 결정 로직
- 프록시가 필요한 빈 식별 과정
- Advisor 매칭 로직
- 프록시 팩토리 선택 (JDK vs CGLIB)

## 🛠 실습 과제

### 과제 1: 커스텀 BeanPostProcessor 구현
**파일 위치**: `src/main/kotlin/bong/training/phase3/postprocessors/`

#### 1-1. 기본 BeanPostProcessor
```kotlin
// LoggingBeanPostProcessor.kt
@Component
class LoggingBeanPostProcessor : BeanPostProcessor {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        logger.info("Before initialization: beanName={}, beanClass={}", 
            beanName, bean.javaClass.simpleName)
        return bean
    }
    
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        logger.info("After initialization: beanName={}, beanClass={}, isProxy={}", 
            beanName, bean.javaClass.simpleName, AopUtils.isAopProxy(bean))
        return bean
    }
}
```

#### 1-2. 커스텀 Auto Proxy Creator
```kotlin
// CustomAutoProxyCreator.kt
@Component
class CustomAutoProxyCreator : AbstractAutoProxyCreator() {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    override fun getAdvicesAndAdvisorsForBean(
        beanClass: Class<*>?, 
        beanName: String?, 
        targetSource: TargetSource?
    ): Array<Any>? {
        logger.info("Checking advisors for bean: $beanName, class: ${beanClass?.simpleName}")
        
        // Phase 2에서 만든 Aspect들을 Advisor로 변환하여 반환하거나
        // DO_NOT_PROXY 반환하여 프록시 생성하지 않음
        
        return if (shouldCreateProxy(beanClass, beanName)) {
            getApplicableAdvisors(beanClass, beanName)
        } else {
            DO_NOT_PROXY
        }
    }
    
    private fun shouldCreateProxy(beanClass: Class<*>?, beanName: String?): Boolean {
        // 프록시 생성 조건 정의
        // 예: 특정 패키지의 클래스만, 특정 어노테이션이 있는 클래스만
        return beanClass?.name?.contains("training.phase2.services") == true
    }
    
    private fun getApplicableAdvisors(beanClass: Class<*>?, beanName: String?): Array<Any> {
        // 적용 가능한 Advisor들 수집
        val advisors = mutableListOf<Advisor>()
        
        // 커스텀 로직으로 Advisor 생성
        if (hasLoggingAnnotation(beanClass)) {
            advisors.add(createLoggingAdvisor())
        }
        
        if (hasPerformanceAnnotation(beanClass)) {
            advisors.add(createPerformanceAdvisor())
        }
        
        return advisors.toTypedArray()
    }
    
    private fun hasLoggingAnnotation(beanClass: Class<*>?): Boolean {
        // 클래스나 메소드에 로깅 관련 어노테이션이 있는지 확인
        return beanClass?.methods?.any { 
            it.isAnnotationPresent(LogExecutionTime::class.java) 
        } ?: false
    }
    
    private fun createLoggingAdvisor(): Advisor {
        val pointcut = AnnotationMatchingPointcut(null, LogExecutionTime::class.java)
        val advice = createLoggingAdvice()
        return DefaultPointcutAdvisor(pointcut, advice)
    }
    
    private fun createLoggingAdvice(): MethodInterceptor {
        return MethodInterceptor { invocation ->
            val startTime = System.currentTimeMillis()
            logger.info("Method {} starting", invocation.method.name)
            
            try {
                val result = invocation.proceed()
                val endTime = System.currentTimeMillis()
                logger.info("Method {} completed in {} ms", 
                    invocation.method.name, endTime - startTime)
                result
            } catch (ex: Exception) {
                logger.error("Method {} failed: {}", invocation.method.name, ex.message)
                throw ex
            }
        }
    }
}
```

### 과제 2: 프록시 생성 과정 관찰
**파일 위치**: `src/main/kotlin/bong/training/phase3/observers/`

#### 2-1. 프록시 생성 추적기
```kotlin
// ProxyCreationTracker.kt
@Component
class ProxyCreationTracker : BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {
    private val proxyCreationHistory = mutableListOf<ProxyCreationRecord>()
    private val logger = LoggerFactory.getLogger(javaClass)
    
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        val isProxy = AopUtils.isAopProxy(bean)
        val proxyType = when {
            AopUtils.isJdkDynamicProxy(bean) -> ProxyType.JDK_DYNAMIC
            AopUtils.isCglibProxy(bean) -> ProxyType.CGLIB
            else -> ProxyType.NO_PROXY
        }
        
        if (isProxy) {
            val targetClass = AopUtils.getTargetClass(bean)
            val advisors = getAdvisors(bean)
            
            val record = ProxyCreationRecord(
                beanName = beanName,
                targetClass = targetClass,
                proxyClass = bean.javaClass,
                proxyType = proxyType,
                advisorCount = advisors.size,
                advisorNames = advisors.map { it.javaClass.simpleName },
                creationTime = System.currentTimeMillis()
            )
            
            proxyCreationHistory.add(record)
            logger.info("Proxy created: {}", record)
        }
        
        return bean
    }
    
    private fun getAdvisors(proxy: Any): List<Advisor> {
        return if (proxy is Advised) {
            proxy.advisors.toList()
        } else {
            emptyList()
        }
    }
    
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        logger.info("=== Proxy Creation Summary ===")
        proxyCreationHistory.forEach { record ->
            logger.info("Bean: {}, ProxyType: {}, Advisors: {}", 
                record.beanName, record.proxyType, record.advisorNames.joinToString())
        }
        logger.info("Total proxies created: {}", proxyCreationHistory.size)
    }
    
    fun getProxyCreationHistory(): List<ProxyCreationRecord> = proxyCreationHistory.toList()
}

data class ProxyCreationRecord(
    val beanName: String,
    val targetClass: Class<*>,
    val proxyClass: Class<*>,
    val proxyType: ProxyType,
    val advisorCount: Int,
    val advisorNames: List<String>,
    val creationTime: Long
)

enum class ProxyType { JDK_DYNAMIC, CGLIB, NO_PROXY }
```

#### 2-2. 빈 생명주기 관찰자
```kotlin
// BeanLifecycleObserver.kt
@Component
class BeanLifecycleObserver : 
    BeanPostProcessor,
    InitializingBean,
    DisposableBean,
    ApplicationContextAware {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var applicationContext: ApplicationContext
    private val lifecycleEvents = mutableListOf<LifecycleEvent>()
    
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
    
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        addLifecycleEvent(beanName, bean.javaClass, LifecyclePhase.BEFORE_INITIALIZATION)
        return bean
    }
    
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        addLifecycleEvent(beanName, bean.javaClass, LifecyclePhase.AFTER_INITIALIZATION)
        
        // 프록시 생성 여부 확인
        if (AopUtils.isAopProxy(bean)) {
            addLifecycleEvent(beanName, bean.javaClass, LifecyclePhase.PROXY_CREATED)
        }
        
        return bean
    }
    
    private fun addLifecycleEvent(beanName: String, beanClass: Class<*>, phase: LifecyclePhase) {
        val event = LifecycleEvent(
            beanName = beanName,
            beanClass = beanClass.simpleName,
            phase = phase,
            timestamp = System.currentTimeMillis()
        )
        lifecycleEvents.add(event)
        
        if (beanName.contains("Service")) { // 서비스 빈만 로깅
            logger.info("Lifecycle Event: {}", event)
        }
    }
    
    fun getLifecycleEvents(beanName: String): List<LifecycleEvent> {
        return lifecycleEvents.filter { it.beanName == beanName }
    }
    
    fun getAllLifecycleEvents(): List<LifecycleEvent> = lifecycleEvents.toList()
}

data class LifecycleEvent(
    val beanName: String,
    val beanClass: String,
    val phase: LifecyclePhase,
    val timestamp: Long
)

enum class LifecyclePhase {
    BEFORE_INITIALIZATION,
    AFTER_INITIALIZATION, 
    PROXY_CREATED
}
```

### 과제 3: 고급 프록시 생성 제어
**파일 위치**: `src/main/kotlin/bong/training/phase3/advanced/`

#### 3-1. 조건부 프록시 생성기
```kotlin
// ConditionalProxyCreator.kt
@Component
@ConditionalOnProperty(name = "app.proxy.conditional.enabled", havingValue = "true")
class ConditionalProxyCreator : AbstractAutoProxyCreator() {
    
    @Value("\${app.proxy.target-packages:bong.training.phase2.services}")
    private lateinit var targetPackages: String
    
    @Value("\${app.proxy.exclude-beans:}")
    private lateinit var excludeBeans: String
    
    override fun getAdvicesAndAdvisorsForBean(
        beanClass: Class<*>?, 
        beanName: String?, 
        targetSource: TargetSource?
    ): Array<Any>? {
        
        // 제외 대상 확인
        if (isExcludedBean(beanName)) {
            return DO_NOT_PROXY
        }
        
        // 대상 패키지 확인
        if (!isTargetPackage(beanClass)) {
            return DO_NOT_PROXY
        }
        
        // 런타임 조건 확인
        if (!shouldCreateProxyAtRuntime(beanClass, beanName)) {
            return DO_NOT_PROXY
        }
        
        return collectApplicableAdvisors(beanClass, beanName)
    }
    
    private fun isExcludedBean(beanName: String?): Boolean {
        return excludeBeans.split(",")
            .map { it.trim() }
            .any { it == beanName }
    }
    
    private fun isTargetPackage(beanClass: Class<*>?): Boolean {
        val packageName = beanClass?.`package`?.name ?: return false
        return targetPackages.split(",")
            .map { it.trim() }
            .any { packageName.startsWith(it) }
    }
    
    private fun shouldCreateProxyAtRuntime(beanClass: Class<*>?, beanName: String?): Boolean {
        // 런타임 조건 (예: 시간, 프로파일, 특정 조건)
        val currentHour = LocalTime.now().hour
        
        // 예: 오전 시간대에만 프록시 생성 (테스트용)
        return currentHour < 12 || System.getProperty("force.proxy") == "true"
    }
}
```

#### 3-2. 메트릭 수집용 BeanPostProcessor
```kotlin
// MetricsCollectingBeanPostProcessor.kt
@Component
class MetricsCollectingBeanPostProcessor : BeanPostProcessor {
    private val registry = mutableMapOf<String, BeanMetrics>()
    private val logger = LoggerFactory.getLogger(javaClass)
    
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        val startTime = System.currentTimeMillis()
        registry[beanName] = BeanMetrics(
            beanName = beanName,
            beanClass = bean.javaClass,
            initStartTime = startTime
        )
        return bean
    }
    
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        val metrics = registry[beanName] ?: return bean
        val endTime = System.currentTimeMillis()
        
        val updatedMetrics = metrics.copy(
            initEndTime = endTime,
            initializationTime = endTime - metrics.initStartTime,
            isProxy = AopUtils.isAopProxy(bean),
            proxyType = getProxyType(bean),
            advisorCount = if (bean is Advised) bean.advisors.size else 0
        )
        
        registry[beanName] = updatedMetrics
        
        if (updatedMetrics.isProxy) {
            logger.info("Proxy metrics: {}", updatedMetrics)
        }
        
        return bean
    }
    
    private fun getProxyType(bean: Any): String {
        return when {
            AopUtils.isJdkDynamicProxy(bean) -> "JDK_DYNAMIC"
            AopUtils.isCglibProxy(bean) -> "CGLIB"
            else -> "NO_PROXY"
        }
    }
    
    fun getBeanMetrics(beanName: String): BeanMetrics? = registry[beanName]
    fun getAllMetrics(): Map<String, BeanMetrics> = registry.toMap()
    
    fun getProxyMetrics(): List<BeanMetrics> {
        return registry.values.filter { it.isProxy }
    }
}

data class BeanMetrics(
    val beanName: String,
    val beanClass: Class<*>,
    val initStartTime: Long,
    val initEndTime: Long = 0,
    val initializationTime: Long = 0,
    val isProxy: Boolean = false,
    val proxyType: String = "NO_PROXY",
    val advisorCount: Int = 0
)
```

### 과제 4: 설정 클래스
**파일 위치**: `src/main/kotlin/bong/training/phase3/config/`

```kotlin
// BeanPostProcessorConfig.kt
@Configuration
class BeanPostProcessorConfig {
    
    @Bean
    @ConditionalOnProperty(name = "app.postprocessor.logging.enabled", havingValue = "true")
    fun loggingBeanPostProcessor(): LoggingBeanPostProcessor {
        return LoggingBeanPostProcessor()
    }
    
    @Bean
    @ConditionalOnProperty(name = "app.postprocessor.tracking.enabled", havingValue = "true")
    fun proxyCreationTracker(): ProxyCreationTracker {
        return ProxyCreationTracker()
    }
    
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) // 가장 먼저 실행
    fun beanLifecycleObserver(): BeanLifecycleObserver {
        return BeanLifecycleObserver()
    }
    
    @Bean
    @ConditionalOnProperty(name = "app.postprocessor.metrics.enabled", havingValue = "true", matchIfMissing = true)
    fun metricsCollectingBeanPostProcessor(): MetricsCollectingBeanPostProcessor {
        return MetricsCollectingBeanPostProcessor()
    }
}
```

## 🧪 테스트 계획

### 테스트 파일 위치
- `src/test/kotlin/bong/training/phase3/`

### 테스트 시나리오

#### 1. BeanPostProcessor 동작 테스트
```kotlin
// BeanPostProcessorTest.kt
@SpringBootTest
class BeanPostProcessorTest {
    @Autowired
    lateinit var proxyCreationTracker: ProxyCreationTracker
    
    @Autowired
    lateinit var beanLifecycleObserver: BeanLifecycleObserver
    
    @Autowired
    lateinit var metricsCollector: MetricsCollectingBeanPostProcessor
    
    @Test
    fun `BeanPostProcessor 실행 순서 확인`() {
        // Phase 2의 서비스 빈들이 생성될 때 BeanPostProcessor들이 올바른 순서로 실행되는지 확인
    }
    
    @Test
    fun `프록시 생성 과정 추적 확인`() {
        val history = proxyCreationTracker.getProxyCreationHistory()
        assertThat(history).isNotEmpty
        
        // UserService, ProductService, OrderService가 프록시로 생성되었는지 확인
        val serviceProxies = history.filter { it.beanName.contains("Service") }
        assertThat(serviceProxies).hasSizeGreaterThan(0)
    }
    
    @Test
    fun `빈 생명주기 이벤트 순서 확인`() {
        val userServiceEvents = beanLifecycleObserver.getLifecycleEvents("userService")
        
        // BEFORE_INITIALIZATION → AFTER_INITIALIZATION → PROXY_CREATED 순서 확인
        assertThat(userServiceEvents).hasSize(3)
        assertThat(userServiceEvents[0].phase).isEqualTo(LifecyclePhase.BEFORE_INITIALIZATION)
        assertThat(userServiceEvents[1].phase).isEqualTo(LifecyclePhase.AFTER_INITIALIZATION)
        assertThat(userServiceEvents[2].phase).isEqualTo(LifecyclePhase.PROXY_CREATED)
    }
}
```

#### 2. 커스텀 Auto Proxy Creator 테스트
```kotlin
// CustomAutoProxyCreatorTest.kt
@SpringBootTest
@TestPropertySource(properties = ["app.proxy.conditional.enabled=true"])
class CustomAutoProxyCreatorTest {
    
    @Autowired
    lateinit var userService: UserService
    
    @Test
    fun `커스텀 프록시 생성기로 생성된 프록시 확인`() {
        assertThat(AopUtils.isAopProxy(userService)).isTrue
        
        // 커스텀 로직으로 생성된 어드바이저 확인
        if (userService is Advised) {
            val advisors = userService.advisors
            assertThat(advisors).isNotEmpty
        }
    }
    
    @Test
    fun `조건부 프록시 생성 로직 테스트`() {
        // System property 설정으로 프록시 생성 강제
        System.setProperty("force.proxy", "true")
        
        // 새로운 컨텍스트로 테스트 실행하여 프록시 생성 확인
    }
}
```

#### 3. 프록시 vs 일반 빈 성능 비교
```kotlin
// ProxyPerformanceTest.kt
@SpringBootTest
class ProxyPerformanceTest {
    
    @Test
    fun `프록시 빈과 일반 빈 생성 시간 비교`() {
        val metrics = metricsCollector.getAllMetrics()
        
        val proxyBeans = metrics.filter { it.value.isProxy }
        val normalBeans = metrics.filter { !it.value.isProxy }
        
        val avgProxyTime = proxyBeans.values.map { it.initializationTime }.average()
        val avgNormalTime = normalBeans.values.map { it.initializationTime }.average()
        
        println("Average proxy bean initialization time: ${avgProxyTime}ms")
        println("Average normal bean initialization time: ${avgNormalTime}ms")
        
        // 프록시 생성으로 인한 오버헤드 분석
    }
}
```

## 📊 학습 결과 분석

### 분석 항목
1. **프록시 생성 결정 과정**
   - 어떤 기준으로 프록시를 생성하는지
   - AbstractAutoProxyCreator의 핵심 로직

2. **실행 순서 분석**  
   - 여러 BeanPostProcessor의 실행 순서
   - @Order 어노테이션의 영향

3. **성능 영향도**
   - 프록시 생성으로 인한 애플리케이션 시작 시간 증가
   - 메모리 사용량 차이

4. **스프링 AOP vs 커스텀 구현**
   - 스프링이 제공하는 기본 구현체의 장점
   - 커스텀 구현 시 고려사항

## 🎓 학습 완료 기준

### 체크리스트
- [ ] BeanPostProcessor 구현 완료
- [ ] 커스텀 Auto Proxy Creator 구현 완료
- [ ] 프록시 생성 과정 추적 시스템 완료
- [ ] 빈 생명주기 전체 흐름 이해
- [ ] Phase 1의 프록시와 Phase 2의 AOP 연결고리 파악
- [ ] 실제 스프링의 구현체와 비교 분석

### 다음 단계 준비
- ProxyFactory의 세부 설정 방법
- AspectJ와의 통합 방식
- 고급 프록시 설정 및 최적화 기법

---
*예상 소요 시간: 5-6일*
*난이도: ⭐⭐⭐⭐☆*