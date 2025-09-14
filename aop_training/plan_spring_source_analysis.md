# Phase 5: 실제 스프링 소스 코드 분석

## 🎯 학습 목표
- 스프링 AOP의 실제 구현 코드를 심층 분석하여 내부 동작 원리 완전 이해
- AbstractAutoProxyCreator 계층 구조와 각 구현체의 역할 파악
- AspectJAutoProxyCreator의 @Aspect 처리 메커니즘 분석
- 실제 애플리케이션 시작 과정에서의 프록시 생성 전체 흐름 추적

## 📚 이론 학습 내용

### 1. 스프링 AOP 아키텍처 전체 구조
```
ProxyProcessorSupport
    ↓
AbstractAutoProxyCreator (핵심 추상 클래스)
    ↓
AbstractAdvisorAutoProxyCreator
    ↓
AspectJAwareAdvisorAutoProxyCreator
    ↓  
AnnotationAwareAspectJAutoProxyCreator (@EnableAspectJAutoProxy에서 사용)
```

### 2. 핵심 인터페이스들
- **BeanPostProcessor**: 빈 후처리 진입점
- **SmartInstantiationAwareBeanPostProcessor**: 고급 빈 처리
- **BeanFactoryAware**: BeanFactory 참조 획득
- **Ordered**: 실행 순서 제어

### 3. 주요 처리 단계
1. **후보 빈 식별**: 프록시가 필요한 빈 찾기
2. **Advisor 수집**: 적용 가능한 어드바이스 모으기
3. **프록시 생성 결정**: 실제 프록시를 만들지 판단
4. **프록시 팩토리 생성**: JDK vs CGLIB 선택
5. **프록시 객체 반환**: 원본 대신 프록시 반환

## 🛠 실습 과제

### 과제 1: 스프링 소스 코드 분석 및 재구현
**파일 위치**: `src/main/kotlin/bong/training/phase5/analysis/`

