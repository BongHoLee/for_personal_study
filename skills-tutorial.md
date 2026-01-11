# Claude Code Skills 튜토리얼

> 제로베이스에서 시작하는 Skills 완벽 가이드

---

## 목차

1. [Step 1: Skills 개념 이해](#step-1-skills-개념-이해)
2. [Step 2: 환경 설정](#step-2-환경-설정)
3. [Step 3: 첫 번째 Skill 만들기](#step-3-첫-번째-skill-만들기)
4. [Step 4: 실용적인 Skill 만들기](#step-4-실용적인-skill-만들기)
5. [Step 5: 고급 기능](#step-5-고급-기능)

---

## Step 1: Skills 개념 이해

### Skills가 뭔가요?

**Skills**는 Claude에게 "특정 작업을 이렇게 해"라고 가르치는 **마크다운 문서**입니다.

쉽게 말해:
- 반복적으로 하는 요청을 자동화하는 "커스텀 지시사항"
- Claude가 상황에 맞게 **자동으로 선택**해서 사용

### Skills로 할 수 있는 것들

| 예시 | 설명 |
|------|------|
| 코드 리뷰 | 팀의 코드 스타일에 맞춰 리뷰하기 |
| 커밋 메시지 | 회사 컨벤션에 맞는 커밋 메시지 생성 |
| 문서화 | 특정 포맷으로 문서 작성 |
| 테스트 작성 | 팀이 선호하는 테스트 패턴 적용 |
| API 설계 | 회사의 API 스타일 가이드 준수 |

### Skills의 동작 원리

```
1. 발견 (Discovery)
   └── Claude가 시작할 때 Skill 이름/설명만 가볍게 로드

2. 활성화 (Activation)
   └── 사용자 요청이 Skill 설명과 일치하면 전체 내용 로드
   └── "이 Skill을 사용할까요?" 권한 요청

3. 실행 (Execution)
   └── Skill의 지시사항에 따라 작업 수행
```

**핵심**: `/skill이름` 같은 명령어 없이, Claude가 **알아서 판단**합니다.

### CLAUDE.md vs Skills

| 구분 | CLAUDE.md | Skills |
|------|-----------|--------|
| **로드 시점** | 항상 자동 로드 | 필요할 때만 로드 |
| **용도** | 프로젝트 전체 설정 | 특정 작업 전문화 |
| **예시** | "이 프로젝트는 TypeScript 사용" | "PR 리뷰는 이렇게 해" |

**비유**:
- CLAUDE.md = 회사 전체 규칙 (복장, 출퇴근 등)
- Skills = 특정 업무 매뉴얼 (고객 응대, 보고서 작성 등)

---

## Step 2: 환경 설정

### Skills 폴더 위치

Skills는 두 가지 위치에 저장할 수 있습니다:

```bash
# 1. 개인용 (모든 프로젝트에서 사용)
~/.claude/skills/

# 2. 프로젝트용 (해당 프로젝트에서만 사용)
.claude/skills/
```

### 폴더 구조

```
~/.claude/skills/
├── my-skill-1/           # Skill 1
│   └── SKILL.md          # 필수 파일
├── my-skill-2/           # Skill 2
│   ├── SKILL.md          # 필수 파일
│   └── reference.md      # 선택: 참고 자료
└── my-skill-3/           # Skill 3
    ├── SKILL.md
    └── scripts/
        └── helper.py     # 선택: 유틸리티 스크립트
```

**규칙**:
- 각 Skill은 **별도 폴더**에 저장
- 폴더 안에 **SKILL.md** 파일 필수
- 폴더 이름은 자유롭게 (소문자, 하이픈 권장)

### SKILL.md 파일 형식

```markdown
---
name: skill-name
description: 이 Skill이 무엇을 하고, 언제 사용하는지 설명
---

# Skill 제목

여기에 Claude가 따를 지시사항을 작성합니다.
```

**YAML 헤더 필드**:

| 필드 | 필수 | 설명 |
|------|------|------|
| `name` | O | Skill 고유 이름 (소문자, 하이픈만) |
| `description` | O | Skill 설명 (Claude가 이걸 보고 판단) |
| `allowed-tools` | X | 사용 가능한 도구 제한 |
| `user-invocable` | X | 슬래시 메뉴 표시 여부 (기본: true) |

### 폴더 생성하기

터미널에서 실행:

```bash
# 개인용 Skills 폴더 생성
mkdir -p ~/.claude/skills

# 확인
ls ~/.claude/
```

---

## Step 3: 첫 번째 Skill 만들기

### 목표

"인사해줘"라고 하면 특별한 방식으로 인사하는 Skill 만들기

### 3-1. 폴더 생성

```bash
mkdir -p ~/.claude/skills/greeting
```

### 3-2. SKILL.md 작성

`~/.claude/skills/greeting/SKILL.md` 파일 생성:

```markdown
---
name: greeting
description: 사용자에게 인사할 때 사용. "인사", "안녕", "hello" 같은 요청에 반응.
---

# Greeting Skill

사용자에게 인사할 때는 다음 형식을 따르세요:

1. 먼저 재미있는 이모지로 시작
2. 사용자의 이름이 있다면 이름을 부르며 인사
3. 오늘의 날짜를 언급
4. 간단한 프로그래밍 관련 명언 하나 추가

## 예시

> 👋 안녕하세요, 개발자님!
> 오늘은 2024년 1월 15일이네요.
>
> 💡 오늘의 명언: "좋은 코드는 그 자체로 최고의 문서다." - Steve McConnell
```

### 3-3. 동작 확인

1. **새 Claude Code 세션 시작** (기존 세션에서는 인식 안 될 수 있음)
2. "인사해줘" 또는 "안녕" 입력
3. Claude가 Skill 사용 권한을 요청하면 승인
4. 특별한 형식의 인사 확인!

### 3-4. 트리거 조건 이해

Claude는 `description`을 보고 Skill 사용 여부를 판단합니다.

```yaml
# 좋은 description
description: 사용자에게 인사할 때 사용. "인사", "안녕", "hello" 같은 요청에 반응.

# 나쁜 description
description: 인사 관련 기능
```

**팁**: description에 **트리거 키워드**를 명시하면 정확도가 올라갑니다.

---

## Step 4: 실용적인 Skill 만들기

### 예제 1: 코드 리뷰 Skill

`~/.claude/skills/code-review/SKILL.md`:

```markdown
---
name: code-review
description: 코드 리뷰, PR 리뷰, 코드 검토 요청 시 사용. "리뷰해줘", "코드 봐줘" 같은 요청에 반응.
allowed-tools: Read, Grep, Glob
---

# Code Review Skill

코드를 리뷰할 때는 다음 체크리스트를 따르세요:

## 체크리스트

### 1. 가독성
- [ ] 변수/함수 이름이 명확한가?
- [ ] 복잡한 로직에 주석이 있는가?
- [ ] 함수가 한 가지 일만 하는가?

### 2. 버그 가능성
- [ ] null/undefined 처리가 되어 있는가?
- [ ] 에러 핸들링이 적절한가?
- [ ] 엣지 케이스를 고려했는가?

### 3. 성능
- [ ] 불필요한 반복이 없는가?
- [ ] 메모리 누수 가능성이 없는가?

### 4. 보안
- [ ] 사용자 입력을 검증하는가?
- [ ] 민감한 정보가 노출되지 않는가?

## 출력 형식

각 항목에 대해:
- ✅ 좋음: 잘된 점 설명
- ⚠️ 개선 필요: 문제점과 해결 방안
- ❌ 심각: 반드시 수정 필요한 부분
```

### 예제 2: 커밋 메시지 Skill

`~/.claude/skills/commit-message/SKILL.md`:

```markdown
---
name: commit-message
description: 커밋 메시지 작성, git commit 관련 요청 시 사용. "커밋 메시지", "commit message" 요청에 반응.
allowed-tools: Bash
---

# Commit Message Skill

커밋 메시지는 Conventional Commits 형식을 따릅니다.

## 형식

```
<type>(<scope>): <subject>

<body>

<footer>
```

## Type 종류

| Type | 설명 |
|------|------|
| feat | 새로운 기능 |
| fix | 버그 수정 |
| docs | 문서 변경 |
| style | 코드 포맷팅 (기능 변경 없음) |
| refactor | 리팩토링 |
| test | 테스트 추가/수정 |
| chore | 빌드, 설정 변경 |

## 규칙

1. subject는 50자 이내
2. subject는 명령형으로 ("Add feature" O, "Added feature" X)
3. body는 72자에서 줄바꿈
4. body에는 "왜" 변경했는지 설명

## 예시

```
feat(auth): Add JWT token refresh mechanism

기존 토큰 만료 시 사용자가 다시 로그인해야 하는 불편함 해소.
Refresh token을 이용해 자동으로 access token 갱신.

Closes #123
```
```

### 예제 3: Kotlin 문서화 Skill

`~/.claude/skills/kotlin-docs/SKILL.md`:

```markdown
---
name: kotlin-docs
description: Kotlin 코드 문서화, KDoc 작성 요청 시 사용. "문서화", "KDoc", "주석 달아줘" 요청에 반응.
allowed-tools: Read, Edit
---

# Kotlin Documentation Skill

Kotlin 코드에 KDoc 주석을 작성합니다.

## KDoc 형식

```kotlin
/**
 * 함수/클래스에 대한 간단한 설명.
 *
 * 더 자세한 설명이 필요하면 여기에 작성합니다.
 *
 * @param paramName 파라미터 설명
 * @return 반환값 설명
 * @throws ExceptionType 예외 발생 조건
 * @see RelatedClass 관련 참조
 * @sample com.example.sampleFunction
 */
```

## 규칙

1. 첫 줄은 간결한 요약 (마침표로 끝)
2. 모든 public API에 문서화 필수
3. @param은 파라미터 순서대로
4. 코드 예시가 있으면 @sample 사용

## 예시

```kotlin
/**
 * 사용자 계정을 생성합니다.
 *
 * 이메일 중복 검사를 수행하고, 비밀번호는 암호화하여 저장합니다.
 *
 * @param email 사용자 이메일 (유효한 이메일 형식이어야 함)
 * @param password 비밀번호 (8자 이상)
 * @return 생성된 계정의 ID
 * @throws DuplicateEmailException 이미 존재하는 이메일인 경우
 */
fun createAccount(email: String, password: String): Long
```
```

### allowed-tools 설정

Skill이 사용할 수 있는 도구를 제한할 수 있습니다:

```yaml
# 읽기만 가능
allowed-tools: Read, Grep, Glob

# 편집도 가능
allowed-tools: Read, Edit, Write

# 터미널 명령어 가능
allowed-tools: Bash, Read
```

**팁**: 보안을 위해 필요한 도구만 허용하세요.

---

## Step 5: 고급 기능

### 5-1. 다중 파일 구조

Skill이 복잡해지면 여러 파일로 분리합니다:

```
api-design/
├── SKILL.md           # 핵심 지시사항
├── rest-guidelines.md # REST API 가이드
├── examples.md        # 예시 모음
└── scripts/
    └── validate.sh    # 검증 스크립트
```

**SKILL.md에서 참조**:

```markdown
---
name: api-design
description: REST API 설계 시 사용
---

# API Design Skill

기본 원칙은 이 문서를 따릅니다.

상세 가이드라인이 필요하면 [rest-guidelines.md](rest-guidelines.md)를 참조하세요.
실제 예시는 [examples.md](examples.md)에서 확인할 수 있습니다.
```

**장점**:
- SKILL.md는 가볍게 유지 (500줄 이하 권장)
- 필요한 정보만 선택적으로 로드
- 컨텍스트 윈도우 효율적 사용

### 5-2. 유틸리티 스크립트 연동

Skill에서 스크립트를 실행할 수 있습니다:

```
lint-helper/
├── SKILL.md
└── scripts/
    └── run-lint.sh
```

**SKILL.md**:

```markdown
---
name: lint-helper
description: 린트 검사 및 자동 수정 요청 시 사용
allowed-tools: Bash, Read
---

# Lint Helper Skill

린트 검사가 필요하면 다음 스크립트를 실행하세요:

```bash
~/.claude/skills/lint-helper/scripts/run-lint.sh
```
```

**scripts/run-lint.sh**:

```bash
#!/bin/bash
npm run lint -- --fix
```

### 5-3. 트러블슈팅

#### Skill이 인식되지 않을 때

1. **새 세션 시작**: Claude Code를 다시 시작
2. **파일 위치 확인**: `~/.claude/skills/폴더명/SKILL.md`
3. **YAML 문법 확인**: `---` 구분자가 정확한지

```bash
# 구조 확인
ls -la ~/.claude/skills/
ls -la ~/.claude/skills/my-skill/
```

#### Skill이 트리거되지 않을 때

1. **description 개선**: 트리거 키워드를 명확하게
2. **명시적 요청**: "코드 리뷰 Skill을 사용해서 리뷰해줘"

#### Skill 내용이 무시될 때

1. **지시사항 단순화**: 너무 복잡하면 일부만 적용될 수 있음
2. **우선순위 명시**: "반드시", "항상" 같은 강조 표현 사용

---

## 부록: 빠른 참조

### SKILL.md 템플릿

```markdown
---
name: my-skill
description: 이 Skill의 용도. "키워드1", "키워드2" 요청에 반응.
allowed-tools: Read, Grep
---

# Skill 제목

## 목적
이 Skill이 해결하는 문제

## 지시사항
1. 첫 번째 단계
2. 두 번째 단계
3. 세 번째 단계

## 출력 형식
결과물의 형식 정의

## 예시
구체적인 예시
```

### 자주 쓰는 allowed-tools 조합

```yaml
# 읽기 전용
allowed-tools: Read, Grep, Glob

# 코드 수정
allowed-tools: Read, Edit, Write

# 전체 (제한 없음)
# allowed-tools를 생략하면 모든 도구 사용 가능
```

### 체크리스트

- [ ] `~/.claude/skills/` 폴더 생성됨
- [ ] Skill 폴더 안에 `SKILL.md` 파일 존재
- [ ] YAML 헤더에 `name`과 `description` 포함
- [ ] `description`에 트리거 키워드 명시
- [ ] 새 Claude Code 세션에서 테스트

---

## 다음 단계

이 튜토리얼을 완료했다면:

1. **직접 만들어보기**: 자신만의 Skill을 만들어보세요
2. **팀과 공유**: `.claude/skills/`에 넣어 프로젝트에 커밋
3. **Plugin으로 발전**: 여러 Skill을 Plugin으로 패키징

---

*이 튜토리얼은 Claude Code Skills 학습을 위해 작성되었습니다.*
