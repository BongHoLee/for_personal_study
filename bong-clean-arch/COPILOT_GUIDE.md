# GitHub Copilot (Claude Sonnet 4.5) 고급 활용 가이드

VS Code에서 GitHub Copilot의 고급 기능과 실용적인 팁을 정리한 문서입니다.

---

## � 핵심 고급 기능

### 1. **파일 참조 문법 (#)**

특정 파일을 명시적으로 참조할 때 `#` 사용:

```
"#Account.kt의 withdraw 메서드를 #Money.kt의 subtract와 연동해줘"
"#CLAUDE.md의 패키지 구조를 참고해서 새 Bounded Context를 만들어줘"
```

**자동완성 지원:**
- `#` 입력 후 파일명 입력 시 자동완성 제공
- 파일 경로를 정확히 몰라도 파일명만으로 참조 가능

**범위 참조:**
```
"#Account.kt:10-30 의 코드를 리팩토링해줘"  // 특정 라인 범위
"#AccountService.kt#deposit 메서드를 분석해줘"  // 특정 심볼
```

### 2. **에이전트 (@ Commands)**

#### `@workspace` - 전체 프로젝트 맥락
```
"@workspace 에서 Money 클래스가 사용되는 모든 곳을 찾아줘"
"@workspace 의 도메인 모델 구조를 분석해줘"
"@workspace 에서 TODO가 남아있는 부분을 정리해줘"
```

#### `@vscode` - VS Code 설정 및 기능
```
"@vscode Kotlin 포맷팅 설정을 어떻게 바꾸지?"
"@vscode 단축키로 멀티커서를 어떻게 쓰지?"
```

#### `@terminal` - 터미널 출력 참조
```
"@terminal 의 에러 메시지를 분석하고 해결해줘"
"@terminal 의 테스트 실패 로그를 보고 원인을 찾아줘"
```

#### `@github` - GitHub 관련 작업 (활성화 시)
```
"@github 이 이슈를 해결하는 PR 제목과 본문을 작성해줘"
"@github main 브랜치와의 차이점을 요약해줘"
```

### 3. **슬래시 명령어 (/ Commands)**

Chat에서 `/` 입력 시 사용 가능한 빠른 명령어:

#### `/explain` - 코드 설명
```
(코드 선택 후)
/explain
→ 선택한 코드의 동작 원리를 자세히 설명
```

#### `/fix` - 문제 해결
```
(에러가 있는 코드 선택 후)
/fix
→ 자동으로 문제를 감지하고 수정 제안
```

#### `/tests` - 테스트 생성
```
(함수/클래스 선택 후)
/tests
→ Kotest 스타일의 테스트 자동 생성
```

#### `/doc` - 문서화
```
(함수/클래스 선택 후)
/doc
→ KDoc 주석 자동 생성
```

#### `/new` - 새 파일 생성
```
/new Repository 인터페이스
→ 프로젝트 구조에 맞게 파일 생성
```

#### `/search` - 검색
```
/search 예외 처리 패턴
→ 워크스페이스 전체에서 관련 코드 검색
```

### 4. **컨텍스트 변수 (#...)**

특수한 컨텍스트를 참조할 때:

#### `#selection` - 현재 선택된 코드
```
"#selection 을 Builder 패턴으로 변경해줘"
```

#### `#editor` - 현재 열린 파일
```
"#editor 의 테스트 커버리지를 확인해줘"
```

#### `#codebase` - 전체 코드베이스
```
"#codebase 에서 유사한 패턴을 찾아줘"
```

#### `#file` - 특정 파일
```
"#file:src/main/kotlin/com/bong/account/domain/Account.kt 를 분석해줘"
```

### 5. **스마트 액션 (Quick Actions)**

코드 위에서 전구💡 아이콘 또는 `Cmd+.`:

- **Copilot 수정 제안** - AI가 자동으로 개선 제안
- **Copilot 설명** - 현재 라인/블록 설명
- **Copilot 테스트 생성** - 단위 테스트 자동 생성

### 6. **인라인 채팅 고급 활용 (`Cmd+I`)**

코드 블록을 선택하고 `Cmd+I`:

```kotlin
// 코드 선택 후 Cmd+I
fun deposit(amount: Money) {
    this.balance = this.balance.add(amount)
}

// 인라인 채팅에서:
"입금 한도 검증 추가하고, 도메인 이벤트 발행해줘"
```

**고급 패턴:**
```
"이 메서드를 @Transaction 처리하고, 로깅 추가, 예외 처리 보완해줘"
"이 클래스의 모든 메서드에 Null 체크 추가해줘"
"SOLID 원칙에 맞게 책임을 분리해줘"
```

## 🎯 실전 활용 패턴

### 멀티 파일 작업 패턴

**여러 파일을 동시에 참조하며 작업:**
```
"#Account.kt의 withdraw를 #TransferService.kt에서 호출하고,
 #AccountRepository.kt에 필요한 메서드를 추가해줘"
```