#### 1-1. AbstractAutoProxyCreator 핵심 로직 분석
```kotlin
// AbstractAutoProxyCreatorAnalysis.kt
@Component
class AbstractAutoProxyCreatorAnalysis {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * AbstractAutoProxyCreator.postProcessAfterInitialization()의 핵심 로직을 분석하고 재구현
     */
    fun analyzePostProcessAfterInitialization(bean: Any, beanName: String): Any? {
        logger.info("=== Analyzing postProcessAfterInitialization for {} ===", beanName)
        
        // 1. 인프라 빈 체크 (스프링 내부 빈들은 프록시 생성 제외)
        if (isInfrastructureBean(bean, beanName)) {
            logger.info("Infrastructure bean detected: {}, skipping proxy creation", beanName)
            return bean
        }
        
        // 2. 이미 프록시된 빈인지 체크
        if (AopUtils.isAopProxy(bean)) {
            logger.info("Bean {} is already a proxy, skipping", beanName)
            return bean
        }
        
        // 3. 프록시 생성 대상인지 확인
        val advisors = getAdvicesAndAdvisorsForBean(bean.javaClass, beanName)
        if (advisors.isEmpty()) {
            logger.info("No advisors found for bean {}, no proxy needed", beanName)
            return bean
        }
        
        // 4. 프록시 생성
        logger.info("Creating proxy for bean {} with {} advisors", beanName, advisors.size)
        return createProxyIfNecessary(bean, beanName, advisors)
    }
    
    /**
     * 인프라 빈 체크 로직 분석
     */
    private fun isInfrastructureBean(bean: Any, beanName: String): Boolean {
        // 스프링 내부 빈들 체크
        val infrastructureClasses = setOf(
            "org.springframework.aop",
            "org.springframework.context",
            "org.springframework.beans",
            "org.springframework.boot"
        )
        
        val packageName = bean.javaClass.`package`?.name ?: ""
        val isInfrastructure = infrastructureClasses.any { packageName.startsWith(it) }
        
        // Advisor, Advice 등도 인프라 빈으로 취급
        val isAopInfrastructure = bean is Advisor || 
                                  bean is Advice ||
                                  bean is Pointcut
        
        return isInfrastructure || isAopInfrastructure || beanName.startsWith("org.springframework")
    }
    
    /**
     * 어드바이저 수집 로직 분석 (실제로는 하위 클래스에서 구현)
     */
    private fun getAdvicesAndAdvisorsForBean(beanClass: Class<*>, beanName: String): List<Advisor> {
        logger.info("Collecting advisors for class: {}, bean: {}", beanClass.simpleName, beanName)
        
        // 실제 AnnotationAwareAspectJAutoProxyCreator의 로직을 시뮬레이션
        val candidateAdvisors = findCandidateAdvisors()
        val eligibleAdvisors = findEligibleAdvisors(candidateAdvisors, beanClass, beanName)
        
        logger.info("Found {} eligible advisors for {}", eligibleAdvisors.size, beanName)
        return eligibleAdvisors
    }
    
    private fun findCandidateAdvisors(): List<Advisor> {
        // 1. 일반적인 Advisor 빈들 수집
        val advisors = mutableListOf<Advisor>()
        
        // 2. @Aspect 어노테이션이 붙은 클래스들에서 Advisor 생성
        // (실제로는 BeanFactoryAspectJAdvisorsBuilder가 담당)
        
        logger.info("Found {} candidate advisors", advisors.size)
        return advisors
    }
    
    private fun findEligibleAdvisors(
        candidateAdvisors: List<Advisor>, 
        beanClass: Class<*>, 
        beanName: String
    ): List<Advisor> {
        val eligibleAdvisors = candidateAdvisors.filter { advisor ->
            canApply(advisor, beanClass)
        }
        
        // ExposeInvocationInterceptor를 맨 앞에 추가 (스프링의 기본 동작)
        return if (eligibleAdvisors.isNotEmpty()) {
            val result = mutableListOf<Advisor>()
            result.add(ExposeInvocationInterceptor.ADVISOR)  // 항상 첫 번째
            result.addAll(eligibleAdvisors)
            result
        } else {
            eligibleAdvisors
        }
    }
    
    private fun canApply(advisor: Advisor, targetClass: Class<*>): Boolean {
        // Pointcut 매칭 로직 (AopUtils.canApply()와 동일)
        if (advisor is PointcutAdvisor) {
            val pointcut = advisor.pointcut
            
            // 클래스 레벨 매칭
            if (!pointcut.classFilter.matches(targetClass)) {
                return false
            }
            
            // 메소드 레벨 매칭
            val methods = targetClass.declaredMethods
            return methods.any { method ->
                pointcut.methodMatcher.matches(method, targetClass)
            }
        }
        
        return true  // IntroductionAdvisor 등은 항상 적용
    }
    
    private fun createProxyIfNecessary(bean: Any, beanName: String, advisors: List<Advisor>): Any {
        val proxyFactory = ProxyFactory()
        proxyFactory.copyFrom(getProxyFactoryConfiguration())
        proxyFactory.setTarget(bean)
        
        // Advisor들 추가
        advisors.forEach { advisor ->
            proxyFactory.addAdvisor(advisor)
        }
        
        // 프록시 타입 결정 로직 분석
        determineProxyType(proxyFactory, bean.javaClass)
        
        val proxy = proxyFactory.proxy
        logger.info("Created proxy for {}: {} (type: {})", 
            beanName, 
            proxy.javaClass.simpleName,
            if (AopUtils.isJdkDynamicProxy(proxy)) "JDK" else "CGLIB"
        )
        
        return proxy
    }
    
    private fun getProxyFactoryConfiguration(): ProxyFactory {
        val factory = ProxyFactory()
        // 기본 설정들 (실제로는 ProxyProcessorSupport에서 관리)
        factory.isProxyTargetClass = false  // 설정에 따라 변경
        factory.isOptimize = false
        factory.isExposeProxy = false
        return factory
    }
    
    private fun determineProxyType(proxyFactory: ProxyFactory, targetClass: Class<*>) {
        // 1. proxyTargetClass 설정 확인
        if (shouldProxyTargetClass(targetClass)) {
            proxyFactory.isProxyTargetClass = true
            return
        }
        
        // 2. 인터페이스 존재 여부 확인
        val interfaces = targetClass.interfaces
        if (interfaces.isEmpty()) {
            logger.info("No interfaces found for {}, using CGLIB", targetClass.simpleName)
            proxyFactory.isProxyTargetClass = true
        } else {
            logger.info("Found {} interfaces for {}, using JDK proxy", interfaces.size, targetClass.simpleName)
            proxyFactory.setInterfaces(*interfaces)
        }
    }
    
    private fun shouldProxyTargetClass(targetClass: Class<*>): Boolean {
        // @EnableAspectJAutoProxy(proxyTargetClass = true) 설정 확인
        // 여기서는 간단히 특정 조건으로 판단
        return targetClass.name.contains("Service")  // 예시 조건
    }
}
```

