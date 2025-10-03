# 디렉토리 구조
```
project-root/
├── CLAUDE.md                    # ⭐ 프로젝트 메모리 (자동 로드)
├── CLAUDE.local.md              # 개인 설정 (gitignore)
├── .claude/
│   ├── commands/                # 커스텀 slash 커맨드
│   │   ├── review.md
│   │   ├── next-checkpoint.md
│   │   └── challenge.md
│   └── settings.json            # Claude Code 설정
├── docs/
│   ├── adr/                     # Architecture Decision Records
│   │   ├── 001-hexagonal.md
│   │   └── 002-cache-strategy.md
│   ├── checkpoints/             # 체크포인트 계획들
│   │   ├── current.md          # 현재 진행 중
│   │   └── completed/
│   │       └── cp1-complete.md
│   ├── feedback/                # 피드백 루프 기록
│   │   ├── 2025-10-03-cache.md
│   │   └── template.md
│   └── learning/
│       ├── log.md              # 학습 일지
│       └── challenges.md       # 챌린지 목록
└── src/                        # 실제 코드
```
---

# 커뮤니케이션 사이클 - 핵심 요약

## 전체 흐름 (1 체크포인트 = 1.5시간)

```
📋 PLAN (15분)
   ↓
💻 IMPLEMENT (45분) 
   ↓
🔍 REVIEW (10분)
   ↓
💬 REFLECT (20분)
   ↓
📝 LEARNING (10분)
   ↓
➡️ NEXT (5분)
```

---

## Phase별 핵심 대화 패턴

### 1. PLAN - 방향 설정

```bash
$ claude
```

**개발자:**
```
@CLAUDE.md
CP4 시작. Redis 캐시 추가.

질문:
1. CachePort 인터페이스 설계?
2. 캐시를 어느 레이어에서?
3. 테스트 전략?

각각 2-3개 옵션 줘.
```

**Claude:**
```
[각 질문마다 옵션 제시 + 추천]
```

**개발자:**
```
Option B로 간다.
/next-checkpoint CP4
```

→ `docs/checkpoints/current.md` 자동 생성

---

### 2. IMPLEMENT - 개발자 작업

```
(Claude와 대화 없음)

- CachePort 인터페이스 작성
- RedisCacheAdapter 구현
- CachedOrderRepository 작성
- 테스트 작성 & 통과 확인
```

---

### 3. REVIEW - 피드백 요청

```bash
$ claude
```

**개발자:**
```
/review Redis 캐싱 구현 완료

특히 확인해줘:
1. Order를 직접 직렬화했는데 괜찮나?
2. 캐시 키 전략 (버저닝 없음)
3. CP5 Kafka 추가 대비 구조
```

**Claude:**
```markdown
## 피드백

1. 직렬화 ⚠️ 개선 필요
   - 문제: 스키마 변경 시 캐시 깨짐
   - 해결: CacheDto 분리
   
2. 캐시 키 🔧 개선 권장
   - 버저닝 추가 ("order:v1:123")
   - KeyStrategy 클래스 분리

3. CP5 대비 ✅ 준비됨

우선순위:
- 🔥 1번 (CacheDto) - 지금 반영
- 🟡 2번 (KeyStrategy) - 선택
```

→ `docs/feedback/2025-10-03-cache.md` 자동 저장

---

### 4. REFLECT - 토론 & 반영

**개발자:**
```
@docs/feedback/2025-10-03-cache.md

질문:
- CacheDto 변환 오버헤드는?
- 1번만 반영하고 2번은 나중에 해도 될까?
```

**Claude:**
```
변환 오버헤드: 0.01ms (무시 가능)

추천: 1번(CacheDto) + 2번(KeyStrategy) 둘 다
이유: CP5에서 패턴 삭제 필요
```

**개발자:**
```
알겠어. 둘 다 반영한다.
(30분 작업)

/review 반영 완료
```

**Claude:**
```
✅ 완료 인정!
```

---

### 5. LEARNING - 학습 기록

**개발자:**
```
# CacheDto 분리 - 도메인 변경 시 캐시 안전성
```

**Claude:**
```
learning-log.md에 저장?
```

**개발자:**
```
ㅇㅇ
```

→ `docs/learning/log.md` 자동 업데이트

---

### 6. NEXT - 다음 단계

**개발자:**
```
/next-checkpoint CP4 complete
```

**Claude:**
```
CP5 계획:
- Kafka 이벤트 발행
- 캐시 무효화 이벤트 기반 전환

첫 질문:
1. Domain Event vs Integration Event?
2. Kafka 호출 레이어?

다음 세션에서 시작!
```

→ `docs/checkpoints/current.md` 업데이트

---

## 피드백 루프 (REVIEW ↔ REFLECT 반복)

```
구현
  ↓
/review ──→ 피드백 받음
  ↓
질문/토론 ──→ 답변
  ↓
반영 결정
  ↓
재구현
  ↓
/review ──→ 재검토
  ↓
✅ 승인
```

**핵심:** 피드백은 여러 번 반복 가능. 완성도 높일 때까지.

---

## 핵심 명령어

| 상황 | 명령어 | 결과 |
|------|--------|------|
| 계획 시작 | `@CLAUDE.md` + 질문 | 옵션 제시 |
| 체크포인트 저장 | `/next-checkpoint CPX` | current.md 생성 |
| 피드백 요청 | `/review 구현 내용` | 구조화된 리뷰 |
| 학습 기록 | `# 배운 내용` | log.md 저장 |
| 다음 단계 | `/next-checkpoint complete` | 다음 CP 계획 |

---

## 실제 사용 예시 (초압축)

```bash
# 시작
$ claude
"@CLAUDE.md CP4 시작, 옵션 줘"
[Claude: 옵션 A/B/C]
"B로 간다. /next-checkpoint CP4"

# (구현 45분)

# 리뷰
$ claude  
"/review Redis 완료, 1.직렬화 2.키전략 확인"
[Claude: CacheDto 분리 필요]
"질문: 오버헤드는?" 
[Claude: 무시 가능]
"반영한다"

# (수정 30분)

"/review 반영완료"
[Claude: ✅ 승인]

# 정리
"# CacheDto 분리로 스키마 안전성 확보"
[Claude: log.md 저장]

"/next-checkpoint CP4 complete"
[Claude: CP5 계획 생성]
```

**1 사이클 = 1.5시간**

