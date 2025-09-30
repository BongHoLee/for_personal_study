# Kafka Consumer 설정 및 에러 핸들링 학습 정리

## 1. ConsumerFactory 설정의 중복성과 우선순위

### 문제 상황
KafkaConfig에서 Deserializer 설정이 여러 곳에서 중복되어 나타나는 현상을 발견했습니다.

### 중복되는 설정들
1. **제네릭 타입**: `ConsumerFactory<String, ConsentMessage>`
2. **Properties 설정**: `ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG`
3. **Constructor 파라미터**: `DefaultKafkaConsumerFactory(props, keyDeserializer, valueDeserializer)`

### 실제 동작 원리

#### 제네릭 타입의 역할
```kotlin
ConsumerFactory<String, ConsentMessage>
```
- **역할**: 컴파일 타임 타입 힌트만 제공
- **런타임 영향**: 없음 (타입 소거로 인해)
- **목적**: 코드 가독성, IDE 지원, 컴파일 타임 안전성

#### Properties vs Constructor 우선순위
```kotlin
// Properties 설정
props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java

// Constructor 파라미터 (이것이 우선 적용됨!)
DefaultKafkaConsumerFactory(props, StringDeserializer(), JsonDeserializer(ConsentMessage::class.java))
```

**우선순위**: Constructor 파라미터 > Properties 설정

### 권장 설정 방식

#### 방법 1: Constructor만 사용 (현재 적용)
```kotlin
return DefaultKafkaConsumerFactory(
    propsWithoutDeserializers,
    StringDeserializer(),
    JsonDeserializer(ConsentMessage::class.java)
)
```

#### 방법 2: Properties만 사용
```kotlin
return DefaultKafkaConsumerFactory<String, ConsentMessage>(props)
```

### 핵심 인사이트
- **실제 타입 결정은 Deserializer가 담당**
- **제네릭 타입은 명확성을 위한 것일 뿐**
- **중복 설정 시 Constructor 파라미터가 우선 적용됨**

---

## 2. Kafka 에러 핸들링 및 재시도 메커니즘

### 요구사항
메시지 처리 중 예외 발생 시 3회 재시도 후 로그 남기기

### 구현된 에러 핸들링 전략

#### DefaultErrorHandler 사용
```kotlin
@Bean
fun kafkaErrorHandler(): DefaultErrorHandler {
    val fixedBackOff = FixedBackOff(1000L, 3L) // 1초 간격, 3회 재시도
    val errorHandler = DefaultErrorHandler(fixedBackOff)
    
    // 재시도해도 의미없는 예외는 제외
    errorHandler.addNotRetryableExceptions(
        JsonProcessingException::class.java,
        IllegalArgumentException::class.java
    )
    
    // 재시도 및 최종 실패 로깅
    errorHandler.setRetryListeners(retryListener)
    
    return errorHandler
}
```

#### Container Factory에 적용
```kotlin
factory.setCommonErrorHandler(kafkaErrorHandler())
```

### 로깅 전략

#### 재시도 중 로깅 (WARN 레벨)
```
Retry attempt 1 failed for topic: mydata.consent.v1, partition: 0, offset: 123, key: user123
```

#### 최종 실패 로깅 (ERROR 레벨)
```
All retries exhausted for topic: mydata.consent.v1, partition: 0, offset: 123, key: user123. Message will be skipped.
```

### 재시도 정책 옵션

#### 고정 간격 재시도
```kotlin
FixedBackOff(1000L, 3L) // 1초 간격, 3회
```

#### 지수 백오프 재시도
```kotlin
ExponentialBackOff().apply {
    initialInterval = 1000L
    multiplier = 2.0
    maxInterval = 10000L
    maxElapsedTime = 30000L
}
```

### 예외 분류 전략

#### 재시도 가능한 예외
- 네트워크 연결 오류
- 데이터베이스 일시적 장애
- 외부 API 호출 실패

#### 재시도 불가능한 예외 (즉시 스킵)
- JSON 파싱 에러 (`JsonProcessingException`)
- 잘못된 인자 (`IllegalArgumentException`)
- 비즈니스 로직 검증 실패

---

## 3. 학습된 핵심 개념

### Spring Kafka의 설정 계층
1. **Framework Level**: Spring Kafka 설정
2. **Consumer Factory Level**: Deserializer, Properties
3. **Listener Container Level**: Error Handler, Concurrency
4. **Message Handler Level**: 실제 비즈니스 로직

### 에러 핸들링의 모범 사례
1. **예외 분류**: 재시도 가능/불가능 구분
2. **적절한 백오프**: 시스템 부하 고려
3. **상세한 로깅**: 디버깅 및 모니터링 지원
4. **DLQ 고려**: 최종 실패 메시지 처리 방안

### 설정 중복 제거의 중요성
- 코드 가독성 향상
- 설정 충돌 방지
- 유지보수성 개선
- 의도 명확화

---

## 4. 향후 개선 방향

### 에러 핸들링 확장
```kotlin
// DLQ(Dead Letter Queue) 전송
override fun recovered(record: ConsumerRecord<*, *>, ex: Exception) {
    log.error("Sending to DLQ: {}", record.key())
    dlqProducer.send(record)
}
```

### 메트릭 수집
```kotlin
// 재시도 횟수, 실패율 등 메트릭 수집
meterRegistry.counter("kafka.consumer.retry", "topic", record.topic()).increment()
```

### 알림 시스템 연동
```kotlin
// 중요한 메시지 처리 실패 시 알림
if (isCriticalMessage(record)) {
    alertService.sendAlert("Critical message processing failed", record)
}
```

이번 학습을 통해 Kafka Consumer 설정의 실제 동작 원리와 효과적인 에러 핸들링 전략에 대해 깊이 있게 이해할 수 있었습니다.