#### 1-2. AspectJ 어드바이저 빌더 분석
```kotlin
// BeanFactoryAspectJAdvisorsBuilderAnalysis.kt
@Component
class BeanFactoryAspectJAdvisorsBuilderAnalysis : ApplicationContextAware {
    private val logger = LoggerFactory.getLogger(javaClass)
    private lateinit var applicationContext: ApplicationContext
    private val aspectBeanNames = mutableSetOf<String>()
    private val advisorsCache = mutableMapOf<String, List<Advisor>>()
    
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
    
    /**
     * BeanFactoryAspectJAdvisorsBuilder.buildAspectJAdvisors()의 핵심 로직 분석
     */
    fun buildAspectJAdvisors(): List<Advisor> {
        logger.info("=== Building AspectJ Advisors ===")
        
        val advisors = mutableListOf<Advisor>()
        
        // 1. @Aspect가 붙은 빈들 찾기
        val aspectBeans = findAspectBeans()
        
        // 2. 각 Aspect 빈에서 어드바이저들 생성
        aspectBeans.forEach { (beanName, aspectClass) ->
            val aspectAdvisors = buildAspectJAdvisors(beanName, aspectClass)
            advisors.addAll(aspectAdvisors)
            advisorsCache[beanName] = aspectAdvisors
        }
        
        logger.info("Built {} advisors from {} aspect beans", advisors.size, aspectBeans.size)
        return advisors
    }
    
    private fun findAspectBeans(): Map<String, Class<*>> {
        val aspectBeans = mutableMapOf<String, Class<*>>()
        
        // ApplicationContext에서 모든 빈 이름 가져오기
        val beanNames = applicationContext.beanDefinitionNames
        
        beanNames.forEach { beanName ->
            try {
                val beanType = applicationContext.getType(beanName)
                if (beanType != null && isAspect(beanType)) {
                    aspectBeans[beanName] = beanType
                    aspectBeanNames.add(beanName)
                    logger.info("Found aspect bean: {} ({})", beanName, beanType.simpleName)
                }
            } catch (ex: Exception) {
                logger.warn("Error checking bean {}: {}", beanName, ex.message)
            }
        }
        
        return aspectBeans
    }
    
    private fun isAspect(beanType: Class<*>): Boolean {
        // 1. @Aspect 어노테이션 확인
        if (beanType.isAnnotationPresent(Aspect::class.java)) {
            return true
        }
        
        // 2. AspectJ 스타일 이름 패턴 확인 (선택사항)
        val className = beanType.simpleName
        return className.endsWith("Aspect") || className.endsWith("Interceptor")
    }
    
    private fun buildAspectJAdvisors(beanName: String, aspectClass: Class<*>): List<Advisor> {
        logger.info("Building advisors for aspect: {} ({})", beanName, aspectClass.simpleName)
        
        val advisors = mutableListOf<Advisor>()
        
        // Aspect 클래스의 모든 메소드 검사
        val methods = aspectClass.declaredMethods
        
        methods.forEach { method ->
            val advisor = getAdvisorForMethod(method, aspectClass, beanName)
            if (advisor != null) {
                advisors.add(advisor)
                logger.info("Created advisor for method: {}.{}", aspectClass.simpleName, method.name)
            }
        }
        
        return advisors
    }
    
    private fun getAdvisorForMethod(method: Method, aspectClass: Class<*>, aspectBeanName: String): Advisor? {
        // 1. Pointcut 추출
        val pointcut = getPointcut(method) ?: return null
        
        // 2. Advice 생성
        val advice = getAdvice(method, aspectClass, aspectBeanName) ?: return null
        
        // 3. Advisor 생성
        val advisor = InstantiationModelAwarePointcutAdvisorImpl(
            pointcut, 
            advice, 
            aspectClass, 
            method,
            applicationContext
        )
        
        return advisor
    }
    
    private fun getPointcut(method: Method): Pointcut? {
        // 각 어드바이스 타입별 어노테이션 확인
        return when {
            method.isAnnotationPresent(Before::class.java) -> {
                val annotation = method.getAnnotation(Before::class.java)
                createPointcut(annotation.value, AdviceType.BEFORE)
            }
            method.isAnnotationPresent(After::class.java) -> {
                val annotation = method.getAnnotation(After::class.java)
                createPointcut(annotation.value, AdviceType.AFTER)
            }
            method.isAnnotationPresent(AfterReturning::class.java) -> {
                val annotation = method.getAnnotation(AfterReturning::class.java)
                createPointcut(annotation.pointcut.ifEmpty { annotation.value }, AdviceType.AFTER_RETURNING)
            }
            method.isAnnotationPresent(AfterThrowing::class.java) -> {
                val annotation = method.getAnnotation(AfterThrowing::class.java)
                createPointcut(annotation.pointcut.ifEmpty { annotation.value }, AdviceType.AFTER_THROWING)
            }
            method.isAnnotationPresent(Around::class.java) -> {
                val annotation = method.getAnnotation(Around::class.java)
                createPointcut(annotation.value, AdviceType.AROUND)
            }
            else -> null
        }
    }
    
    private fun createPointcut(expression: String, adviceType: AdviceType): Pointcut {
        logger.debug("Creating {} pointcut with expression: {}", adviceType, expression)
        
        // AspectJ 표현식을 사용한 Pointcut 생성
        val pointcut = AspectJExpressionPointcut()
        pointcut.expression = expression
        
        return pointcut
    }
    
    private fun getAdvice(method: Method, aspectClass: Class<*>, aspectBeanName: String): Advice? {
        return when {
            method.isAnnotationPresent(Before::class.java) -> {
                AspectJMethodBeforeAdvice(method, createPointcut("", AdviceType.BEFORE), aspectClass)
            }
            method.isAnnotationPresent(After::class.java) -> {
                AspectJAfterAdvice(method, createPointcut("", AdviceType.AFTER), aspectClass)
            }
            method.isAnnotationPresent(AfterReturning::class.java) -> {
                AspectJAfterReturningAdvice(method, createPointcut("", AdviceType.AFTER_RETURNING), aspectClass)
            }
            method.isAnnotationPresent(AfterThrowing::class.java) -> {
                AspectJAfterThrowingAdvice(method, createPointcut("", AdviceType.AFTER_THROWING), aspectClass)
            }
            method.isAnnotationPresent(Around::class.java) -> {
                AspectJAroundAdvice(method, createPointcut("", AdviceType.AROUND), aspectClass)
            }
            else -> null
        }
    }
    
    enum class AdviceType {
        BEFORE, AFTER, AFTER_RETURNING, AFTER_THROWING, AROUND
    }
    
    fun getCachedAdvisors(): Map<String, List<Advisor>> = advisorsCache.toMap()
    
    fun getAspectBeanNames(): Set<String> = aspectBeanNames.toSet()
}

/**
 * InstantiationModelAwarePointcutAdvisorImpl의 간단한 구현
 */
class InstantiationModelAwarePointcutAdvisorImpl(
    private val pointcut: Pointcut,
    private val advice: Advice,
    private val aspectClass: Class<*>,
    private val method: Method,
    private val applicationContext: ApplicationContext
) : PointcutAdvisor {
    
    override fun getPointcut(): Pointcut = pointcut
    override fun getAdvice(): Advice = advice
    override fun isPerInstance(): Boolean = false
    
    override fun toString(): String {
        return "InstantiationModelAwarePointcutAdvisorImpl: pointcut=[${pointcut}]; advice=[${advice}]"
    }
}
```

