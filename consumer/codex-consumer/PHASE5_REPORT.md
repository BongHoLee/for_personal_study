# Phase 5 진행 보고서 - 설정 완성

## 1. 사전 검토 및 조정
- Kafka Listener 세부 설정과 재시도 정책이 고정 값으로 하드코딩되어 있어 향후 운영 환경에서 유연하게 조정하기 어렵다는 점을 확인했습니다.
- MySQL 접속 설정은 application.yml에 존재하지 않아 Hikari 풀 튜닝이나 프로파일별 설정 적용이 곤란했고, Avro 관련 설정 또한 분산되어 있었습니다.

## 2. 구현 내용
- **KafkaConsumerProperties**를 도입하여 리스너 동시성, Ack 모드, 재시도 간격/횟수, Avro specific reader 여부를 외부 설정으로 제어 가능하도록 구성했습니다 (`codex-consumer/src/main/kotlin/com/codex/consumer/config/KafkaConsumerProperties.kt:1`).
- **KafkaConfig**가 새 프로퍼티를 주입받아 Listener 컨테이너 동시성·Ack 모드와 `DefaultErrorHandler`의 FixedBackOff 값을 동적으로 적용하며, Avro specific reader 설정을 Consumer/Producer 팩토리에 전달하도록 개선했습니다 (`codex-consumer/src/main/kotlin/com/codex/consumer/config/KafkaConfig.kt:24`).
- `application.yml`에 Kafka 커스텀 프로퍼티와 Hikari 풀 파라미터(풀 이름, 최대 커넥션, Timeouts 등)를 추가해 기본 실행 환경에서도 연결 설정을 중앙 집중화했습니다 (`codex-consumer/src/main/resources/application.yml:1`).

## 3. 테스트
- `KafkaConfigTest`: 프로퍼티 바인딩, Listener 동시성/ACK 모드, ErrorHandler 타입, Consumer Factory 역직렬화 설정까지 검증 (`codex-consumer/src/test/kotlin/com/codex/consumer/config/KafkaConfigTest.kt:1`).
- `DataSourceConfigurationTest`: Hikari 풀 이름과 커넥션 풀 크기 프로퍼티가 적용되는지 확인 (`codex-consumer/src/test/kotlin/com/codex/consumer/config/DataSourceConfigurationTest.kt:1`).
- 전체 스위트 실행 명령: `../gradlew :codex-consumer:test`

## 4. 다음 단계 제안
- Phase 6에서는 Docker Compose/.env와 Spring 프로파일을 연계하여 환경별 데이터베이스·Kafka 설정을 분리하고, 동시성/재시도 설정이 운영 환경에서도 안전하게 작동하는지 통합 검증을 진행하는 것이 좋습니다.
