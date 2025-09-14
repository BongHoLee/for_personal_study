# Phase 1: 프록시 기초 학습

## 🎯 학습 목표

- JDK 동적 프록시와 CGLIB 프록시의 기본 원리 이해
- 두 방식의 차이점과 장단점 비교
- 실제 코드로 프록시 생성과 동작 과정 체험

## 📚 이론 학습 내용

### 1. 프록시 패턴 기초

- 프록시 패턴의 개념과 목적
  - 대리인 객체(Proxy 객체)를 통해 **실제 객체에 대한 접근**을 제어한다.
  - 원본 객체의 호출의 결과 값 등을 직접적으로 '변경'하지 않는다.
  - 주요 예시: 인증/인가 확인, lazy loading,  캐싱, 로깅, 트랜잭션 관리 등
- 정적 프록시 vs 동적 프록시
  - **정적 프록시**
    - 컴파일 시점에 프록시 클래스를 직접 작성하거나 코드 생성도구로 만들어둔다.
    - 프록시와 원본이 같은 인터페이스를 구현한다.
    - 장점 : 구조가 단순하고 동작에 대한 예측이 쉽다. 코드레벨에서 직접 작성하기 때문에 명확하게 파악이 가능하다.
    - 단점 : 동일한 횡단 관심사(로깅 등)를 여러 클래스에 적용하려면 각각의 프록시 클래스를 만들어야 한다. 원본 클래스 마다 프록시 클래스를 만들어야 한다.
  - **동적 프록시**
    - `런타임 시점`에 리플렉션, 바이트코드 조작을 통해 **프록시 객체를 자동 생성**한다.
    - JDK 동적 프록시 : 인터페이스 기반
    - CGLIB 프록시 : 클래스 상속 기반
    - 장점: 프록시 클래스를 직접 작성할 필요가 없고, 여러 클래스에 동일한 횡단 관심사를 쉽게 적용할 수 있다.
    - 단점: 리플렉션/바이트코드 조작에 따라 성능 오버헤드가 발생하고, 디버깅이 어려울 수 있다.

- 프록시가 해결하는 대표적인 문제들
  - 로깅/모니터링 → 메소드 호출 시간 측정, 호출 횟수 로깅
  - 트랜잭션 관리 → 메소드 실행 전/후에 트랜잭션 열고 닫기
  - 보안/권한 제어 → 접근 권한이 없는 경우 차단
  - 지연 로딩 (Lazy Loading) → 실제 객체 필요할 때 생성
  - 캐싱 → 동일 요청 결과를 캐시에서 반환

### 2. JDK 동적 프록시

- InvocationHandler 인터페이스
- Proxy.newProxyInstance() 메소드
- 인터페이스 기반 프록시의 특징과 제한사항
  - 실제 객체와 프록시가 구현하기 위한 공통의 인터페이스가 반드시 필요하다.
  - `InvocationHandler`를 활용한다. 클라이언트가 메서드를 호출하면 `InvocationHandler.invoke(proxy, method, args)`로 위임된다.
  - 개발자는 이 메서드 안에서 부가기능을 넣거나 원본 객체의 메서드를 `method.invoke(target, args)`로 호출한다.
  - JDK 표준 라이브러리에 포함되기 때문에 별도 의존성이 필요하지 않다.
  - Reflaction API 기반이다.
  - Spring AOP는 대상 Bean이 인터페이스를 구현한 경우에는 JDK 동적 프록시를 우선으로 사용한다.

### 3. CGLIB 프록시

- 상속 기반 프록시 생성
- MethodInterceptor 인터페이스
- 클래스 기반 프록시의 특징과 제한사항
  - CGLIB는 `바이트 코드를 조작`해서 대상 클래스를 상속한 서브클래스를 생성한다.
  - 인터페이스 없이도 프록시 생성이 가능하다. -> 개발자가 인터페이스를 직접 둘 필요가 없다.
  - 클라이언트가 메서드를 호출하면 `MethodInterceptor.intercept(obj, method, args, proxy)`로 위임된다.
  - `proxy.invokeSuper(obj, args)`를 호출하면 원본 메서드가 호출된다.
  - Spring AOP는 인터페이스가 없을 때 CGLIB를 이용해서 프록시 객체를 생성한다.
  - Hibernate, Spring, Mockito 같은 곳에서 엔티티/빈 등의 객체 프록시 생성에 활용한다.

## 🛠 실습 과제

### 과제 1: JDK 동적 프록시 구현

**파일 위치**: `src/main/kotlin/bong/training/phase1/jdk/`

#### 1-1. 기본 서비스 인터페이스 및 구현체

```kotlin
// UserService.kt - 인터페이스
interface UserService {
    fun findUser(id: Long): String
    fun saveUser(name: String): Long
    fun deleteUser(id: Long): Boolean
}

// UserServiceImpl.kt - 구현체
class UserServiceImpl : UserService {
    override fun findUser(id: Long): String = "User-$id"
    override fun saveUser(name: String): Long = System.currentTimeMillis()
    override fun deleteUser(id: Long): Boolean = true
}
```

