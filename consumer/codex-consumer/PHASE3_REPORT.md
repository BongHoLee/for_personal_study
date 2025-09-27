# Phase 3 진행 보고서 - Kafka Consumer

## 1. 사전 검토 및 조정
- Kafka 관련 설정과 메시지 모델 의존성을 점검한 결과, 수신 메시지를 역직렬화하기 위한 Jackson/Avro 지원과 Listener Ack 모드 설정이 필요함을 확인했습니다.
- 서비스 계층은 Phase 4에서 본격 구현 예정이므로, 이번 단계에서는 로깅 기반 기본 구현을 제공하고 테스트에서는 MockBean으로 대체하도록 방향을 정했습니다.

## 2. 구현 내용
- **KafkaConfig**: `JsonDeserializer`/`ByteArrayDeserializer` 기반 ConsumerFactory와 수동 커밋 Listener Container를 구성하고, 재시도 2회 + 로그 기록을 수행하는 `DefaultErrorHandler`를 도입했습니다.
- **MydataConsentConsumer**: `is_remove=false` 이벤트 필터링 및 멱등 처리를 위한 서비스 호출, 실패 시 ErrorHandler 위임.
- **PayAccountDeletedConsumer**: Avro 메시지를 `PayAccountDeletedDecoder`로 역직렬화 후 서비스 호출.
- **PayAccountDeletedDecoder**: Avro 바이트 스트림을 안전하게 역직렬화하고 실패 시 `MessageDecodingException`으로 감쌈.
- **LoggingMydata/PayTerminateService**: Phase 4 이전까지의 기본 동작을 위해 수신 이벤트를 로그로 남기는 임시 구현 제공.
- **Gradle 및 설정 보완**: Avro 코드 생성 디렉터리 설정, Awaitility/Mockito-Kotlin 의존성 추가, `application.yml`에 Kafka 기본 설정 포함.

## 3. 테스트
- `MydataConsentConsumerTest`: Embedded Kafka 기반으로 `is_remove=true`/`false` 케이스를 검증하여 서비스 위임과 필터링을 확인.
- `PayAccountDeletedConsumerTest`: Avro 직렬화/역직렬화 라운드 트립을 통해 서비스 호출과 null 사유 처리 확인.
- `ConsentMessageTest`, `PayAccountDeletedEnvelopAvroTest`: Phase 2 테스트를 재활용해 메시지 역직렬화 보증.
- 실행 명령: `./gradlew :codex-consumer:test`

## 4. 다음 단계 제안
- Phase 4에서 멱등성 로직을 포함한 `MydataTerminateService`/`PayTerminateService` 실제 구현을 진행하고, 이번 단계에서 작성한 Consumer 테스트가 그대로 통과하는지 회귀 검증을 수행합니다.
