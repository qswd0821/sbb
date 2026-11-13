# 기술적 개선 정리 — 강사 수업(slog 14163·14168) 기준

책(점프 투 스프링부트)은 도구가 오래되어 비교 항목이 너무 많아진다. 그래서 이 문서는 **강사 수업 코드(slog 14163, 14168)를 기준선**으로 놓고, 이 프로젝트에서 **기술적으로 무엇이 달라졌는지**만 정리한다.

- 기준 코드: [jhs512/p-14163-2](https://github.com/jhs512/p-14163-2) (책 2-14까지), [jhs512/p-14168-2](https://github.com/jhs512/p-14168-2) (Post/PostComment, 46강 시큐리티 도입까지)
- 구성
  1. 강사 수업에서 가져와 그대로 적용한 기술
  2. **강사 수업 범위를 넘어 추가한 기술적 개선** ← 핵심
  3. 강사 코드에는 있지만 아직 적용하지 않은 것 (다음 개선 후보)

---

## 1. 강사 수업에서 가져와 적용한 기술

| 기술 | 강사 출처 | 이 프로젝트 커밋 |
|---|---|---|
| yaml + dev/test 프로파일, 테스트는 메모리 DB | 14163 5강 | 002-04, 002-05 |
| SQL·바인딩 값·트랜잭션 로그 | 14163 6강 | 002-06 |
| `mappedBy` 유무에 따른 중간 테이블 실험 | 14163 11·12강 | 002-10, 002-11 |
| `@ManyToOne(fetch = LAZY)`, 컬렉션 `new ArrayList<>()` | 14163 22~25강 | 002-16 |
| `addAnswer()` + `CascadeType.PERSIST` (리포지터리 없이 자식 저장) | 14163 21·37강 | 002-16, 002-23 |
| 샘플 데이터 `BaseInitData`(self 프록시로 `@Transactional`), 테스트 `@Transactional` 롤백 | 14163 14·18강, 14168 2강 | 002-12, 002-14 |
| 커스텀 예외로 404 | 14163 35강 | 002-21 |
| `@Valid` + `BindingResult`, `th:object/th:field`, `#fields` 오류 프래그먼트, `data-error-field-name` | 14168 9~25강 | 002-28 |
| `layout:title-pattern`, 공통 `global.js` | 14168 44·45강 | 002-26, 002-28 |
| `@ModelAttribute` 공통 모델 변수 | 14168 19강 | 003-15, 004-03 |
| `HiddenHttpMethodFilter`(`_method`)로 `@DeleteMapping` | 14168 41강 | 003-11 |
| 시큐리티 도입: 전부 허용으로 시작, H2 콘솔 CSRF·frame 예외 | 14168 46강 | 003-06 |
| cdnjs, 프리텐다드 | 14163 39강 | 002-25 |

---

## 2. 강사 수업 범위를 넘어 추가한 기술적 개선

강사 코드에 없거나(인증·검색·마크다운·조회 수 등은 수업 범위 밖), 같은 문제를 다른 방식으로 푼 것.

### 2-1. 보안

| 개선 | 강사 기준 | 이 프로젝트 | 왜 (기술적 근거) | 커밋 |
|---|---|---|---|---|
| **마크다운 XSS 방지** | (마크다운 없음) | commonmark `escapeHtml(true)` + `sanitizeUrls(true)` 후 `th:utext` | `th:utext` 는 HTML 을 그대로 출력 → 사용자 입력 `<script>`, `javascript:` 링크가 실행될 수 있음. 렌더러 단계에서 무력화 | 003-15 |
| **작성자 권한 검사 + 403** | (인증 없음) | 서버에서 `작성자 == Principal` 검사, 실패 시 `403 FORBIDDEN` | 버튼을 숨겨도 URL 직접 호출은 막을 수 없음. 권한 없음은 400 이 아니라 403 이 HTTP 의미에 맞음 | 003-10 ~ 003-12 |
| **로그아웃 POST + CSRF** | (로그인 없음) | `logoutUrl()` + POST 폼 | GET 로그아웃은 외부 사이트의 `<img src>` 등으로 강제 로그아웃 가능. 시큐리티 7 기본값과도 일치 | 003-08 |
| **상태 변경은 모두 POST/DELETE** | 글·댓글 삭제는 DELETE | 추천도 GET 링크 대신 POST 폼 | 링크 미리보기·크롤러가 GET 을 호출해도 데이터가 바뀌지 않도록, 그리고 CSRF 토큰 검사를 받도록 | 003-13 |
| **삭제 폼을 JS 없이** | `<a method="DELETE">` + `global.js` 가 폼을 동적 생성, `data-onclick` 을 `new Function` 으로 실행 | `<form th:method="delete">` (타임리프가 `_method`·CSRF 자동 삽입) + `onsubmit confirm` | 문자열을 코드로 실행하는 `new Function` 이 없어 CSP 적용이 쉽고, JS 가 꺼져도 동작 | 003-11 |

### 2-2. JPA·쿼리

| 개선 | 강사 기준 | 이 프로젝트 | 왜 | 커밋 |
|---|---|---|---|---|
| **원자적 조회 수 증가** | (조회 수 없음) | `@Modifying` JPQL `viewCount = viewCount + 1` | "읽고-더하고-저장" 은 동시 요청 시 갱신 손실. DB 한 문장으로 증가 | 004-07 |
| **벌크 update 와 1차 캐시 순서** | — | update 를 **먼저** 실행한 뒤 엔티티 조회 | 벌크 update 는 영속성 컨텍스트를 우회 → 먼저 읽어 둔 엔티티는 옛 값 (OSIV 로 요청 내내 같은 컨텍스트) | 004-07 |
| **기존 행이 있는 테이블에 NOT NULL 컬럼 추가** | — | `@ColumnDefault("0")` + `nullable = false` | `ddl-auto: update` 로 컬럼 추가 시 기존 행 때문에 실패하지 않도록 DB 기본값 지정 | 004-07 |
| **자식 컬렉션 전체 로딩 대신 페이징 조회** | `post.getComments()` 전체를 화면에 출력 | 답변은 `findByQuestion(question, Pageable)` 로 5개씩 | 14163 13강 "N 이 커지면 다른 방법" 을 실제 적용. 컬렉션은 등록·삭제(cascade)용으로만 사용 | 004-01 |
| **집계 정렬(추천순)** | — | `left join a.voter v ... group by a order by count(v) desc` + 별도 `countQuery` | 연관 컬렉션 크기는 `Sort` 로 표현 불가. group by 쿼리는 자동 count 쿼리가 틀리므로 직접 지정 | 004-01 |
| **검색 JPQL** | (검색 없음) | 질문·답변·작성자 `left outer join` + `distinct` + `Pageable`, `(:category is null or q.category = :category)` | 조인 조건이 많아 메서드 이름 규칙으로 불가. 답변 수만큼 질문이 중복되는 것을 distinct 로 제거, 선택 조건을 쿼리 하나로 | 003-16, 004-03 |
| **데이터 마이그레이션 벌크 update** | — | 카테고리 도입 시 `category is null` 인 기존 질문을 한 번에 기본 카테고리로 | 엔티티를 하나씩 읽어 수정하면 N 번 조회·수정. 시작 시 1문장, 재실행해도 0건(멱등) | 004-03 |
| **역방향 컬렉션을 두지 않는 조회** | — | 프로필: `SiteUser` 에 `@OneToMany` 없이 `findTop10ByAuthorOrderByCreateDateDesc`, `countByAuthor` | 회원→작성 글은 N 이 무한히 커짐 (14163 13강 케이스 1). 필요한 10개만 LIMIT 조회 | 004-05 |
| **하나의 엔티티가 두 부모 중 하나에 소속** | 댓글은 Post 에만 | `Comment` 가 `question` 또는 `answer` 에 연결, `getQuestionId()` 로 돌아갈 위치 계산, 양쪽 부모에 `cascade REMOVE` | 같은 기능을 엔티티 두 개로 중복 구현하지 않음. 부모 삭제 시 고아 댓글 방지 | 004-02 |

### 2-3. 웹 계층·템플릿

| 개선 | 강사 기준 | 이 프로젝트 | 왜 | 커밋 |
|---|---|---|---|---|
| **Thymeleaf 3.1 의 `@빈` 참조 제한 대응** | `@ModelAttribute` 를 컨트롤러 안에 둠 | `@ControllerAdvice` 로 **모든 컨트롤러**에 `commonUtil`, `categoryList` 제공 | 책 방식 `${@commonUtil.markdown()}` 이 3.1.5 에서 500. 공통 변수는 전역으로 한 번만 | 003-15, 004-03 |
| **오류 포커스 순서를 화면(DOM)에서 결정** | 메시지에 `01-title-...`, `02-content-...` 처럼 순번·필드명을 넣고 `sorted()` 후 `split('-')` 로 분해, 첫 항목에 포커스 | 메시지는 사람이 읽는 문장 그대로 두고, 오류 필드 집합 중 **폼에서 가장 위에 있는 필드**에 포커스 | 둘 다 "검증 순서가 보장되지 않는" 문제를 푼다. 강사 방식은 메시지에 순번·필드명을 관리해야 하고(필드 추가 시 번호 재조정), 이 방식은 화면 배치가 곧 순서라 추가 관리가 없음 | 002-28 |
| **검색·페이징 상태를 URL 에** | (검색 없음) | GET 검색 폼 + `@{/question/list(page=, kw=, category=)}` | 숨은 폼 + JS 제출 없이 검색 결과를 공유·새로고침·뒤로가기 가능 | 003-16 |
| **파라미터 받는 프래그먼트** | 오류 프래그먼트(파라미터 없음) | `commentsFragment(comments, actionUrl)` 를 질문·답변에서 재사용 | 같은 UI 를 데이터만 바꿔 두 곳에 | 004-02 |
| **작은 폼의 실패 처리** | 실패 시 폼 템플릿을 다시 렌더링 | 상세 페이지 안의 댓글 폼은 PRG + flash attribute 로 1회성 메시지 | 상세 페이지 전체(질문·답변 페이징)를 다시 그리지 않아도 됨, 새로고침 시 재전송 없음 | 004-02 |
| **앵커 리다이렉트** | `redirect:/posts/{id}` | `redirect:...#answer_{id}` (등록·수정·추천) | 처리 후 방금 다룬 답변 위치로 스크롤 | 003-14 |
| **속성 하나로 에디터 적용** | — | `textarea[data-markdown-editor]` → `global.js` 가 EasyMDE 로 교체(`forceSync`) | 페이지마다 스크립트를 넣지 않음, 서버 검증·폼 전송은 그대로 | 004-08 |

### 2-4. 초기 데이터·설정

| 개선 | 강사 기준 | 이 프로젝트 | 왜 | 커밋 |
|---|---|---|---|---|
| **공통/개발 전용 초기 데이터 분리** | `BaseInitData` 하나 (`count() > 0` 이면 종료) | `BaseInitData`(공통, `@Order(1)`) + `DevInitData`(`@Profile("dev")`, `@Order(2)`, 300건) | 테스트는 소량·고정 데이터, 개발 화면은 대량 데이터. 실행 순서 보장 | 003-02 |
| **단계별 멱등 초기화** | 데이터가 하나라도 있으면 전체 건너뜀 | 카테고리·질문을 **각각** 검사 | 기존 dev DB 에 새 테이블(카테고리)이 추가돼도 초기 데이터가 채워짐 | 004-03 |

---

## 3. 강사 코드에는 있지만 아직 적용하지 않은 것 (다음 개선 후보)

14168 최종 코드와 비교했을 때 이 프로젝트가 **뒤처진** 부분. 책의 엔티티·URL 구조를 유지하느라 미뤘다.

| 강사(14168) | 이 프로젝트 현재 | 적용 시 효과 |
|---|---|---|
| `BaseEntity` (`@MappedSuperclass`) + JPA Auditing `@CreatedDate`, `@LastModifiedDate` | 엔티티마다 `id`, `createDate`, `modifyDate` 를 직접 선언·세팅 | 중복 제거, 생성·수정 시각 자동 기록 |
| 엔티티 `equals`/`hashCode` (id 기반) | 미정의 | 컬렉션에서 엔티티 비교가 안정적 (14168 37·38강) |
| `@Setter` 없이 도메인 메서드 (`post.modify()`, `addComment()`, `deleteComment()`) + `@NoArgsConstructor` | `@Setter` 로 서비스에서 필드를 직접 변경 | 변경 지점이 엔티티로 모여 규칙(수정일시 등)을 빠뜨리지 않음 |
| 폼 DTO `record` | `class` + Lombok | 불변, 코드량 감소 (14168 40강) |
| REST 스타일 URL `/posts/{id}/comments/{id}/delete`, 수정은 `@PutMapping` | 책 URL `/question/modify/{id}`(POST), `/answer/delete/{id}` | 자원 중심 URL, 메서드로 행위 구분 (14163 37강 표, 14168 26강) |
| `orphanRemoval = true` | `cascade REMOVE` 만 | 컬렉션에서 빼기만 해도 삭제 |
| 정적 파일 `static/resource/**` + favicon 허용 | `static/` 바로 아래 | 시큐리티 접근 제어를 걸 때 경로 하나로 허용 (14168 46강) |
| `domain/<도메인>/<계층>`, `global/<관심사>` 패키지 | `com.mysite.sbb.<도메인>` (책 2-06) | 도메인 + 계층 + 전역 설정 구분 |