#### 1-2. JDK 동적 프록시 구현

```kotlin
// LoggingInvocationHandler.kt
class LoggingInvocationHandler(private val target: Any) : InvocationHandler {
    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
        // 메소드 실행 전 로그
        // 메소드 실행
        // 메소드 실행 후 로그
        // 실행 시간 측정
    }
}

// ProxyFactory.kt
class JdkProxyFactory {
    companion object {
        fun <T> createProxy(target: T, targetClass: Class<T>): T {
            // Proxy.newProxyInstance() 사용
        }
    }
}
```

### 과제 2: CGLIB 프록시 구현

**파일 위치**: `src/main/kotlin/bong/training/phase1/cglib/`

#### 2-1. CGLIB용 서비스 클래스 (인터페이스 없음)

```kotlin
// OrderService.kt - 구체 클래스
open class OrderService {
    open fun createOrder(productId: Long, quantity: Int): String {
        return "Order created: product=$productId, qty=$quantity"
    }
  
    open fun cancelOrder(orderId: String): Boolean {
        return true
    }
}
```

#### 2-2. CGLIB 프록시 구현

```kotlin
// LoggingMethodInterceptor.kt
class LoggingMethodInterceptor(private val target: Any) : MethodInterceptor {
    override fun intercept(obj: Any?, method: Method?, args: Array<out Any>?, proxy: MethodProxy?): Any? {
        // 메소드 실행 전 처리
        // proxy.invokeSuper() 또는 method.invoke() 사용
        // 메소드 실행 후 처리
    }
}

// CglibProxyFactory.kt
class CglibProxyFactory {
    companion object {
        fun <T> createProxy(targetClass: Class<T>): T {
            // Enhancer 사용하여 프록시 생성
        }
    }
}
```

### 과제 3: 프록시 비교 및 분석

**파일 위치**: `src/main/kotlin/bong/training/phase1/comparison/`

```kotlin
// ProxyComparison.kt
class ProxyComparison {
    fun compareProxyTypes() {
        // JDK vs CGLIB 성능 비교
        // 메모리 사용량 비교
        // 제약사항 비교
    }
}
```

## 🧪 테스트 계획

### 테스트 파일 위치

- `src/test/kotlin/bong/training/phase1/`

### 테스트 시나리오

#### 1. JDK 동적 프록시 테스트

```kotlin
// JdkProxyTest.kt
class JdkProxyTest {
    @Test
    fun `JDK 프록시 생성 및 메소드 호출 테스트`()
  
    @Test
    fun `로깅 기능 정상 동작 확인`()
  
    @Test
    fun `실행 시간 측정 기능 확인`()
  
    @Test
    fun `예외 발생 시 프록시 동작 확인`()
}
```

#### 2. CGLIB 프록시 테스트

```kotlin
// CglibProxyTest.kt
class CglibProxyTest {
    @Test
    fun `CGLIB 프록시 생성 및 메소드 호출 테스트`()
  
    @Test
    fun `final 메소드 처리 확인`()
  
    @Test
    fun `생성자 매개변수가 있는 클래스 프록시 생성`()
}
```

#### 3. 프록시 비교 테스트

```kotlin
// ProxyComparisonTest.kt
class ProxyComparisonTest {
    @Test
    fun `JDK vs CGLIB 성능 비교`()
  
    @Test
    fun `인터페이스 유무에 따른 프록시 선택`()
  
    @Test
    fun `프록시 생성 시간 비교`()
}
```

## 📊 학습 결과 분석

### 분석 항목

1. **성능 비교**

   - 프록시 생성 시간
   - 메소드 호출 오버헤드
   - 메모리 사용량
2. **제약사항 분석**

   - JDK 프록시: 인터페이스 필수
   - CGLIB: final 클래스/메소드 제한
   - 생성자 매개변수 처리
3. **사용 시점 결정 기준**

   - 언제 JDK 프록시를 사용할지
   - 언제 CGLIB 프록시를 사용할지
   - 스프링이 선택하는 기준

## 🎓 학습 완료 기준

### 체크리스트

- [ ]  JDK 동적 프록시 구현 완료
- [ ]  CGLIB 프록시 구현 완료
- [ ]  모든 테스트 케이스 통과
- [ ]  성능 비교 분석 완료
- [ ]  두 방식의 차이점 명확히 이해
- [ ]  실제 프록시 객체 디버깅으로 내부 구조 확인

### 다음 단계 준비

- 스프링 AOP 어노테이션 학습 준비
- Aspect, Advice 개념 예습
- 프록시 기반 AOP 동작 원리 연결고리 파악

---

*예상 소요 시간: 3-4일*
*난이도: ⭐⭐☆☆☆*
