# Phase 4: ProxyFactory와 고급 설정

## 🎯 학습 목표
- ProxyFactory를 직접 사용하여 프로그래매틱 프록시 생성 이해
- ProxyFactoryBean을 통한 XML/JavaConfig 기반 프록시 설정
- 고급 프록시 옵션들의 실제 동작과 성능 영향 분석
- 스프링 AOP의 최적화 기법과 실제 운영 환경에서의 고려사항

## 📚 이론 학습 내용

### 1. ProxyFactory 아키텍처
- **ProxyFactory**: 프로그래매틱 프록시 생성의 핵심
- **AdvisedSupport**: ProxyFactory의 부모 클래스, 설정 관리
- **AopProxyFactory**: JDK vs CGLIB 선택 로직
- **ProxyCreatorSupport**: 프록시 생성 지원 기능

### 2. 고급 프록시 설정 옵션
- **proxyTargetClass**: CGLIB 강제 사용
- **optimize**: 프록시 최적화 옵션
- **frozen**: 설정 변경 금지
- **exposeProxy**: 현재 프록시 노출 (AopContext)
- **preFiltered**: 사전 필터링된 어드바이저

### 3. TargetSource의 다양한 구현
- **SingletonTargetSource**: 기본 구현 (싱글톤)
- **PrototypeTargetSource**: 매번 새 인스턴스
- **ThreadLocalTargetSource**: 스레드별 인스턴스
- **PoolingTargetSource**: 객체 풀링
- **HotSwappableTargetSource**: 런타임 대상 교체

## 🛠 실습 과제

### 과제 1: 프로그래매틱 ProxyFactory 활용
**파일 위치**: `src/main/kotlin/bong/training/phase4/factory/`

#### 1-1. 기본 ProxyFactory 사용
```kotlin
// ProgrammaticProxyFactory.kt
@Component
class ProgrammaticProxyFactory {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    fun <T> createBasicProxy(target: T, targetClass: Class<T>): T {
        val proxyFactory = ProxyFactory(target)
        
        // 기본 어드바이저 추가
        proxyFactory.addAdvisor(createLoggingAdvisor())
        proxyFactory.addAdvisor(createPerformanceAdvisor())
        
        return proxyFactory.proxy as T
    }
    
    fun <T> createOptimizedProxy(target: T, targetClass: Class<T>): T {
        val proxyFactory = ProxyFactory(target)
        
        // 최적화 옵션 설정
        proxyFactory.isOptimize = true  // 성능 최적화
        proxyFactory.isFrozen = true    // 설정 변경 금지
        proxyFactory.isProxyTargetClass = true  // CGLIB 강제 사용
        
        // 선택적 어드바이저 추가
        proxyFactory.addAdvisor(createConditionalAdvisor())
        
        return proxyFactory.proxy as T
    }
    
    fun <T> createExposeProxyEnabled(target: T, targetClass: Class<T>): T {
        val proxyFactory = ProxyFactory(target)
        proxyFactory.isExposeProxy = true  // AopContext.currentProxy() 사용 가능
        
        proxyFactory.addAdvisor(createSelfInvocationAdvisor())
        
        return proxyFactory.proxy as T
    }
    
    private fun createLoggingAdvisor(): Advisor {
        val pointcut = Pointcut.TRUE  // 모든 메소드 대상
        val advice = MethodInterceptor { invocation ->
            logger.info("Method {} called with args: {}", 
                invocation.method.name, invocation.arguments.contentToString())
            invocation.proceed()
        }
        return DefaultPointcutAdvisor(pointcut, advice)
    }
    
    private fun createPerformanceAdvisor(): Advisor {
        val advice = MethodInterceptor { invocation ->
            val startTime = System.nanoTime()
            try {
                invocation.proceed()
            } finally {
                val endTime = System.nanoTime()
                val duration = (endTime - startTime) / 1_000_000.0
                logger.info("Method {} executed in {:.2f}ms", invocation.method.name, duration)
            }
        }
        return DefaultPointcutAdvisor(advice)
    }
    
    private fun createConditionalAdvisor(): Advisor {
        val pointcut = object : StaticMethodMatcherPointcut() {
            override fun matches(method: Method, targetClass: Class<*>): Boolean {
                // 특정 조건에서만 어드바이스 적용
                return method.name.startsWith("save") || method.name.startsWith("delete")
            }
        }
        
        val advice = MethodInterceptor { invocation ->
            logger.warn("Dangerous operation: {}", invocation.method.name)
            invocation.proceed()
        }
        
        return DefaultPointcutAdvisor(pointcut, advice)
    }
    
    private fun createSelfInvocationAdvisor(): Advisor {
        val advice = MethodInterceptor { invocation ->
            val currentProxy = AopContext.currentProxy()
            logger.info("Current proxy class: {}, method: {}", 
                currentProxy.javaClass.simpleName, invocation.method.name)
            invocation.proceed()
        }
        return DefaultPointcutAdvisor(advice)
    }
}
```