### 과제 2: 실제 애플리케이션 시작 과정 추적
**파일 위치**: `src/main/kotlin/bong/training/phase5/tracing/`

#### 2-1. 애플리케이션 시작 과정 추적기
```kotlin
// ApplicationStartupTracer.kt
@Component
class ApplicationStartupTracer : 
    ApplicationListener<ContextRefreshedEvent>,
    BeanPostProcessor {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    private val processingHistory = mutableListOf<BeanProcessingEvent>()
    private val proxyCreationEvents = mutableListOf<ProxyCreationEvent>()
    
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        val event = BeanProcessingEvent(
            beanName = beanName,
            beanClass = bean.javaClass.name,
            isProxy = AopUtils.isAopProxy(bean),
            proxyType = getProxyType(bean),
            timestamp = System.currentTimeMillis(),
            threadName = Thread.currentThread().name
        )
        
        processingHistory.add(event)
        
        if (event.isProxy) {
            val proxyEvent = ProxyCreationEvent(
                beanName = beanName,
                targetClass = AopUtils.getTargetClass(bean).name,
                proxyClass = bean.javaClass.name,
                advisorCount = if (bean is Advised) bean.advisors.size else 0,
                timestamp = event.timestamp
            )
            proxyCreationEvents.add(proxyEvent)
            
            logger.info("PROXY CREATED: {} -> {} (advisors: {})", 
                proxyEvent.targetClass, proxyEvent.proxyClass, proxyEvent.advisorCount)
        }
        
        return bean
    }
    
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        logger.info("=== APPLICATION STARTUP COMPLETED ===")
        printStartupSummary()
        printProxyCreationSummary()
        printTimeline()
    }
    
    private fun getProxyType(bean: Any): String {
        return when {
            AopUtils.isJdkDynamicProxy(bean) -> "JDK_DYNAMIC"
            AopUtils.isCglibProxy(bean) -> "CGLIB"
            else -> "NO_PROXY"
        }
    }
    
    private fun printStartupSummary() {
        val totalBeans = processingHistory.size
        val proxyBeans = processingHistory.count { it.isProxy }
        val jdkProxies = processingHistory.count { it.proxyType == "JDK_DYNAMIC" }
        val cglibProxies = processingHistory.count { it.proxyType == "CGLIB" }
        
        logger.info("=== STARTUP SUMMARY ===")
        logger.info("Total beans processed: {}", totalBeans)
        logger.info("Proxy beans created: {} ({:.1f}%)", proxyBeans, proxyBeans.toDouble() / totalBeans * 100)
        logger.info("JDK Dynamic proxies: {}", jdkProxies)
        logger.info("CGLIB proxies: {}", cglibProxies)
    }
    
    private fun printProxyCreationSummary() {
        logger.info("=== PROXY CREATION DETAILS ===")
        proxyCreationEvents.forEach { event ->
            logger.info("PROXY: {} -> {} ({} advisors)", 
                event.targetClass.substringAfterLast('.'), 
                event.proxyClass.substringAfterLast('.'),
                event.advisorCount)
        }
    }
    
    private fun printTimeline() {
        logger.info("=== PROCESSING TIMELINE ===")
        
        val startTime = processingHistory.minOfOrNull { it.timestamp } ?: return
        
        processingHistory
            .filter { it.isProxy }  // 프록시만 표시
            .sortedBy { it.timestamp }
            .forEach { event ->
                val elapsed = event.timestamp - startTime
                logger.info("[{:04d}ms] {} ({})", elapsed, event.beanName, event.proxyType)
            }
    }
    
    fun getProcessingHistory(): List<BeanProcessingEvent> = processingHistory.toList()
    fun getProxyCreationEvents(): List<ProxyCreationEvent> = proxyCreationEvents.toList()
}

data class BeanProcessingEvent(
    val beanName: String,
    val beanClass: String,
    val isProxy: Boolean,
    val proxyType: String,
    val timestamp: Long,
    val threadName: String
)

data class ProxyCreationEvent(
    val beanName: String,
    val targetClass: String,
    val proxyClass: String,
    val advisorCount: Int,
    val timestamp: Long
)
```

