# 책 기준 구현 vs 개선 구현 정리

점프 투 스프링부트(책)의 각 단원을 **책 그대로 구현한 부분**과 **장희성 강사 수업(14163, 14168) 등을 근거로 바꿔 구현한 부분**으로 나눠 정리한다.

- **책 그대로**: 책의 설계·코드 흐름을 따른 것 (버전 차이로 인한 문법 변경은 포함)
- **개선**: 책과 다르게 만든 것 + 이유 + 출처
- 커밋 번호는 README `## 작업내역`, GitHub 태그(`/tree/<번호>`)와 같다.

---

## 한눈에 보기

| 영역 | 책 그대로 | 개선 |
|---|---|---|
| 빌드·설정 | Spring Web, Lombok, DevTools, H2, JPA, Thymeleaf, Validation, Security, commonmark | Kotlin DSL, `application.yaml`, dev/test 프로파일, SQL·바인딩 로그 |
| 엔티티 | Question, Answer, SiteUser, `@ManyToOne`, `@OneToMany(mappedBy, REMOVE)`, `@ManyToMany` voter | 전 연관관계 `LAZY`, 컬렉션 초기화, `addAnswer()` + `PERSIST`, `Optional` 조회, `BaseEntity`+Auditing, equals/hashCode, `orphanRemoval`, Setter 제거·도메인 메서드 (005) |
| 테스트 | 리포지터리 CRUD 테스트 | 테스트용 메모리 DB, 샘플 데이터 자동 생성, 테스트별 롤백, 테스트 클래스 분리 |
| 화면 | 부트스트랩, 레이아웃, 내비게이션 바, 페이징, 폼 오류 표시 | cdnjs, 프리텐다드, 페이지별 title, `global.js`, 첫 오류 필드 포커스 |
| HTTP | 컨트롤러 구성 (URL 은 005-05 에서 REST 로 변경) | `/questions/{id}/...` REST URL, 수정은 `PUT`, 삭제는 `DELETE`, 추천·로그아웃은 POST, 권한 없음은 403, 검색은 JS 없는 GET |
| 보안 | 회원가입, 로그인, `@PreAuthorize`, 작성자 확인 | 로그아웃 POST+CSRF, 마크다운 XSS 방지 |
| 3-14 | 9개 기능 중 7개 + 비밀번호 변경 | 각 기능 구현 방식은 아래 표 참고 |

---

## 1장 개발 준비 (001-01 ~ 001-05)

| 책 그대로 | 개선 |
|---|---|
| 스프링부트 프로젝트 생성 (`com.mysite.sbb`, `SbbApplication`) | `build.gradle` → **`build.gradle.kts`(Kotlin DSL)**, STS → 인텔리제이 (14163 2강) |
| 의존성: Spring Web, Lombok, DevTools | Spring Boot 4.1.2 / Java 25 / Gradle 9.8.2 (강사 최신 실습 환경) |
| `HelloController` (`/hello` → Hello World) | git 저장소 + README 작업내역 일지, 커밋 번호 태그 |
| | `HelloLombok` 예제 클래스는 생략 (빌드 성공으로 Lombok 동작 확인) |

## 2장 기본 기능 (002-01 ~ 002-29)

### 2-02 간단한 웹 프로그램
| 책 그대로 | 개선 |
|---|---|
| `MainController` `/sbb`, void → 오류 → `@ResponseBody` 문자열 응답 | 404(매핑 없음) / 500(void) / 200 을 **직접 띄워 비교**, 500 원인(`Circular view path`) 분석 (14163 3·4강) |

### 2-03 JPA, H2
| 책 그대로 | 개선 |
|---|---|
| H2, Spring Data JPA, `ddl-auto: update`, H2 콘솔 | `application.properties` → **`application.yaml`** (14163 5강) |
| | DB 위치 `~/local` → **`./db_dev`** (프로젝트별, 빈 파일 직접 생성 불필요) |
| | **dev / test 프로파일 분리**, 테스트는 `jdbc:h2:mem:db_test` 메모리 DB (14163 5강) |
| | `H2Dialect` 지정 생략 (하이버네이트 자동 감지) |
| | `show-sql`, `format_sql`, 바인딩 값·트랜잭션 TRACE 로그 (14163 6강) |
| | Boot 4 의 별도 모듈 `spring-boot-h2console` |

### 2-04 엔티티
| 책 그대로 | 개선 |
|---|---|
| `Question`(subject 200, content TEXT, createDate), `Answer`, `@ManyToOne Question` | `@ManyToOne` 이 FK 를 만드는 것을 SQL 로그로 확인 (14163 7강) |
| `@OneToMany(mappedBy = "question", cascade = REMOVE) List<Answer> answerList` | **mappedBy 를 뺐을 때 중간 테이블이 생기는 실험** 후 적용 (14163 11·12강, 커밋 002-10 → 002-11) |