#### 1-2. TargetSource 활용
```kotlin
// TargetSourceFactory.kt
@Component
class TargetSourceFactory {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    fun createPrototypeTargetSource(targetClass: Class<*>): TargetSource {
        return object : AbstractPrototypeBasedTargetSource() {
            override fun newPrototypeInstance(): Any {
                logger.info("Creating new prototype instance of {}", targetClass.simpleName)
                // 실제로는 ApplicationContext에서 프로토타입 빈을 가져와야 함
                return targetClass.getDeclaredConstructor().newInstance()
            }
            
            override fun getTargetClass(): Class<*> = targetClass
        }
    }
    
    fun createThreadLocalTargetSource(initialTarget: Any): TargetSource {
        return ThreadLocalTargetSource().apply {
            setTargetClass(initialTarget.javaClass)
            setTarget(initialTarget)
        }
    }
    
    fun createHotSwappableTargetSource(initialTarget: Any): HotSwappableTargetSource {
        return HotSwappableTargetSource(initialTarget)
    }
    
    fun createPoolingTargetSource(targetClass: Class<*>, maxSize: Int): TargetSource {
        return object : AbstractPoolingTargetSource() {
            private val pool = LinkedList<Any>()
            
            override fun getTarget(): Any {
                return synchronized(pool) {
                    if (pool.isNotEmpty()) {
                        pool.removeFirst()
                    } else {
                        createNewInstance()
                    }
                }
            }
            
            override fun releaseTarget(target: Any) {
                synchronized(pool) {
                    if (pool.size < maxSize) {
                        pool.add(target)
                    }
                }
            }
            
            override fun getTargetClass(): Class<*> = targetClass
            override fun getPoolingConfigMixin(): Any = this
            
            private fun createNewInstance(): Any {
                logger.info("Creating new pooled instance of {}", targetClass.simpleName)
                return targetClass.getDeclaredConstructor().newInstance()
            }
        }
    }
}
```

### 과제 2: ProxyFactoryBean 설정
**파일 위치**: `src/main/kotlin/bong/training/phase4/config/`