**파일 + 범위 + 심볼 조합:**
```
"#Account.kt:50-100 의 로직을 #Money.kt의 값 객체 패턴을 따라 리팩토링해줘"
```

### 컨벤션 기반 작업

**프로젝트 문서 활용:**
```
"#CLAUDE.md의 아키텍처 규칙을 따라 Payment Bounded Context를 생성해줘"
"#guideline.md의 네이밍 규칙에 맞게 #AccountService.kt를 수정해줘"
"#도메인모델.md를 참고해서 Order 애그리게이트를 설계해줘"
```

### 에러 해결 워크플로우

```
1단계: "@terminal 의 에러 로그 확인"
2단계: "@workspace 에서 유사한 해결 사례 찾기"
3단계: "#file:문제파일.kt 수정"
4단계: "테스트로 검증"
```

---

## � 프로 팁 & 숨겨진 기능

### 1. **Participants (참여자) 활용**

특정 도메인 전문가처럼 동작하도록 지시:

```
"DDD 전문가 관점에서 #Account.kt를 리뷰해줘"
"시큐리티 전문가로서 인증 로직의 취약점을 찾아줘"
"성능 최적화 전문가로서 #AccountRepository.kt를 분석해줘"
```

### 2. **체인 프롬프트 (Chained Prompts)**

이전 대화의 결과를 활용한 연속 작업:

```
대화 1: "@workspace 에서 모든 Service 클래스를 찾아줘"
대화 2: "찾은 Service들의 공통 패턴을 추출해줘"
대화 3: "공통 패턴을 BaseService 추상 클래스로 만들어줘"
대화 4: "각 Service가 BaseService를 상속하도록 리팩토링해줘"
```

### 3. **조건부 작업**

```
"#Account.kt에서:
 - 만약 @Transactional이 없으면 추가하고
 - 로깅이 없으면 추가하고
 - 예외 처리가 없으면 추가해줘"
```

### 4. **비교 분석**

```
"#AccountService.kt와 #OrderService.kt의 차이점을 분석하고
 일관성 있게 통일해줘"

"#Account.kt:v1.0과 현재 버전을 비교해서 변경사항을 설명해줘"
```

### 5. **배치 작업**

```
"src/main/kotlin/com/bong 하위의 모든 Service 클래스에:
 1. @Service 어노테이션 확인
 2. 생성자 주입 패턴 적용
 3. KDoc 주석 추가"
```

### 6. **템플릿 기반 생성**

```
"#AccountService.kt를 템플릿으로 PaymentService를 생성해줘.
 Payment 도메인에 맞게 조정하고, 테스트도 함께 만들어줘"
```

### 7. **컨텍스트 파일 활용**

프로젝트 루트에 `.github/copilot-instructions.md` 생성:
```markdown
# Copilot Instructions

## 프로젝트 컨벤션
- 모든 도메인 객체는 불변(immutable)으로 작성
- 테스트는 Kotest의 BehaviorSpec 사용
- 패키지 구조는 Hexagonal Architecture 준수

## 금지 사항
- 도메인 레이어에서 Spring 어노테이션 사용 금지
- mutable 상태 금지
- getter/setter 금지
```

이 파일은 Copilot이 자동으로 읽어서 모든 응답에 반영합니다.

### 8. **스니펫 참조**

```
"@workspace 에서 'Repository 구현 패턴'을 찾아서
 새로운 PaymentRepository에 적용해줘"
```

### 9. **디버깅 모드**

```
"#AccountService.kt:45에서 NPE가 발생해.
 1. 스택 트레이스 분석
 2. null 발생 지점 추적
 3. 방어 코드 추가
 4. 테스트 케이스 추가"
```

### 10. **아키텍처 가드**

```
"@workspace 에서 도메인 레이어가 인프라 레이어를 의존하는 곳을 찾아줘"
"Hexagonal Architecture 원칙을 위반하는 의존성을 모두 찾아줘"
```

---

## � 커스터마이징 & 자동화

### VS Code Settings에 단축 명령어 추가

`.vscode/settings.json`:
```json
{
  "github.copilot.chat.quickActions": [
    {
      "title": "DDD 리뷰",
      "prompt": "이 코드를 DDD 관점에서 리뷰하고 개선점을 제시해줘"
    },
    {
      "title": "Kotest 테스트 생성",
      "prompt": "이 코드의 Kotest BehaviorSpec 테스트를 생성해줘"
    },
    {
      "title": "아키텍처 검증",
      "prompt": "이 코드가 Hexagonal Architecture를 준수하는지 검증해줘"
    }
  ]
}
```

### Keybindings 커스터마이징

`.vscode/keybindings.json`:
```json
[
  {
    "key": "cmd+shift+t",
    "command": "github.copilot.chat.quickChat",
    "args": "/tests"
  },
  {
    "key": "cmd+shift+d",
    "command": "github.copilot.chat.quickChat",
    "args": "/doc"
  }
]
```