#### 2-2. 스프링 컨텍스트 라이프사이클 추적
```kotlin
// SpringContextLifecycleTracer.kt
@Component
class SpringContextLifecycleTracer :
    ApplicationListener<ApplicationEvent>,
    BeanFactoryPostProcessor,
    BeanPostProcessor {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    private val events = mutableListOf<LifecycleEventRecord>()
    
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        addEvent("BeanFactoryPostProcessor.postProcessBeanFactory", "BeanFactory post-processing")
        
        // BeanDefinition 분석
        val beanDefinitionCount = beanFactory.beanDefinitionCount
        val beanNames = beanFactory.beanDefinitionNames
        
        logger.info("BeanFactory contains {} bean definitions", beanDefinitionCount)
        
        // AOP 관련 빈들 찾기
        val aopBeans = beanNames.filter { beanName ->
            try {
                val beanDefinition = beanFactory.getBeanDefinition(beanName)
                val beanClassName = beanDefinition.beanClassName
                beanClassName?.contains("Aspect") == true || 
                beanClassName?.contains("Advisor") == true ||
                beanClassName?.contains("Proxy") == true
            } catch (ex: Exception) {
                false
            }
        }
        
        logger.info("Found {} AOP-related beans: {}", aopBeans.size, aopBeans)
    }
    
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        if (isImportantBean(beanName)) {
            addEvent("postProcessBeforeInitialization", "Processing $beanName")
        }
        return bean
    }
    
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (isImportantBean(beanName)) {
            val isProxy = AopUtils.isAopProxy(bean)
            addEvent("postProcessAfterInitialization", 
                "Processed $beanName (proxy: $isProxy)")
        }
        return bean
    }
    
    override fun onApplicationEvent(event: ApplicationEvent) {
        when (event) {
            is ContextStartedEvent -> addEvent("ContextStartedEvent", "Context started")
            is ContextRefreshedEvent -> {
                addEvent("ContextRefreshedEvent", "Context refreshed")
                printFullLifecycle()
            }
            is ContextStoppedEvent -> addEvent("ContextStoppedEvent", "Context stopped")
            is ContextClosedEvent -> addEvent("ContextClosedEvent", "Context closed")
        }
    }
    
    private fun isImportantBean(beanName: String): Boolean {
        return beanName.contains("Service") || 
               beanName.contains("Controller") ||
               beanName.contains("Repository") ||
               beanName.contains("Aspect") ||
               beanName.contains("Advisor")
    }
    
    private fun addEvent(eventType: String, description: String) {
        val event = LifecycleEventRecord(
            timestamp = System.currentTimeMillis(),
            eventType = eventType,
            description = description,
            threadName = Thread.currentThread().name
        )
        events.add(event)
        
        logger.debug("LIFECYCLE EVENT: {} - {}", eventType, description)
    }
    
    private fun printFullLifecycle() {
        logger.info("=== FULL CONTEXT LIFECYCLE ===")
        
        val startTime = events.minOfOrNull { it.timestamp } ?: return
        
        events.forEach { event ->
            val elapsed = event.timestamp - startTime
            logger.info("[{:04d}ms] {} - {}", elapsed, event.eventType, event.description)
        }
        
        val totalTime = events.maxOfOrNull { it.timestamp }?.let { it - startTime } ?: 0
        logger.info("Total context initialization time: {}ms", totalTime)
    }
    
    fun getLifecycleEvents(): List<LifecycleEventRecord> = events.toList()
}

data class LifecycleEventRecord(
    val timestamp: Long,
    val eventType: String,
    val description: String,
    val threadName: String
)
```

### 과제 3: 디버깅 및 트러블슈팅 도구
**파일 위치**: `src/main/kotlin/bong/training/phase5/debugging/`