### 2-05 리포지터리
| 책 그대로 | 개선 |
|---|---|
| `QuestionRepository`, `AnswerRepository`, `findBySubject`, `findBySubjectAndContent`, `findBySubjectLike` | 단건 조회 리턴 `Question` → **`Optional<Question>`** (14163 16강) |
| 저장·조회·수정·삭제·답변 저장/조회 테스트 | `SbbApplicationTests` 하나 → **`QuestionRepositoryTest`** 로 분리 (14163 15강) |
| | 테스트 안에서 save → **샘플 데이터 자동 생성(`BaseInitData`)** (14163 14강) |
| | 테스트 클래스 **`@Transactional` 롤백**으로 테스트 간 독립성 (14163 18강) |
| | `@ManyToOne(fetch = LAZY)`, `answerList = new ArrayList<>()`, `addAnswer()` + `CascadeType.PERSIST` (14163 21~25강, 커밋 002-16) |

### 2-06 ~ 2-12 도메인 분류, 목록, 서비스, 상세, 답변
| 책 그대로 | 개선 |
|---|---|
| `question`, `answer` 패키지 분리 | |
| `/question/list` 타임리프 목록, `Model` | |
| `/` → 목록 리다이렉트 | |
| `QuestionService` 도입 | |
| `/question/detail/{id}`, `DataNotFoundException`(404) | 404 처리 근거를 `No value present`(500) 와 비교 (14163 35강) |
| `@RequestMapping("/question")` 프리픽스 | |
| `POST /answer/create/{id}`, 상세에 답변 목록 | 답변 저장을 `answerRepository.save` 대신 **`question.addAnswer()`** (14163 37강) |
| | 처리 후 302 리다이렉트(PRG) 이유 정리 (14168 28강) |

### 2-13 ~ 2-16 디자인, 레이아웃, 질문 등록
| 책 그대로 | 개선 |
|---|---|
| `static/style.css` | |
| 부트스트랩 테이블·카드 UI | 부트스트랩 파일 다운로드 → **cdnjs 링크**, **프리텐다드** 폰트 (14163 39강) |
| `layout.html` + `layout:decorate` / `layout:fragment` | **`layout:title-pattern`** 으로 페이지별 title (14168 45강) |
| `QuestionForm`, `AnswerForm`, `@Valid` + `BindingResult`, `form_errors.html` | 오류에 `data-error-field-name` → **`global.js` 가 첫 오류 필드에 포커스** (14168 15·24·25강) |
| `th:object`, `th:field` 로 입력값 유지 | 서버 오류 순서가 일정하지 않아 **폼 필드 순서 기준**으로 포커스 (자체 개선) |

## 3장 서비스 개발 (003-01 ~ 003-16)

| 단원 | 책 그대로 | 개선 |
|---|---|---|
| 3-01 내비게이션 바 | `navbar.html` 프래그먼트 | |
| 3-02 페이징 | `Page`, `PageRequest`, 최신순, 이전/다음/번호 | 300개 샘플은 테스트 코드 대신 **dev 전용 `DevInitData`** (테스트는 메모리 DB 라 화면에 안 보임) |
| 3-03 번호 | 전체 개수 - (페이지×크기) - 인덱스 | |
| 3-04 답변 수 | 목록 제목 옆 답변 수 | |
| 3-05 시큐리티 | `SecurityFilterChain`, H2 콘솔 CSRF·frame 예외 | 처음엔 모두 허용으로 도입, `th:action` 의 CSRF 자동 삽입 확인 (14168 46강) |
| 3-06 회원가입 | `SiteUser`, BCrypt, 비밀번호 불일치·중복 처리 | |
| 3-07 로그인 | `UserDetailsService`, `formLogin`, `sec:authorize` | **로그아웃 GET 링크 → POST 폼 + CSRF** (시큐리티 7 기본, 강제 로그아웃 방지) |
| 3-08 글쓴이 | `author`, `Principal`, `@PreAuthorize` | `author` 도 `LAZY` (14163 25강 원칙 유지) |
| 3-09 수정·삭제 | 작성자 확인, `modifyDate`, 폼 재사용 | 권한 없음 400 → **403**, 삭제 GET 링크 → **`DELETE` 메서드(`_method`) + CSRF** (14168 41강, 14163 37강) |
| 3-10 추천 | `@ManyToMany Set<SiteUser> voter` | 추천 GET 링크 → **POST 폼 + CSRF** |
| 3-11 앵커 | `#answer_{id}` 리다이렉트 | |
| 3-12 마크다운 | commonmark, `th:utext` | **HTML escape·위험 URL 제거로 XSS 방지**, `${@commonUtil...}` 이 Thymeleaf 3.1 에서 500 → **`@ControllerAdvice` 공통 모델 변수** (14168 19강) |
| 3-13 검색 | `@Query` JPQL (질문·답변·글쓴이 left join, distinct) | 숨은 폼 + JS 제출 → **GET 검색 폼 + `@{...(page, kw)}` 링크** (URL 공유·새로고침 가능) |