#### 2-1. Java Config 기반 설정
```kotlin
// ProxyFactoryBeanConfig.kt
@Configuration
class ProxyFactoryBeanConfig {
    
    @Bean
    fun userServiceTarget(): UserServiceImpl {
        return UserServiceImpl()
    }
    
    @Bean
    fun userServiceProxy(userServiceTarget: UserServiceImpl): ProxyFactoryBean {
        val factory = ProxyFactoryBean()
        
        // 대상 객체 설정
        factory.setTarget(userServiceTarget)
        
        // 인터페이스 설정 (JDK 프록시용)
        factory.setInterfaces(UserService::class.java)
        
        // 어드바이저 설정
        factory.setInterceptorNames("loggingAdvisor", "performanceAdvisor", "validationAdvisor")
        
        // 프록시 설정
        factory.setProxyTargetClass(false)  // JDK 프록시 사용
        factory.setOptimize(true)
        factory.setExposeProxy(true)
        
        return factory
    }
    
    @Bean
    fun orderServiceProxy(): ProxyFactoryBean {
        val factory = ProxyFactoryBean()
        
        // 대상 클래스만 설정 (CGLIB 프록시)
        factory.setTargetClass(OrderService::class.java)
        factory.setProxyTargetClass(true)
        
        // TargetSource 설정
        factory.targetSource = createCustomTargetSource()
        
        // 어드바이저 설정
        factory.addAdvisor(createTransactionAdvisor())
        factory.addAdvisor(createCachingAdvisor())
        
        return factory
    }
    
    private fun createCustomTargetSource(): TargetSource {
        // 커스텀 TargetSource 구현
        return object : TargetSource {
            private val target = OrderService()
            
            override fun getTargetClass(): Class<*>? = OrderService::class.java
            override fun isStatic(): Boolean = true
            override fun getTarget(): Any = target
            override fun releaseTarget(target: Any) { /* no-op */ }
        }
    }
    
    @Bean
    fun loggingAdvisor(): Advisor {
        val advice = MethodInterceptor { invocation ->
            println("=== ProxyFactoryBean Logging ===")
            println("Method: ${invocation.method.name}")
            println("Args: ${invocation.arguments.contentToString()}")
            val result = invocation.proceed()
            println("Result: $result")
            result
        }
        return DefaultPointcutAdvisor(advice)
    }
    
    @Bean
    fun performanceAdvisor(): Advisor {
        val advice = MethodInterceptor { invocation ->
            val start = System.currentTimeMillis()
            try {
                invocation.proceed()
            } finally {
                val end = System.currentTimeMillis()
                println("=== Performance: ${invocation.method.name} took ${end - start}ms ===")
            }
        }
        return DefaultPointcutAdvisor(advice)
    }
    
    @Bean
    fun validationAdvisor(): Advisor {
        val pointcut = object : StaticMethodMatcherPointcut() {
            override fun matches(method: Method, targetClass: Class<*>): Boolean {
                return method.parameterCount > 0
            }
        }
        
        val advice = MethodBeforeAdvice { method, args, target ->
            args.forEach { arg ->
                if (arg == null) {
                    throw IllegalArgumentException("Null argument not allowed for ${method.name}")
                }
            }
        }
        
        return DefaultPointcutAdvisor(pointcut, advice)
    }
    
    private fun createTransactionAdvisor(): Advisor {
        val advice = MethodInterceptor { invocation ->
            println("=== Transaction START for ${invocation.method.name} ===")
            try {
                val result = invocation.proceed()
                println("=== Transaction COMMIT ===")
                result
            } catch (ex: Exception) {
                println("=== Transaction ROLLBACK: ${ex.message} ===")
                throw ex
            }
        }
        return DefaultPointcutAdvisor(advice)
    }
    
    private fun createCachingAdvisor(): Advisor {
        val cache = mutableMapOf<String, Any>()
        
        val advice = MethodInterceptor { invocation ->
            val cacheKey = "${invocation.method.name}:${invocation.arguments.contentHashCode()}"
            
            cache[cacheKey] ?: run {
                val result = invocation.proceed()
                cache[cacheKey] = result ?: "null"
                result
            }
        }
        
        return DefaultPointcutAdvisor(advice)
    }
}
```

### 과제 3: 고급 프록시 최적화
**파일 위치**: `src/main/kotlin/bong/training/phase4/advanced/`

