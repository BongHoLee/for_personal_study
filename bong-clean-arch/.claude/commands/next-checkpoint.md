---
description: 다음 체크포인트 계획
---

# Next Checkpoint

인자: $ARGUMENTS

## 실행 모드

### 1. PLAN 단계 (CP 시작)
인자가 CP 번호인 경우 (예: `CP4`, `CP2-redis`)

**동작:**
- `docs/checkpoints/current.md` 생성/업데이트
- 학습자가 선택한 방향을 기록

**포함 내용:**
```markdown
# CP번호: [제목]

## 학습 목표
- ...

## 구현 범위
- ...

## 설계 고민 포인트
- ...

## 완료 기준
- ...
```

### 2. NEXT 단계 (CP 완료)
인자가 `complete`인 경우

**동작:**
1. 현재 CP 완료도 평가
2. `docs/checkpoints/current.md` → `docs/checkpoints/completed/[CP명].md` 이동
3. 다음 CP 계획 제시
4. 새로운 `current.md` 생성

**참고 파일:**
- 학습 로그: @docs/learning/log.md
- 최근 피드백: @docs/feedback/