## 3-14 추가 기능 (004-01 ~ 004-09)

책은 기능 목록만 제시하고 구현은 독자에게 맡긴다. 아래는 구현 방식.

| 기능 | 구현 | 설계상 선택과 이유 |
|---|---|---|
| 답변 페이징·정렬 | 5개씩, 최신순/추천순 | 추천순은 `group by` + `count(voter)` JPQL. `answerList` 전체 대신 필요한 5개만 조회 |
| 댓글 | `Comment`(질문 또는 답변), 등록·삭제 | 질문/답변 공통 **파라미터 프래그먼트**, 실패 시 flash 메시지, 상위 글 삭제 시 함께 삭제 |
| 카테고리 | 질문답변·강좌·자유게시판, 목록 필터, 메뉴 | 기존 질문은 `질문답변` 으로 **일괄 update**, 메뉴는 공통 모델 변수 |
| 비밀번호 변경 | 현재 비밀번호 확인 후 변경 | `passwordEncoder.matches` 로 해시 비교 |
| 비밀번호 찾기 | ⏸ 보류 | SMTP 메일 계정 필요 |
| 프로필 | 기본 정보, 활동 수, 최근 질문·답변·댓글 | 회원→글 `@OneToMany` 를 두지 않고 리포지터리 `Top10` 조회 (14163 13강 케이스 1) |
| 최근 활동 | 최근 답변·댓글 10개 | |
| 조회 수 | 상세 진입 시 +1 | `viewCount + 1` **DB 일괄 update** (동시 조회 시 누락 방지), 증가 후 조회해 1차 캐시 문제 회피 |
| 소셜 로그인 | ⏸ 보류 | OAuth 클라이언트 ID/Secret 필요 |
| 마크다운 에디터 | EasyMDE | 책 추천 simpleMDE 는 관리 중단 → 후속 포크, `data-markdown-editor` 속성 하나로 적용 |

## 강사 기법 뒤늦게 적용 (005-01 ~ 005-07)

책 구조를 유지하느라 미뤘던 부분을 강사(14168) 방식으로 바꿨다. 상세 비교는 [improvements-vs-slog.md](improvements-vs-slog.md) 3장.

| 책 | 바뀐 구현 | 커밋 |
|---|---|---|
| 엔티티마다 `id`, `createDate`, `modifyDate`, 서비스에서 `LocalDateTime.now()` | `BaseEntity` + JPA Auditing | 005-01 |
| equals 없음, `answerRepository.delete` | id 기반 equals/hashCode, `orphanRemoval` + 부모 컬렉션에서 제거 | 005-02 |
| `@Setter` + 서비스에서 `setXxx` | 생성자 + 도메인 메서드, 더티체킹 | 005-03 |
| 폼 `class` + getter/setter | `record` | 005-04 |
| `/question/list`, `/question/detail/{id}`, `/answer/modify/{id}`(POST) | `/questions`, `/questions/{id}`, `/questions/{qid}/answers/{id}/modify`(PUT) | 005-05 |
| `static/style.css` | `static/resource/style.css` | 005-06 |
| `com.mysite.sbb.question` 등 도메인 패키지 | `domain/question/question/{controller,entity,repository,service}`, `global/...` | 005-07 |

---

## 책 코드가 현재 버전에서 그대로 동작하지 않아 바꾼 부분

개선이라기보다 **버전 차이로 인한 필수 수정**이다.

| 책 | 현재 (Boot 4 / Security 7 / Thymeleaf 3.1) |
|---|---|
| `spring-boot-starter-web` | `spring-boot-starter-webmvc` (+ 테스트 스타터 모듈 분리) |
| H2 콘솔 설정만으로 동작 | `spring-boot-h2console` 의존성 필요 |
| `AntPathRequestMatcher` 로 로그아웃 GET | 제거됨 → `logoutUrl()` + POST |
| `${@commonUtil.markdown(...)}` | 제한된 표현식으로 500 → 모델 변수 |