#### 3-1. AOP 디버깅 유틸리티
```kotlin
// AopDebuggingUtils.kt
@Component
class AopDebuggingUtils : ApplicationContextAware {
    private lateinit var applicationContext: ApplicationContext
    private val logger = LoggerFactory.getLogger(javaClass)
    
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
    
    /**
     * 특정 빈의 프록시 정보를 상세 분석
     */
    fun analyzeBean(beanName: String) {
        logger.info("=== ANALYZING BEAN: {} ===", beanName)
        
        try {
            val bean = applicationContext.getBean(beanName)
            
            // 기본 정보
            logger.info("Bean class: {}", bean.javaClass.name)
            logger.info("Is AOP proxy: {}", AopUtils.isAopProxy(bean))
            
            if (AopUtils.isAopProxy(bean)) {
                analyzeProxy(bean, beanName)
            } else {
                logger.info("Bean is not a proxy")
            }
            
        } catch (ex: Exception) {
            logger.error("Error analyzing bean {}: {}", beanName, ex.message)
        }
    }
    
    private fun analyzeProxy(proxy: Any, beanName: String) {
        logger.info("=== PROXY ANALYSIS ===")
        
        // 프록시 타입
        val proxyType = when {
            AopUtils.isJdkDynamicProxy(proxy) -> "JDK Dynamic Proxy"
            AopUtils.isCglibProxy(proxy) -> "CGLIB Proxy"
            else -> "Unknown"
        }
        logger.info("Proxy type: {}", proxyType)
        
        // 타겟 클래스
        val targetClass = AopUtils.getTargetClass(proxy)
        logger.info("Target class: {}", targetClass.name)
        
        // 인터페이스들
        val interfaces = targetClass.interfaces
        logger.info("Implemented interfaces: {}", interfaces.map { it.name })
        
        // Advised 정보
        if (proxy is Advised) {
            analyzeAdvised(proxy)
        }
        
        // 실제 타겟 객체에 접근 시도
        try {
            val target = AopTestUtils.getTargetObject(proxy)
            logger.info("Actual target object: {}", target.javaClass.name)
        } catch (ex: Exception) {
            logger.warn("Cannot access target object: {}", ex.message)
        }
    }
    
    private fun analyzeAdvised(advised: Advised) {
        logger.info("=== ADVISED CONFIGURATION ===")
        logger.info("Proxy target class: {}", advised.isProxyTargetClass)
        logger.info("Optimize: {}", advised.isOptimize)
        logger.info("Expose proxy: {}", advised.isExposeProxy)
        logger.info("Frozen: {}", advised.isFrozen)
        logger.info("Pre-filtered: {}", advised.isPreFiltered)
        
        // Advisors 분석
        val advisors = advised.advisors
        logger.info("Number of advisors: {}", advisors.size)
        
        advisors.forEachIndexed { index, advisor ->
            logger.info("Advisor {}: {}", index, advisor.javaClass.simpleName)
            
            if (advisor is PointcutAdvisor) {
                val pointcut = advisor.pointcut
                logger.info("  Pointcut: {}", pointcut)
                
                if (pointcut is AspectJExpressionPointcut) {
                    logger.info("  Expression: {}", pointcut.expression)
                }
            }
            
            val advice = advisor.advice
            logger.info("  Advice: {}", advice.javaClass.simpleName)
        }
    }
    
    /**
     * 모든 프록시 빈들 나열
     */
    fun listAllProxyBeans() {
        logger.info("=== ALL PROXY BEANS ===")
        
        val beanNames = applicationContext.beanDefinitionNames
        val proxyBeans = mutableListOf<String>()
        
        beanNames.forEach { beanName ->
            try {
                val bean = applicationContext.getBean(beanName)
                if (AopUtils.isAopProxy(bean)) {
                    proxyBeans.add(beanName)
                    
                    val proxyType = if (AopUtils.isJdkDynamicProxy(bean)) "JDK" else "CGLIB"
                    val targetClass = AopUtils.getTargetClass(bean).simpleName
                    val advisorCount = if (bean is Advised) bean.advisors.size else 0
                    
                    logger.info("{}: {} -> {} ({} advisors)", 
                        beanName, targetClass, proxyType, advisorCount)
                }
            } catch (ex: Exception) {
                // 빈 생성 실패 시 무시
            }
        }
        
        logger.info("Total proxy beans: {}", proxyBeans.size)
    }
    
    /**
     * 특정 메소드가 어떤 어드바이스들에 매칭되는지 확인
     */
    fun checkMethodMatching(beanName: String, methodName: String) {
        logger.info("=== METHOD MATCHING ANALYSIS: {}.{} ===", beanName, methodName)
        
        try {
            val bean = applicationContext.getBean(beanName)
            
            if (!AopUtils.isAopProxy(bean)) {
                logger.info("Bean is not a proxy")
                return
            }
            
            val targetClass = AopUtils.getTargetClass(bean)
            val method = targetClass.declaredMethods.find { it.name == methodName }
            
            if (method == null) {
                logger.warn("Method {} not found in {}", methodName, targetClass.name)
                return
            }
            
            if (bean is Advised) {
                val advisors = bean.advisors
                
                advisors.forEachIndexed { index, advisor ->
                    val matches = when (advisor) {
                        is PointcutAdvisor -> {
                            val pointcut = advisor.pointcut
                            pointcut.classFilter.matches(targetClass) &&
                            pointcut.methodMatcher.matches(method, targetClass)
                        }
                        else -> true  // IntroductionAdvisor 등
                    }
                    
                    logger.info("Advisor {} ({}): MATCH = {}", 
                        index, advisor.javaClass.simpleName, matches)
                }
            }
            
        } catch (ex: Exception) {
            logger.error("Error checking method matching: {}", ex.message)
        }
    }
    
    /**
     * 프록시 생성 시뮬레이션 (실제 생성하지 않고 로직만 확인)
     */
    fun simulateProxyCreation(targetClass: Class<*>) {
        logger.info("=== SIMULATING PROXY CREATION FOR {} ===", targetClass.simpleName)
        
        // 1. 어드바이저 수집 시뮬레이션
        val candidateAdvisors = findAllAdvisors()
        logger.info("Found {} candidate advisors", candidateAdvisors.size)
        
        // 2. 매칭 확인
        val matchingAdvisors = candidateAdvisors.filter { advisor ->
            canApply(advisor, targetClass)
        }
        logger.info("Found {} matching advisors", matchingAdvisors.size)
        
        // 3. 프록시 타입 결정
        val shouldUseClassProxy = shouldProxyTargetClass(targetClass)
        val proxyType = if (shouldUseClassProxy) "CGLIB" else "JDK Dynamic"
        logger.info("Would create {} proxy", proxyType)
        
        if (matchingAdvisors.isEmpty()) {
            logger.info("No proxy would be created (no matching advisors)")
        }
    }
    
    private fun findAllAdvisors(): List<Advisor> {
        return try {
            applicationContext.getBeansOfType(Advisor::class.java).values.toList()
        } catch (ex: Exception) {
            emptyList()
        }
    }
    
    private fun canApply(advisor: Advisor, targetClass: Class<*>): Boolean {
        return if (advisor is PointcutAdvisor) {
            val pointcut = advisor.pointcut
            pointcut.classFilter.matches(targetClass) &&
            targetClass.declaredMethods.any { method ->
                pointcut.methodMatcher.matches(method, targetClass)
            }
        } else {
            true
        }
    }
    
    private fun shouldProxyTargetClass(targetClass: Class<*>): Boolean {
        return targetClass.interfaces.isEmpty() || 
               targetClass.name.contains("Service")  // 예시 로직
    }
}
```