### Task 자동화와 연동

```
"./gradlew test를 실행하고, 실패한 테스트가 있으면 분석해서 수정해줘"
"빌드 에러를 확인하고 자동으로 수정해줘"
```

---

## 🎨 고급 시나리오 예제

### 시나리오 1: TDD 워크플로우
```
Step 1: "/tests AccountService의 transfer 메서드"
Step 2: "실패하는 테스트를 먼저 작성했어. 이제 구현해줘"
Step 3: "@terminal 테스트 결과 확인"
Step 4: "실패한 케이스를 수정해줘"
```

### 시나리오 2: 리팩토링 파이프라인
```
"#AccountService.kt를:
 1. 먼저 현재 구조를 분석하고
 2. SOLID 위반 사항을 찾고
 3. 리팩토링 계획을 세우고
 4. 단계별로 리팩토링하고
 5. 각 단계마다 테스트로 검증해줘"
```

### 시나리오 3: 새 기능 추가 (End-to-End)
```
"Transfer 기능을 추가할거야. #CLAUDE.md의 아키텍처를 따라서:

1. 도메인 분석
   - @workspace 에서 유사한 도메인 이벤트 패턴 찾기
   - Transfer 도메인 설계

2. 계층별 구현
   - Domain: TransferEvent, Transfer 값 객체
   - Application: TransferUseCase (Port), TransferService
   - Adapter: TransferController, TransferRepository

3. 테스트
   - 각 계층별 단위 테스트 (Kotest)
   - 통합 테스트 시나리오

4. 검증
   - 아키텍처 의존성 규칙 준수 확인
   - 테스트 커버리지 확인"
```

### 시나리오 4: 버그 헌팅
```
"@workspace 에서:
 - 예외 처리가 누락된 메서드
 - Null 체크가 없는 코드
 - 트랜잭션 처리가 필요한데 없는 곳
 모두 찾아서 리포트해줘"
```

### 시나리오 5: 문서 자동화
```
"@workspace 의 도메인 모델들을:
 1. 클래스 다이어그램 (Mermaid)
 2. API 명세서 (OpenAPI)
 3. 시퀀스 다이어그램 (주요 유스케이스)
 모두 docs/ 폴더에 생성해줘"
```

---

## ⚡ 성능 최적화 팁

### 1. 컨텍스트 범위 최적화
```
❌ "@workspace 전체를 분석해줘" (느림)
✅ "@workspace/src/main/kotlin/com/bong/account 만 분석해줘" (빠름)
```

### 2. 명시적 파일 참조
```
❌ "Account 클래스 찾아서 수정해줘" (검색 필요)
✅ "#Account.kt 수정해줘" (직접 접근)
```

### 3. 점진적 작업
```
❌ "전체 프로젝트 리팩토링" (한 번에 너무 많음)
✅ "Account 도메인만 먼저 리팩토링" (단계적)
```

---

## � 주의사항 & Best Practices

### 하지 말아야 할 것
```
❌ "모든 파일 다 고쳐줘"
❌ "완벽한 코드 만들어줘"
❌ 컨텍스트 없이 "이거 왜 안돼?"
```

### 해야 할 것
```
✅ "#Account.kt의 withdraw 메서드에 검증 로직 추가"
✅ "@terminal 에러를 보고 #AccountTest.kt 수정"
✅ "#CLAUDE.md 규칙을 따라 새 기능 추가"
```

### 컨텍스트 관리
- 대화가 길어지면 새 채팅 시작 (컨텍스트 초기화)
- 중요한 프로젝트 규칙은 `.github/copilot-instructions.md`에 저장
- 자주 참조하는 문서는 프로젝트 루트에 배치

---

## � 추가 리소스

### 공식 문서
- [GitHub Copilot Docs](https://docs.github.com/copilot)
- [VS Code Copilot Guide](https://code.visualstudio.com/docs/copilot/overview)

### 프로젝트 특화 문서
- `#CLAUDE.md` - 프로젝트 개요 및 아키텍처
- `#guideline.md` - 코딩 컨벤션
- `#도메인모델.md` - 도메인 설계
- `.github/copilot-instructions.md` - Copilot 커스텀 지침

---

## 🎓 학습 곡선

1. **입문** (1-2일)
   - 기본 채팅, `/` 명령어 사용
   - `#` 파일 참조

2. **중급** (1주)
   - `@` 에이전트 활용
   - 인라인 채팅 (`Cmd+I`)
   - 멀티 파일 작업

3. **고급** (2-4주)
   - 체인 프롬프트
   - 커스텀 지침 설정
   - 자동화 워크플로우

---

**마지막 팁:** Copilot은 도구입니다. 결과를 항상 리뷰하고, 이해하고, 필요시 수정하세요! 🚀