#### 3-1. 프록시 성능 최적화
```kotlin
// ProxyOptimizer.kt
@Component
class ProxyOptimizer {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    fun createOptimizedProxy(target: Any): Any {
        val proxyFactory = ProxyFactory(target)
        
        // 최적화 설정
        applyOptimizationSettings(proxyFactory)
        
        // 필요한 어드바이저만 추가
        addOptimizedAdvisors(proxyFactory, target.javaClass)
        
        return proxyFactory.proxy
    }
    
    private fun applyOptimizationSettings(factory: ProxyFactory) {
        // 성능 최적화 옵션들
        factory.isOptimize = true           // 프록시 최적화 활성화
        factory.isFrozen = true             // 런타임 설정 변경 금지 (성능 향상)
        factory.isPreFiltered = true        // 사전 필터링된 어드바이저 (빠른 매칭)
        
        // CGLIB 설정 (클래스 기반이 더 빠른 경우가 많음)
        factory.isProxyTargetClass = true
        
        // 불필요한 기능 비활성화
        factory.isExposeProxy = false       // AopContext 비활성화 (약간의 성능 향상)
    }
    
    private fun addOptimizedAdvisors(factory: ProxyFactory, targetClass: Class<*>) {
        // 대상 클래스에 따라 필요한 어드바이저만 선별적으로 추가
        val advisors = selectOptimalAdvisors(targetClass)
        
        advisors.forEach { advisor ->
            factory.addAdvisor(advisor)
            logger.debug("Added advisor {} for class {}", 
                advisor.javaClass.simpleName, targetClass.simpleName)
        }
    }
    
    private fun selectOptimalAdvisors(targetClass: Class<*>): List<Advisor> {
        val advisors = mutableListOf<Advisor>()
        
        // 클래스 이름 기반 선별
        when {
            targetClass.simpleName.contains("Service") -> {
                advisors.add(createFastLoggingAdvisor())
                advisors.add(createPerformanceMonitoringAdvisor())
            }
            targetClass.simpleName.contains("Repository") -> {
                advisors.add(createTransactionAdvisor())
            }
            targetClass.simpleName.contains("Controller") -> {
                advisors.add(createWebLoggingAdvisor())
            }
        }
        
        return advisors
    }
    
    private fun createFastLoggingAdvisor(): Advisor {
        // 빠른 로깅을 위한 최적화된 어드바이스
        val advice = MethodInterceptor { invocation ->
            if (logger.isDebugEnabled) {  // 로그 레벨 체크로 불필요한 처리 방지
                logger.debug("Calling: {}", invocation.method.name)
            }
            invocation.proceed()
        }
        
        // 특정 메소드만 대상으로 하는 포인트컷
        val pointcut = object : StaticMethodMatcherPointcut() {
            override fun matches(method: Method, targetClass: Class<*>): Boolean {
                // 자주 호출되지 않는 중요한 메소드만 대상
                return method.name.startsWith("create") || 
                       method.name.startsWith("delete") ||
                       method.name.startsWith("update")
            }
        }
        
        return DefaultPointcutAdvisor(pointcut, advice)
    }
    
    private fun createPerformanceMonitoringAdvisor(): Advisor {
        val advice = MethodInterceptor { invocation ->
            val startTime = System.nanoTime()
            try {
                invocation.proceed()
            } finally {
                val duration = System.nanoTime() - startTime
                // 임계값을 넘는 경우만 로깅 (성능 영향 최소화)
                if (duration > 1_000_000) { // 1ms 이상
                    logger.warn("Slow method: {} took {:.2f}ms", 
                        invocation.method.name, duration / 1_000_000.0)
                }
            }
        }
        return DefaultPointcutAdvisor(advice)
    }
    
    private fun createTransactionAdvisor(): Advisor {
        // 실제 트랜잭션 관리는 스프링에 위임하고, 여기서는 모니터링만
        val advice = MethodInterceptor { invocation ->
            logger.debug("Transaction method: {}", invocation.method.name)
            invocation.proceed()
        }
        return DefaultPointcutAdvisor(advice)
    }
    
    private fun createWebLoggingAdvisor(): Advisor {
        val advice = MethodInterceptor { invocation ->
            // 웹 요청에 특화된 로깅
            logger.info("Web request: {}", invocation.method.name)
            invocation.proceed()
        }
        return DefaultPointcutAdvisor(advice)
    }
}
```

#### 3-2. 런타임 프록시 관리
```kotlin
// RuntimeProxyManager.kt  
@Component
class RuntimeProxyManager {
    private val hotSwappableTargets = mutableMapOf<String, HotSwappableTargetSource>()
    private val logger = LoggerFactory.getLogger(javaClass)
    
    fun <T> createSwappableProxy(initialTarget: T, proxyId: String): T {
        val targetSource = HotSwappableTargetSource(initialTarget)
        hotSwappableTargets[proxyId] = targetSource
        
        val proxyFactory = ProxyFactory()
        proxyFactory.targetSource = targetSource
        proxyFactory.addAdvisor(createSwapLoggingAdvisor(proxyId))
        
        return proxyFactory.proxy as T
    }
    
    fun <T> swapTarget(proxyId: String, newTarget: T): T? {
        val targetSource = hotSwappableTargets[proxyId]
        return if (targetSource != null) {
            val oldTarget = targetSource.swap(newTarget) as T?
            logger.info("Swapped target for proxy {}: {} -> {}", 
                proxyId, oldTarget?.javaClass?.simpleName, newTarget?.javaClass?.simpleName)
            oldTarget
        } else {
            logger.warn("No swappable proxy found with id: {}", proxyId)
            null
        }
    }
    
    fun getCurrentTarget(proxyId: String): Any? {
        return hotSwappableTargets[proxyId]?.target
    }
    
    private fun createSwapLoggingAdvisor(proxyId: String): Advisor {
        val advice = MethodInterceptor { invocation ->
            logger.debug("Method call on swappable proxy {}: {}", proxyId, invocation.method.name)
            invocation.proceed()
        }
        return DefaultPointcutAdvisor(advice)
    }
    
    fun listManagedProxies(): Map<String, String> {
        return hotSwappableTargets.mapValues { (_, targetSource) ->
            targetSource.target?.javaClass?.simpleName ?: "null"
        }
    }
}
```