## 🧪 테스트 계획

### 테스트 파일 위치
- `src/test/kotlin/bong/training/phase5/`

### 테스트 시나리오

#### 1. 소스 코드 분석 검증
```kotlin
// SpringSourceAnalysisTest.kt
@SpringBootTest
@TestPropertySource(properties = [
    "logging.level.bong.training.phase5=DEBUG"
])
class SpringSourceAnalysisTest {
    
    @Autowired
    lateinit var abstractAutoProxyCreatorAnalysis: AbstractAutoProxyCreatorAnalysis
    
    @Autowired
    lateinit var aspectJAdvisorsBuilderAnalysis: BeanFactoryAspectJAdvisorsBuilderAnalysis
    
    @Autowired
    lateinit var userService: UserService
    
    @Test
    fun `AbstractAutoProxyCreator 로직 분석 테스트`() {
        // Phase 2의 서비스 빈으로 분석 로직 테스트
        val result = abstractAutoProxyCreatorAnalysis.analyzePostProcessAfterInitialization(
            userService, "userService"
        )
        
        // 원본과 다른 객체 (프록시)가 반환되어야 함
        assertThat(result).isNotSameAs(userService)
        assertThat(AopUtils.isAopProxy(result)).isTrue
    }
    
    @Test
    fun `AspectJ 어드바이저 빌딩 로직 분석`() {
        val advisors = aspectJAdvisorsBuilderAnalysis.buildAspectJAdvisors()
        
        assertThat(advisors).isNotEmpty
        
        // Phase 2에서 만든 Aspect들이 Advisor로 변환되었는지 확인
        val aspectBeanNames = aspectJAdvisorsBuilderAnalysis.getAspectBeanNames()
        assertThat(aspectBeanNames).contains("loggingAspect", "performanceAspect")
        
        // 캐시된 어드바이저 확인
        val cachedAdvisors = aspectJAdvisorsBuilderAnalysis.getCachedAdvisors()
        assertThat(cachedAdvisors).isNotEmpty
    }
    
    @Test
    fun `실제 스프링과 커스텀 분석 결과 비교`() {
        // 실제 스프링이 생성한 프록시와 비교
        assertThat(AopUtils.isAopProxy(userService)).isTrue
        
        if (userService is Advised) {
            val springAdvisors = userService.advisors
            val customAdvisors = aspectJAdvisorsBuilderAnalysis.buildAspectJAdvisors()
            
            // 어드바이저 개수나 타입이 유사한지 확인
            logger.info("Spring advisors: {}", springAdvisors.size)
            logger.info("Custom advisors: {}", customAdvisors.size)
        }
    }
}
```

