# Presentation Layer DTO 직렬화 가이드

## 개요

이 문서는 V1과 V2 API의 Request/Response DTO 구조와 kotlinx.serialization을 사용한 직렬화 메커니즘을 설명합니다.

## 목차
1. [V1과 V2의 차이점](#v1과-v2의-차이점)
2. [UserIdentifier 통합 설계](#useridentifier-통합-설계)
3. [V2의 type 필드 자동 처리](#v2의-type-필드-자동-처리)
4. [V1의 Flatten 처리](#v1의-flatten-처리)

---

## V1과 V2의 차이점

### V1: CI만 지원 (단순 문자열)

**JSON 형식:**
```json
{
  "api_transaction_id": "tx-123",
  "request_time": "2025-11-04T10:00:00",
  "ci": "ci-value-12345"
}
```

**코드:**
```kotlin
val request = V1RequestBody(
    requestId = "tx-123",
    requestDateTime = "2025-11-04T10:00:00",
    ci = UserIdentifier.CI(value = "ci-value-12345")
)
```

### V2: CI, DI, Email 지원 (객체 형태)

**JSON 형식:**
```json
{
  "api_transaction_id": "tx-123",
  "request_time": "2025-11-04T10:00:00",
  "user_identifier": {
    "type": "CI",
    "value": "ci-value-12345"
  }
}
```

**코드:**
```kotlin
val request = V2RequestBody(
    requestId = "tx-123",
    requestDateTime = "2025-11-04T10:00:00",
    userIdentifier = UserIdentifier.CI(value = "ci-value-12345")
)
```

---

## UserIdentifier 통합 설계

### 설계 원칙

V1과 V2에서 **동일한 `UserIdentifier` 타입**을 사용하여 중복을 제거하고 일관성을 확보합니다.

### 구현 구조

```kotlin
@Serializable
sealed interface UserIdentifier {
    val value: String

    @Serializable
    @SerialName("CI")
    data class CI(override val value: String) : UserIdentifier

    @Serializable
    @SerialName("DI")
    data class DI(override val value: String) : UserIdentifier

    @Serializable
    @SerialName("EMAIL")
    data class Email(override val value: String) : UserIdentifier
}
```

### sealed interface vs sealed class

**sealed interface를 선택한 이유:**

1. **더 자연스러운 구조**
   - data class는 이미 암묵적으로 `Any`를 상속
   - sealed class를 상속하면 불필요한 상속 계층 생성
   - interface는 "계약"을 의미하므로 타입 정의에 더 적합

2. **다중 구현 가능**
   ```kotlin
   // sealed interface: 여러 인터페이스 구현 가능
   data class CI(override val value: String) : UserIdentifier, OtherInterface

   // sealed class: 단일 상속만 가능
   data class CI(override val value: String) : UserIdentifier() // 다른 클래스 상속 불가
   ```

3. **Kotlin 관례**
   - 구현체가 data class일 때는 sealed interface 사용 권장
   - 공통 상태나 로직이 필요할 때만 sealed class 사용

4. **직렬화 동작은 동일**
   - kotlinx.serialization은 sealed class와 sealed interface를 동일하게 처리
   - discriminator 필드 자동 생성 방식 동일
   - @SerialName 동작 동일

### 장점

1. **타입 통일**: V1의 `Ci`와 V2의 `UserIdentifier.CI`를 하나로 통합
2. **중복 제거**: 동일한 의미의 서로 다른 객체가 존재하지 않음
3. **확장성**: 새로운 식별자 타입 추가 시 한 곳에서만 수정
4. **타입 안정성**: sealed class로 컴파일 타임에 타입 체크

### 기술적 구현

**V1에서의 사용:**
```kotlin
@Serializable
data class V1RequestBody(
    @SerialName("ci")
    @Serializable(with = UserIdentifierAsStringSerializer::class)
    val ci: UserIdentifier.CI  // V2와 동일한 타입
)
```

**V2에서의 사용:**
```kotlin
@Serializable
data class V2RequestBody(
    @SerialName("user_identifier")
    val userIdentifier: UserIdentifier  // 기본 Serializer 사용
)
```

---

## V2의 type 필드 자동 처리

### 핵심 개념

V2의 JSON에는 `type`과 `value` 필드가 있지만, **코드에는 `value`만 정의**되어 있습니다.

```kotlin
// 코드 정의 - type 필드 없음!
@Serializable
@SerialName("CI")
data class CI(
    override val value: String  // value만 정의
) : UserIdentifier()
```

```json
// JSON 결과 - type 필드 자동 생성!
{
  "type": "CI",
  "value": "ci-value-12345"
}
```

### kotlinx.serialization의 Sealed 타입 처리 메커니즘

#### 1. Discriminator 필드 자동 생성 (JSON에 추가)

kotlinx.serialization은 sealed 타입(sealed class 또는 sealed interface)을 직렬화할 때 **JSON 출력에 자동으로 discriminator 필드를 추가**합니다.

**중요**: 이 필드는 **JSON에만 존재**하고, Kotlin 코드의 프로퍼티로는 존재하지 않습니다.

```kotlin
// Kotlin 코드 - type 프로퍼티 없음
@Serializable
sealed interface UserIdentifier {
    val value: String  // value만 정의

    @Serializable
    @SerialName("CI")
    data class CI(override val value: String) : UserIdentifier
}

// Kotlin 객체 생성
val ci = UserIdentifier.CI(value = "ci-12345")

// ↓ 직렬화 시

// JSON 출력 - type 필드가 자동으로 추가됨!
{"type": "CI", "value": "ci-12345"}
```

**동작 원리:**
- **기본 discriminator 이름**: `"type"`
- **어디에 추가되는가**: JSON 문자열 출력에 추가됨 (Kotlin 객체가 아님)
- **역할**: 역직렬화 시 어떤 하위 타입으로 복원할지 판단
- **sealed interface와 sealed class 동일하게 동작**

```
┌─────────────────────────────────────────────────────────────┐
│                    직렬화 과정                                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Kotlin 객체                                                 │
│  ┌─────────────────────────┐                                │
│  │ UserIdentifier.CI(      │                                │
│  │   value = "ci-12345"    │  type 프로퍼티 없음!             │
│  │ )                       │                                │
│  └─────────────────────────┘                                │
│            ↓                                                 │
│   kotlinx.serialization                                     │
│   (자동으로 type 필드 추가)                                    │
│            ↓                                                 │
│  JSON 문자열                                                  │
│  ┌─────────────────────────┐                                │
│  │ {                       │                                │
│  │   "type": "CI",         │  ← JSON에만 존재!               │
│  │   "value": "ci-12345"   │                                │
│  │ }                       │                                │
│  └─────────────────────────┘                                │
│                                                             │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   역직렬화 과정                               │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  JSON 문자열                                                  │
│  ┌─────────────────────────┐                                │
│  │ {                       │                                │
│  │   "type": "CI",         │  ← 이 값을 읽음                 │
│  │   "value": "ci-12345"   │                                │
│  │ }                       │                                │
│  └─────────────────────────┘                                │
│            ↓                                                 │
│   kotlinx.serialization                                     │
│   (type 값을 보고 어떤 클래스로 복원할지 판단)                   │
│   "type": "CI" → UserIdentifier.CI로 복원                    │
│            ↓                                                 │
│  Kotlin 객체                                                 │
│  ┌─────────────────────────┐                                │
│  │ UserIdentifier.CI(      │                                │
│  │   value = "ci-12345"    │  type 프로퍼티 없음!             │
│  │ )                       │                                │
│  └─────────────────────────┘                                │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

#### 2. @SerialName의 역할

`@SerialName` 어노테이션은 **discriminator 필드에 들어갈 값**을 지정합니다.

```kotlin
@Serializable
@SerialName("CI")  // ← JSON의 type 필드에 "CI"가 들어감
data class CI(override val value: String) : UserIdentifier()

@Serializable
@SerialName("DI")  // ← JSON의 type 필드에 "DI"가 들어감
data class DI(override val value: String) : UserIdentifier()
```

**동작 원리:**
- **직렬화**: `UserIdentifier.CI` → `{"type": "CI", ...}`
- **역직렬화**: `{"type": "CI", ...}` → `UserIdentifier.CI`로 복원

#### 3. 왜 코드에 type 프로퍼티가 없는가? (충돌 문제)

kotlinx.serialization이 **JSON 출력에 자동으로 type 필드를 추가**하기 때문에, 코드에 type 프로퍼티를 정의하면 충돌이 발생합니다.

##### ❌ 잘못된 방법: 코드에 type 프로퍼티 추가

```kotlin
@Serializable
sealed interface UserIdentifier {
    val value: String

    @Serializable
    @SerialName("CI")
    data class CI(
        val type: String = "CI",  // ❌ type 프로퍼티 정의
        override val value: String
    ) : UserIdentifier
}
```

**직렬화 시도 시:**
```
에러: Sealed class 'CI' cannot be serialized as base class 'UserIdentifier'
because it has property name that conflicts with JSON class discriminator 'type'
```

##### 충돌이 발생하는 이유

```
JSON 출력 단계에서 충돌 발생:

1. Kotlin 객체: CI(type = "CI", value = "ci-12345")
   ↓
2. kotlinx.serialization이 직렬화 시작
   ↓
3. CI 클래스의 프로퍼티를 JSON으로 변환
   - type 프로퍼티 → "type": "CI"  (개발자가 정의한 프로퍼티)
   - value 프로퍼티 → "value": "ci-12345"
   ↓
4. sealed 타입이므로 discriminator 추가 시도
   - discriminator 이름: "type"
   - discriminator 값: "CI" (@SerialName에서 가져옴)
   ↓
5. ❌ 충돌 발생!
   - JSON에 "type" 필드가 이미 존재함 (3번에서 추가됨)
   - kotlinx.serialization이 또 "type"를 추가하려고 함
   - 같은 이름의 필드를 두 번 추가할 수 없음

결과 JSON (충돌로 인해 생성 실패):
{
  "type": "CI",        // ← 프로퍼티에서 온 값
  "type": "CI",        // ← discriminator로 추가하려는 값 (충돌!)
  "value": "ci-12345"
}
```

##### ✅ 올바른 방법: 코드에서 type 프로퍼티 제거

```kotlin
@Serializable
sealed interface UserIdentifier {
    val value: String

    @Serializable
    @SerialName("CI")
    data class CI(
        override val value: String  // ✅ value만 정의
    ) : UserIdentifier
}
```

**직렬화 과정:**
```
1. Kotlin 객체: CI(value = "ci-12345")  // type 프로퍼티 없음
   ↓
2. kotlinx.serialization이 직렬화 시작
   ↓
3. CI 클래스의 프로퍼티를 JSON으로 변환
   - value 프로퍼티 → "value": "ci-12345"
   ↓
4. sealed 타입이므로 discriminator 추가
   - discriminator 이름: "type"
   - discriminator 값: "CI" (@SerialName에서 가져옴)
   ↓
5. ✅ 성공!

결과 JSON:
{
  "type": "CI",        // ← discriminator (kotlinx.serialization이 추가)
  "value": "ci-12345"  // ← 프로퍼티에서 온 값
}
```

##### 핵심 정리

| 항목 | 설명 |
|------|------|
| **JSON의 type 필드 출처** | kotlinx.serialization이 자동으로 추가 (코드의 프로퍼티가 아님) |
| **충돌 발생 시점** | JSON 생성 단계에서 같은 이름의 필드를 중복 추가하려고 할 때 |
| **해결 방법** | 코드에서 type 프로퍼티를 제거하고 @SerialName만 사용 |
| **discriminator 값 지정** | @SerialName("CI")로 지정 |

#### 4. 타입 구분이 필요한 경우

비즈니스 로직에서 타입 구분이 필요하면 **Kotlin의 타입 체크**를 사용합니다.

```kotlin
// ❌ 코드에 type 필드가 없으므로 불가능
// userIdentifier.type

// ✅ when 표현식 사용
when (userIdentifier) {
    is UserIdentifier.CI -> println("CI 타입입니다")
    is UserIdentifier.DI -> println("DI 타입입니다")
    is UserIdentifier.Email -> println("Email 타입입니다")
}
```

### 정리

| 항목 | 설명 |
|------|------|
| **JSON 필드** | `type`, `value` 두 개 존재 |
| **코드 프로퍼티** | `value` 하나만 정의 |
| **type 처리** | kotlinx.serialization이 자동으로 추가/제거 |
| **@SerialName** | discriminator 값 지정 (예: "CI", "DI") |
| **타입 구분** | `when (x) { is CI -> ... }` 사용 |

---

## V1의 Flatten 처리

### 문제 상황

V1과 V2에서 **동일한 타입(`UserIdentifier.CI`)을 사용**하면서도 **JSON 형식은 다르게** 표현해야 합니다.

```kotlin
// 코드는 동일
val ci = UserIdentifier.CI(value = "ci-12345")
```

```json
// V1: 단순 문자열
{ "ci": "ci-12345" }

// V2: 객체 형태
{ "user_identifier": { "type": "CI", "value": "ci-12345" } }
```

### 해결 방법: 커스텀 Serializer

**V1에서만 커스텀 Serializer 적용:**

```kotlin
@Serializable
data class V1RequestBody(
    @SerialName("ci")
    @Serializable(with = UserIdentifierAsStringSerializer::class)
    val ci: UserIdentifier.CI
)
```

### UserIdentifierAsStringSerializer 구현

```kotlin
object UserIdentifierAsStringSerializer : KSerializer<UserIdentifier.CI> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UserIdentifier.CI", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UserIdentifier.CI) {
        // 객체를 단순 문자열로 변환
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): UserIdentifier.CI {
        // 단순 문자열을 객체로 변환
        return UserIdentifier.CI(value = decoder.decodeString())
    }
}
```

### 동작 원리

#### 직렬화 (Kotlin → JSON)

```kotlin
// 코드
UserIdentifier.CI(value = "ci-12345")

// serialize 메서드 호출
encoder.encodeString(value.value)  // "ci-12345"만 추출

// JSON 결과
"ci-12345"  // 객체가 아닌 단순 문자열
```

#### 역직렬화 (JSON → Kotlin)

```json
// JSON
"ci-12345"

// deserialize 메서드 호출
decoder.decodeString()  // "ci-12345" 읽음
↓
UserIdentifier.CI(value = "ci-12345")  // 객체로 복원

// 코드 결과
UserIdentifier.CI(value = "ci-12345")
```

### 장점

1. **타입 통일**: V1과 V2 모두 `UserIdentifier.CI` 사용
2. **JSON 유연성**: API 버전별로 다른 JSON 형식 지원
3. **타입 안정성**: 코드에서는 타입이 명확하게 보장됨
4. **유지보수성**: UserIdentifier 로직이 한 곳에 집중

---

## 전체 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                      UserIdentifier                         │
│                  (sealed interface)                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                 │
│  │    CI    │  │    DI    │  │  Email   │                 │
│  └──────────┘  └──────────┘  └──────────┘                 │
│  (data class)  (data class)  (data class)                  │
└─────────────────────────────────────────────────────────────┘
                    ↓                    ↓
        ┌───────────────────┐  ┌───────────────────┐
        │   V1RequestBody   │  │   V2RequestBody   │
        │                   │  │                   │
        │  ci: CI           │  │  userIdentifier:  │
        │  (String 형태)     │  │  UserIdentifier   │
        │                   │  │  (Object 형태)     │
        └───────────────────┘  └───────────────────┘
                    ↓                    ↓
        UserIdentifier        기본 Serializer
        AsStringSerializer    (자동 type 추가)
                    ↓                    ↓
        ┌───────────────────┐  ┌───────────────────┐
        │  V1 JSON          │  │  V2 JSON          │
        │  {                │  │  {                │
        │    "ci": "value"  │  │    "user_id...": {│
        │  }                │  │      "type": "CI",│
        │                   │  │      "value": "..." │
        │                   │  │    }              │
        │                   │  │  }                │
        └───────────────────┘  └───────────────────┘
```

## 핵심 포인트 정리

### 1. sealed interface 선택
- ✅ sealed class보다 더 유연한 구조
- ✅ data class 구현체와 자연스럽게 조합
- ✅ 다중 인터페이스 구현 가능
- ✅ 직렬화 동작은 sealed class와 동일

### 2. V1과 V2의 타입 통일
- ✅ 동일한 `UserIdentifier.CI` 타입 사용
- ✅ 중복 타입(`Ci`, `CI`) 제거
- ✅ 일관된 비즈니스 로직 처리

### 3. V2의 type 필드 자동 처리
- ✅ `type` 프로퍼티를 코드에 정의하지 않음
- ✅ kotlinx.serialization이 sealed 타입을 위해 자동 생성
- ✅ `@SerialName`으로 discriminator 값만 지정

### 4. V1의 Flatten 처리
- ✅ 커스텀 Serializer로 객체를 문자열로 변환
- ✅ API 버전별로 다른 JSON 형식 지원
- ✅ 타입 안정성과 JSON 간결성 동시 확보

### 5. 유지보수성
- ✅ UserIdentifier는 한 곳에서만 정의
- ✅ 새로운 식별자 추가 시 sealed interface에만 추가
- ✅ V1, V2 모두 자동으로 지원 가능

---

## 참고 자료

- [kotlinx.serialization 공식 문서](https://github.com/Kotlin/kotlinx.serialization)
- [Sealed Classes Polymorphism](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md)
- [Custom Serializers](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serializers.md)