## 🧪 테스트 계획

### 테스트 파일 위치
- `src/test/kotlin/bong/training/phase4/`

### 테스트 시나리오

#### 1. ProxyFactory 기능 테스트
```kotlin
// ProxyFactoryTest.kt
@SpringBootTest
class ProxyFactoryTest {
    
    @Autowired
    lateinit var programmaticProxyFactory: ProgrammaticProxyFactory
    
    @Test
    fun `기본 ProxyFactory로 프록시 생성 테스트`() {
        val target = UserServiceImpl()
        val proxy = programmaticProxyFactory.createBasicProxy(target, UserServiceImpl::class.java)
        
        assertThat(AopUtils.isAopProxy(proxy)).isTrue
        
        // 프록시를 통한 메소드 호출 테스트
        val result = proxy.findUser(1L)
        assertThat(result).isNotNull
    }
    
    @Test
    fun `최적화된 프록시 설정 확인`() {
        val target = UserServiceImpl()
        val proxy = programmaticProxyFactory.createOptimizedProxy(target, UserServiceImpl::class.java)
        
        assertThat(AopUtils.isCglibProxy(proxy)).isTrue  // CGLIB 프록시 확인
        
        if (proxy is Advised) {
            assertThat(proxy.isFrozen).isTrue       // frozen 설정 확인
            assertThat(proxy.isOptimize).isTrue     // optimize 설정 확인
        }
    }
    
    @Test
    fun `ExposeProxy 설정으로 AopContext 사용 가능 확인`() {
        val target = UserServiceImpl()
        val proxy = programmaticProxyFactory.createExposeProxyEnabled(target, UserServiceImpl::class.java)
        
        // AopContext.currentProxy() 호출이 가능한지 테스트
        assertDoesNotThrow {
            proxy.findUser(1L)
        }
    }
}
```

#### 2. TargetSource 테스트
```kotlin
// TargetSourceTest.kt
@SpringBootTest
class TargetSourceTest {
    
    @Autowired
    lateinit var targetSourceFactory: TargetSourceFactory
    
    @Test
    fun `HotSwappable TargetSource 테스트`() {
        val initialTarget = UserServiceImpl()
        val targetSource = targetSourceFactory.createHotSwappableTargetSource(initialTarget)
        
        val proxyFactory = ProxyFactory()
        proxyFactory.targetSource = targetSource
        val proxy = proxyFactory.proxy as UserService
        
        // 초기 대상으로 메소드 호출
        val result1 = proxy.findUser(1L)
        
        // 대상 교체
        val newTarget = UserServiceImpl()
        targetSource.swap(newTarget)
        
        // 새로운 대상으로 메소드 호출
        val result2 = proxy.findUser(1L)
        
        // 두 결과가 다른 인스턴스에서 나온 것인지 확인
        assertThat(result1).isEqualTo(result2)
    }
    
    @Test
    fun `ThreadLocal TargetSource 테스트`() {
        val initialTarget = UserServiceImpl()
        val targetSource = targetSourceFactory.createThreadLocalTargetSource(initialTarget)
        
        val proxyFactory = ProxyFactory()
        proxyFactory.targetSource = targetSource
        val proxy = proxyFactory.proxy as UserService
        
        val results = Collections.synchronizedList(mutableListOf<String>())
        
        // 여러 스레드에서 동시 실행
        val threads = (1..3).map { threadId ->
            Thread {
                repeat(2) {
                    results.add("Thread-$threadId: ${proxy.findUser(threadId.toLong())}")
                }
            }
        }
        
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        
        assertThat(results).hasSize(6)
        println(results.sorted())
    }
}
```