#### 2. 애플리케이션 시작 과정 분석
```kotlin
// ApplicationStartupAnalysisTest.kt
@SpringBootTest
class ApplicationStartupAnalysisTest {
    
    @Autowired
    lateinit var applicationStartupTracer: ApplicationStartupTracer
    
    @Autowired
    lateinit var springContextLifecycleTracer: SpringContextLifecycleTracer
    
    @Test
    fun `애플리케이션 시작 과정 분석`() {
        // 애플리케이션이 시작된 후 분석
        val processingHistory = applicationStartupTracer.getProcessingHistory()
        val proxyCreationEvents = applicationStartupTracer.getProxyCreationEvents()
        
        assertThat(processingHistory).isNotEmpty
        assertThat(proxyCreationEvents).isNotEmpty
        
        // 프록시 생성 순서 분석
        val proxyBeans = processingHistory.filter { it.isProxy }
        logger.info("Proxy beans created in order:")
        proxyBeans.forEach { event ->
            logger.info("  {} ({})", event.beanName, event.proxyType)
        }
        
        // 서비스 빈들이 프록시로 생성되었는지 확인
        val serviceBeans = proxyBeans.filter { it.beanName.contains("Service") }
        assertThat(serviceBeans).isNotEmpty
    }
    
    @Test
    fun `컨텍스트 라이프사이클 이벤트 순서 확인`() {
        val lifecycleEvents = springContextLifecycleTracer.getLifecycleEvents()
        
        assertThat(lifecycleEvents).isNotEmpty
        
        // 이벤트 순서가 올바른지 확인
        val eventTypes = lifecycleEvents.map { it.eventType }
        assertThat(eventTypes).contains(
            "BeanFactoryPostProcessor.postProcessBeanFactory",
            "postProcessAfterInitialization",
            "ContextRefreshedEvent"
        )
    }
}
```

#### 3. 디버깅 도구 테스트
```kotlin
// AopDebuggingTest.kt
@SpringBootTest
class AopDebuggingTest {
    
    @Autowired
    lateinit var aopDebuggingUtils: AopDebuggingUtils
    
    @Test
    fun `빈 분석 도구 테스트`() {
        // Phase 2의 서비스 빈 분석
        assertDoesNotThrow {
            aopDebuggingUtils.analyzeBean("userService")
            aopDebuggingUtils.analyzeBean("orderService")
        }
    }
    
    @Test
    fun `모든 프록시 빈 나열 테스트`() {
        assertDoesNotThrow {
            aopDebuggingUtils.listAllProxyBeans()
        }
    }
    
    @Test
    fun `메소드 매칭 분석 테스트`() {
        assertDoesNotThrow {
            aopDebuggingUtils.checkMethodMatching("userService", "createUser")
            aopDebuggingUtils.checkMethodMatching("orderService", "createOrder")
        }
    }
    
    @Test
    fun `프록시 생성 시뮬레이션 테스트`() {
        assertDoesNotThrow {
            aopDebuggingUtils.simulateProxyCreation(UserServiceImpl::class.java)
            aopDebuggingUtils.simulateProxyCreation(OrderService::class.java)
        }
    }
}
```

## 📊 학습 결과 분석 및 최종 정리

### 분석 항목
1. **스프링 AOP의 전체 아키텍처 이해도**
   - 각 컴포넌트의 역할과 상호작용
   - 실제 구현과 이론의 일치도

2. **성능 및 메모리 분석**
   - 프록시 생성으로 인한 시작 시간 증가
   - 런타임 성능 오버헤드
   - 메모리 사용량 분석

3. **실무 적용 가능성**
   - 디버깅 도구의 실용성
   - 트러블슈팅 시나리오 대응
   - 성능 튜닝 포인트 식별

## 🎓 최종 학습 완료 기준

### 체크리스트
- [ ] AbstractAutoProxyCreator 계층 구조 완전 이해
- [ ] AspectJ 어드바이저 생성 과정 분석 완료
- [ ] 애플리케이션 시작 과정의 프록시 생성 흐름 추적 완료
- [ ] 실제 스프링 소스와 동일한 로직 구현 및 검증
- [ ] 디버깅 및 트러블슈팅 도구 세트 완성
- [ ] Phase 1~4까지의 모든 지식 통합 완료

### 최종 성과물
1. **완전한 AOP 메커니즘 이해**: 프록시 → AOP → 빈후처리기 → ProxyFactory → 실제구현
2. **실무 도구 세트**: 디버깅, 분석, 트러블슈팅 도구
3. **성능 최적화 지식**: 실제 운영 환경에서의 고려사항
4. **스프링 내부 동작 원리 습득**: 프레임워크 레벨의 깊은 이해

---
*예상 소요 시간: 6-7일*
*난이도: ⭐⭐⭐⭐⭐*
*이 단계를 완료하면 스프링 AOP의 모든 메커니즘을 마스터하게 됩니다!*