#### 3. 성능 비교 테스트
```kotlin
// ProxyPerformanceComparisonTest.kt
@SpringBootTest
class ProxyPerformanceComparisonTest {
    
    private val iterations = 1_000_000
    
    @Test
    fun `일반 객체 vs 프록시 객체 성능 비교`() {
        val normalTarget = UserServiceImpl()
        val proxyTarget = createProxyWithMinimalAdvice(normalTarget)
        
        // 워밍업
        repeat(10_000) { 
            normalTarget.findUser(1L)
            proxyTarget.findUser(1L)
        }
        
        // 일반 객체 성능 측정
        val normalTime = measureTimeMillis {
            repeat(iterations) {
                normalTarget.findUser(1L)
            }
        }
        
        // 프록시 객체 성능 측정
        val proxyTime = measureTimeMillis {
            repeat(iterations) {
                proxyTarget.findUser(1L)
            }
        }
        
        val overhead = ((proxyTime - normalTime).toDouble() / normalTime * 100)
        
        println("Normal object: ${normalTime}ms")
        println("Proxy object: ${proxyTime}ms")
        println("Overhead: ${"%.2f".format(overhead)}%")
        
        // 일반적으로 프록시 오버헤드는 20% 이내
        assertThat(overhead).isLessThan(50.0)
    }
    
    @Test  
    fun `JDK vs CGLIB 프록시 성능 비교`() {
        val target = UserServiceImpl()
        
        // JDK 동적 프록시
        val jdkProxy = createJdkProxy(target)
        
        // CGLIB 프록시
        val cglibProxy = createCglibProxy(target)
        
        // 각각 성능 측정
        val jdkTime = measureTimeMillis {
            repeat(iterations) {
                jdkProxy.findUser(1L)
            }
        }
        
        val cglibTime = measureTimeMillis {
            repeat(iterations) {
                cglibProxy.findUser(1L)
            }
        }
        
        println("JDK Proxy: ${jdkTime}ms")
        println("CGLIB Proxy: ${cglibTime}ms")
        
        val difference = kotlin.math.abs(jdkTime - cglibTime).toDouble() / kotlin.math.min(jdkTime, cglibTime) * 100
        println("Performance difference: ${"%.2f".format(difference)}%")
    }
    
    private fun createProxyWithMinimalAdvice(target: UserService): UserService {
        val proxyFactory = ProxyFactory(target)
        proxyFactory.addAdvice(MethodInterceptor { it.proceed() })  // 최소한의 어드바이스
        return proxyFactory.proxy as UserService
    }
    
    private fun createJdkProxy(target: UserServiceImpl): UserService {
        val proxyFactory = ProxyFactory(target)
        proxyFactory.setInterfaces(UserService::class.java)
        proxyFactory.isProxyTargetClass = false
        proxyFactory.addAdvice(MethodInterceptor { it.proceed() })
        return proxyFactory.proxy as UserService
    }
    
    private fun createCglibProxy(target: UserServiceImpl): UserService {
        val proxyFactory = ProxyFactory(target)
        proxyFactory.isProxyTargetClass = true
        proxyFactory.addAdvice(MethodInterceptor { it.proceed() })
        return proxyFactory.proxy as UserService
    }
}
```

## 📊 학습 결과 분석

### 분석 항목
1. **ProxyFactory vs @Aspect 방식 비교**
   - 유연성 vs 편의성
   - 성능 차이 분석
   - 사용 시점 결정 기준

2. **최적화 설정들의 실제 효과**
   - optimize, frozen, preFiltered 옵션들의 성능 영향
   - 메모리 사용량 차이

3. **TargetSource별 특성 분석**
   - 각 TargetSource의 사용 사례
   - 성능 및 메모리 영향도

4. **실제 운영 환경 고려사항**
   - 프록시로 인한 디버깅 어려움
   - 스택 트레이스 복잡성
   - 메모리 누수 가능성

## 🎓 학습 완료 기준

### 체크리스트
- [ ] ProxyFactory를 이용한 다양한 프록시 생성 방법 습득
- [ ] ProxyFactoryBean 설정 방법 완전 이해
- [ ] 고급 프록시 옵션들의 효과 확인
- [ ] TargetSource를 활용한 특수 프록시 구현
- [ ] 성능 최적화 기법 적용 및 검증
- [ ] 실제 운영 환경에서의 고려사항 파악

### 다음 단계 준비
- 실제 스프링 소스 코드 분석 준비
- AbstractAutoProxyCreator 상속 구조 이해
- AspectJAutoProxyCreator의 내부 동작 원리

---
*예상 소요 시간: 4-5일*
*난이도: ⭐⭐⭐⭐☆*