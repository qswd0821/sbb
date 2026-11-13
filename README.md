# SBB (Spring Boot Board)

점프 투 스프링부트를 구현하면서 장희성 강사 수업(14163, 14168) 내용을 적용해 개선하는 복습 프로젝트

## 개발 환경
- Java 25 / Spring Boot 4.1.2 / Gradle 9.8.2 (Kotlin DSL)

## 실행 방법
시스템 기본 Java가 8이므로, 명령마다 `JAVA_HOME`을 JDK 25로 지정한다.

```bash
JAVA_HOME=/c/Users/Sub/.jdks/ms-25.0.4.1 ./gradlew bootRun   # 서버 실행 → http://localhost:8080
JAVA_HOME=/c/Users/Sub/.jdks/ms-25.0.4.1 ./gradlew test      # 테스트
```

인텔리제이에서는 Project Structure → SDK를 `ms-25` 로 지정하면 된다.

## 프로젝트 구조
| 위치 | 역할 |
|---|---|
| `src/main/java/com/mysite/sbb` | 자바 코드 (아래 패키지 구조) |
| `SbbApplication.java` | 시작점. `@SpringBootApplication` → 이 패키지 하위를 스캔하므로 모든 클래스는 `com.mysite.sbb` 아래에 둔다 |
| `src/main/resources/templates` | 타임리프 템플릿 (HTML + 자바 데이터) |
| `src/main/resources/static/resource` | CSS, JS 등 정적 파일 (주소 `/resource/...`, 시큐리티에서 경로 하나로 허용) |
| `src/main/resources/application.yaml` (+ `-dev`, `-test`) | 환경 설정. dev는 `./db_dev` 파일 DB, test는 메모리 DB |
| `src/test/java` | 서버 없이 실행하는 JUnit 테스트 |
| `build.gradle.kts` | 플러그인·의존성·빌드 설정 (책은 Groovy `build.gradle`, 우리는 강사 방식 Kotlin DSL) |

패키지 구조 (강사 14168 방식 `domain/<그룹>/<엔티티>/<계층>`, `global/<관심사>`)

```
com.mysite.sbb
├── SbbApplication
├── domain
│   ├── home/controller                 MainController(/ → /questions), HelloController
│   ├── question                        질문 게시판 (질문과 그에 속한 답변·댓글·카테고리)
│   │   ├── question/{controller, entity, repository, service}
│   │   ├── answer/{controller, entity, repository, service}
│   │   ├── comment/{controller, entity, repository, service}
│   │   ├── category/{entity, repository, service}
│   │   └── recent/controller           최근 활동
│   └── user/user/{controller, entity, repository, service}   회원, 프로필, 로그인
└── global
    ├── jpa/entity                      BaseEntity (id, 생성·수정일시, equals/hashCode)
    ├── security                        SecurityConfig
    ├── initData                        BaseInitData, DevInitData
    ├── exception                       DataNotFoundException
    ├── util                            CommonUtil (마크다운)
    └── web                             GlobalModelAttributes (모든 화면 공통 모델 변수)
```

폼(`record`)은 그 폼을 받는 컨트롤러와 같은 `controller` 패키지에 둔다.

## 구현 현황 (점프 투 스프링부트 1장 ~ 3-14)
| 범위 | 커밋 | 상태 |
|---|---|---|
| 1장 개발 환경, 첫 컨트롤러 | 001-01 ~ 001-05 | ✅ |
| 2장 컨트롤러·JPA·엔티티·리포지터리·타임리프·폼 검증 (2-01 ~ 2-16) | 002-01 ~ 002-29 | ✅ |
| 3장 내비게이션·페이징·시큐리티·글쓴이·수정/삭제·추천·앵커·마크다운·검색 (3-01 ~ 3-13) | 003-01 ~ 003-16 | ✅ |
| 3-14 답변 페이징·정렬, 댓글, 카테고리, 비밀번호 변경, 프로필, 최근 활동, 조회 수, 마크다운 에디터 | 004-01 ~ 004-08 | ✅ |
| 3-14 비밀번호 **찾기**(임시 비밀번호 메일 발송) | - | ⏸ SMTP 메일 계정 필요 |
| 3-14 소셜 로그인 | - | ⏸ 구글 등 OAuth 클라이언트 ID/Secret 필요 |

단원별로 **책 그대로 구현한 부분 / 개선한 부분**을 나눈 전체 정리: [docs/book-vs-improvement.md](docs/book-vs-improvement.md)

**강사 수업(slog 14163·14168) 코드 대비 기술적 개선**과 아직 적용하지 않은 강사 기법: [docs/improvements-vs-slog.md](docs/improvements-vs-slog.md)

### 책과 다르게 적용한 주요 개선 (출처)
- 빌드·설정: Kotlin DSL, `application.yaml`, dev/test 프로파일, SQL·바인딩 로그 (14163 2·5·6강)
- JPA: `mappedBy` 실험, `@ManyToOne(fetch = LAZY)`, 컬렉션 초기화, `addAnswer` + `CascadeType.PERSIST` (14163 11~25강)
- 테스트: `BaseInitData` 샘플 데이터 + 리포지터리 테스트 클래스, `@Transactional` 롤백 (14163 14~18강)
- 폼: `@Valid` + `BindingResult`, `th:object/th:field`, 오류 프래그먼트, 첫 오류 필드 포커스 (14168 9~25강)
- 화면: cdnjs, 프리텐다드, `layout:title-pattern`, `global.js` (14163 39강, 14168 44·45강)
- HTTP: 처리 후 302, 삭제는 `DELETE`(`_method`), 추천·로그아웃은 POST + CSRF, 권한 없음은 403 (14168 7·26·28·41강, 14163 37강)
- 보안: 마크다운 렌더링 시 HTML escape·위험 URL 제거 (XSS 방지)

## 작업내역

### 001-01 프로젝트 세팅, git 저장소 초기화 및 README 작성
- [코드 보기](https://github.com/qswd0821/sbb/tree/001-01)
- 작업: git init, .gitignore, README 생성
- 이유: 작업 이력을 커밋과 README 일지로 남기기 위한 기반
- 결과: 첫 커밋 생성

### 001-02 프로젝트 세팅, 스프링부트 프로젝트 생성
- [코드 보기](https://github.com/qswd0821/sbb/tree/001-02)
- 작업: Spring Initializr로 프로젝트 생성 (Boot 4.1.2, Java 25, Gradle Kotlin DSL, `com.mysite.sbb`)
- 의존성: Spring Web, Lombok, DevTools (나머지는 필요한 단원에서 추가)
- 이유: 책의 SBB 구현을 시작할 기반, 강사(14163 2강)와 동일한 빌드 구성
- 결과: `./gradlew build` 성공

### 001-03 프로젝트 세팅, 서버 실행 및 브라우저 접속 확인
- [코드 보기](https://github.com/qswd0821/sbb/tree/001-03)
- 작업: JDK 25로 `./gradlew bootRun` 실행, `http://localhost:8080/` 접속
- 이유: 내장 톰캣이 뜨고 요청을 받는지 확인 (컨트롤러가 없으니 404가 정상)
- 결과: `Starting SbbApplication using Java 25.0.4.1`, 브라우저에 Whitelabel Error Page(404) 표시

### 001-04 프로젝트 세팅, HelloController로 첫 응답 확인
- [코드 보기](https://github.com/qswd0821/sbb/tree/001-04)
- 작업: `GET /hello` → `"Hello World"` 를 반환하는 `HelloController` 추가 (책 1장 스프링부트 맛보기)
- 이유: 요청 → 컨트롤러 → 응답 흐름과 DevTools 자동 재시작이 동작하는지 확인
- 결과: `./gradlew classes` 후 DevTools가 서버를 자동 재시작(1.96초), 브라우저에 `Hello World` 표시

### 001-05 프로젝트 세팅, 완료
- [코드 보기](https://github.com/qswd0821/sbb/tree/001-05)
- 작업: README에 실행 방법(JDK 25 지정 방법) 정리
- 이유: 시스템 Java가 8이라 그냥 `gradlew`를 실행하면 실패함, 다시 와도 바로 실행할 수 있도록 기록
- 결과: 작업환경 세팅(001) 완료 — git, 프로젝트 생성, 빌드, 서버 실행, 첫 응답까지 확인

### 002-01 프로젝트 구조, 디렉터리별 역할 정리 (책 2-01)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-01)
- 작업: README에 `## 프로젝트 구조` 섹션 추가
- 이유: 2장부터 파일이 늘어나므로 각 파일을 어디에 둘지 기준을 먼저 정리, 특히 컴포넌트 스캔 범위(`com.mysite.sbb` 하위) 확인
- 책과 차이: `build.gradle` → `build.gradle.kts`(강사 방식), STS → 인텔리제이
- 결과: 코드 변경 없음, 문서 정리

### 002-02 간단한 웹 프로그램, MainController 추가 (void → 500 확인) (책 2-02)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-02)
- 작업: `GET /sbb` 에 매핑된 `MainController.index()` 추가, 리턴 타입 `void`, 콘솔에 `index` 출력
- 이유: 404(매핑 없음)와 500(매핑은 있으나 처리 중 오류)의 차이를 직접 확인 (14163 3강 DispatcherServlet 흐름)
- 결과
  - `/sbb` → **500** `Circular view path [sbb]` (void라서 URL 이름 `sbb`를 뷰 이름으로 사용 → 같은 URL로 다시 포워드), 콘솔에 `index` 출력 = 메서드는 호출됨
  - `/not-exist` → **404** (매핑된 메서드 없음)

### 002-03 간단한 웹 프로그램, @ResponseBody로 응답 (책 2-02)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-03)
- 작업: `index()` 에 `@ResponseBody` 추가, 리턴 타입 `String` → `"안녕하세요 sbb에 오신 것을 환영합니다."`
- 이유: 리턴값을 뷰 이름이 아닌 응답 본문으로 쓰게 하여 500 해결 (14163 4강)
- 결과: `/sbb` → **200**, `Content-Type: text/plain;charset=UTF-8`, 브라우저에 환영 문구 표시

### 002-04 JPA 데이터베이스, H2 추가 및 dev 프로파일 설정 (책 2-03, 14163 5강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-04)
- 작업
  - 의존성: `com.h2database:h2`(runtimeOnly), `spring-boot-h2console`
  - `application.properties` 삭제 → `application.yaml`(앱 이름, `profiles.active: dev`) + `application-dev.yaml`(`jdbc:h2:./db_dev`)
- 이유
  - 책은 홈 폴더(`~/local`)의 DB 하나를 사용 → 강사 방식으로 프로젝트 폴더 DB + 프로파일 분리
  - yaml은 계층 구조라 설정이 늘어날수록 읽기 쉬움
  - Boot 4부터 H2 콘솔이 별도 모듈(`spring-boot-h2console`)
- 결과
  - 로그: `The following 1 profile is active: "dev"`
  - `/h2-console` → 200, 콘솔 화면 표시
  - 콘솔에서 `jdbc:h2:./db_dev` 연결 시 `Database not found` → 아직 앱이 DB에 연결한 적이 없어 파일이 없음 (JPA 추가 후 앱 기동 시 생성 예정, 002-06)

### 002-05 JPA 데이터베이스, test 프로파일 분리 (메모리 DB) (14163 5강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-05)
- 작업: `application-test.yaml`(`jdbc:h2:mem:db_test`) 추가, `SbbApplicationTests` 에 `@ActiveProfiles("test")`
- 이유: 테스트가 개발 DB(`./db_dev`)를 건드리지 않고, 매번 비어 있는 메모리 DB에서 시작하도록 (테스트 간 독립성)
- 결과: `./gradlew test` 통과(1건), 테스트 로그 `The following 1 profile is active: "test"`

### 002-06 JPA 데이터베이스, JPA 추가 및 SQL 로그 설정 (책 2-03, 14163 6강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-06)
- 작업
  - 의존성: `spring-boot-starter-data-jpa`, `spring-boot-starter-data-jpa-test`
  - `application.yaml`: `ddl-auto: update`, `show-sql`, `format_sql`, `highlight_sql`, `use_sql_comments`, 바인딩 값·트랜잭션 TRACE 로그
- 이유
  - JPA(하이버네이트)로 SQL 없이 자바 코드로 DB를 다루기 위함
  - JPA가 실제로 만드는 SQL과 바인딩 값을 눈으로 확인해야 동작을 이해할 수 있음 (강사 방식)
  - 책의 `H2Dialect` 지정은 생략: 최신 하이버네이트는 DB 종류를 자동 감지
- 결과
  - 서버: `HikariPool-1 - Added connection conn0: url=jdbc:h2:./db_dev`, `Initialized JPA EntityManagerFactory`
  - `db_dev.mv.db` 파일 자동 생성 → H2 콘솔 `jdbc:h2:./db_dev` 연결 성공 (002-04의 `Database not found` 해결)
  - 테스트: 통과(1건), `url=jdbc:h2:mem:db_test` 로 메모리 DB 사용 확인

### 002-07 작업내역, 커밋 번호 태그와 코드 링크 추가
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-07)
- 작업: 기존 커밋에 번호 태그(001-01 ~ 002-06) 부여, 작업내역 항목마다 GitHub 코드 링크 추가
- 이유: README 일지에서 해당 시점의 코드를 바로 열어볼 수 있게 (강사 slog 페이지의 강의별 커밋 링크 방식)
- 결과: 이후 모든 커밋도 같은 번호로 태그

### 002-08 엔티티, Question 엔티티 추가 (책 2-04)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-08)
- 작업: `Question` 엔티티 (id IDENTITY, subject 200자, content TEXT, createDate)
- 이유: 질문 데이터를 저장할 테이블을 자바 클래스로 정의 (ORM)
- 결과: 서버 재시작 시 하이버네이트가 `create table question (...)` 실행 (ddl-auto: update)

### 002-09 엔티티, Answer 엔티티와 @ManyToOne 추가 (책 2-04, 14163 7강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-09)
- 작업: `Answer` 엔티티 (content, createDate, `@ManyToOne Question question`)
- 이유: 답변 N개 → 질문 1개 관계. `@ManyToOne` 은 FK 컬럼을 만든다 (14163 7강)
- 결과: `create table answer (... question_id integer ...)` + `alter table answer add constraint FK... foreign key (question_id)`

### 002-10 엔티티, 실험 - mappedBy 없는 @OneToMany는 중간 테이블 생성 (14163 11강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-10)
- 작업: `Question` 에 `@OneToMany List<Answer> answerList` 를 **mappedBy 없이** 추가 (실험)
- 이유: 책은 mappedBy를 쓰라고만 함 → 빼면 무슨 일이 생기는지 직접 확인 (14163 11강)
- 결과: `create table question_answer_list (question_id, answer_list_id)` 중간 테이블 생성. 이미 `answer.question_id` 가 있는데도 관계를 중복 저장하는 쓸데없는 테이블

### 002-11 엔티티, @OneToMany에 mappedBy와 cascade REMOVE 적용 (책 2-04, 14163 9·12강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-11)
- 작업: `@OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE) List<Answer> answerList`
- 이유: mappedBy로 관계의 주인이 `Answer.question` 임을 알려 중간 테이블 없이 `answer.question_id` 사용. 질문 삭제 시 답변도 삭제
- 결과: 개발 DB 재생성 후 `question`, `answer` 테이블만 생성 (중간 테이블 없음). `@OneToMany` 는 DB 컬럼을 만들지 않음

### 002-12 리포지터리, Question/Answer 리포지터리와 테스트 샘플 데이터 (책 2-05, 14163 14·15강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-12)
- 작업: `QuestionRepository`, `AnswerRepository` (JpaRepository), test 프로파일 전용 `TestInitData`(질문 2, 답변 1), `QuestionRepositoryTest`(findAll, findById)
- 이유: 책은 테스트 안에서 데이터를 save 하고 `SbbApplicationTests` 하나에 몰아넣음 → 강사 방식으로 샘플 데이터 자동 생성 + 리포지터리별 테스트 클래스 분리
- 결과: 테스트 3건 통과

### 002-13 리포지터리, 쿼리 메서드 findBySubject/And/Like (책 2-05, 14163 16·17강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-13)
- 작업: `findBySubject`, `findBySubjectAndContent`, `findBySubjectLike` + 테스트
- 이유: 메서드 이름 규칙(findBy + 컬럼 + And/Or/Like/Between/In/OrderBy...)만으로 JPA가 SQL을 생성
- 책과 차이: 단건 조회 리턴을 `Question` 대신 `Optional<Question>` (14163 방식, 결과 없음을 타입으로 표현)
- 결과: 테스트 6건 통과

### 002-14 리포지터리, 수정·삭제 테스트와 @Transactional 롤백 (책 2-05, 14163 18·19강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-14)
- 작업: 질문 수정(t6), 삭제(t7) 테스트
- 이유: 테스트 클래스의 `@Transactional` 로 각 테스트가 끝나면 롤백 → t6에서 제목을 바꾸거나 t7에서 지워도 다른 테스트에 영향 없음 (테스트 간 독립성)
- 결과: 테스트 8건 통과 (t7은 다른 테스트 후에도 count=2에서 시작)

### 002-15 리포지터리, answerRepository로 답변 생성·조회 (책 2-05, 14163 20강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-15)
- 작업: 답변 생성(t8, `answerRepository.save`), 답변 조회(t9, `answer.getQuestion()`)
- 이유: 책 방식(관계지향적: 자식 리포지터리로 직접 저장)을 먼저 확인 → 다음 커밋에서 OneToMany 방식과 비교
- 결과: 테스트 10건 통과

### 002-16 리포지터리, OneToMany 답변 생성과 LAZY·컬렉션 초기화 (14163 21~25강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-16)
- 작업: `Question.addAnswer()`, cascade에 `PERSIST` 추가, `answerList = new ArrayList<>()`, `Answer.question` 에 `fetch = LAZY`, TestInitData도 addAnswer 사용, 테스트 t10·t11
- 이유: 책에 없는 강사 개선. 답변을 질문을 통해 객체지향적으로 생성(리포지터리 주입 불필요), 새 엔티티의 NPE 방지, 불필요한 조회를 줄이는 LAZY
- 결과: 테스트 12건 통과. t10에서 addAnswer 직후 id=null → flush 후 INSERT되어 id 생성 확인

### 002-17 도메인 분류, question·answer 패키지로 분리 (책 2-06, 14163 26강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-17)
- 작업: `com.mysite.sbb.question`(Question, QuestionRepository, 테스트), `com.mysite.sbb.answer`(Answer, AnswerRepository)
- 이유: 기능(도메인)별로 파일을 모아 관련 코드를 찾기 쉽게. 모두 `com.mysite.sbb` 하위라 컴포넌트 스캔 범위 안
- 결과: 테스트 12건 통과

### 002-18 질문 목록, 타임리프로 /question/list 구현 (책 2-07, 14163 28~32강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-18)
- 작업: 타임리프 의존성, `QuestionController`(GET /question/list → Model에 questionList), `question_list.html`(th:each 테이블), `TestInitData` → `BaseInitData`(dev에서도 샘플 데이터)
- 이유: 컨트롤러는 데이터만 준비하고 HTML은 템플릿이 생성 (서버 사이드 렌더링). dev DB에도 화면 확인용 데이터 필요
- 결과: `/question/list` → 200, 샘플 질문 2건 표시

### 002-19 루트 URL, / 접속 시 질문 목록으로 리다이렉트 (책 2-08, 14163 33강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-19)
- 작업: `MainController` 에 `GET /` → `redirect:/question/list`
- 이유: 사이트 주소만 입력해도 메인 화면(질문 목록)이 보이도록
- 결과: `/` → 302, `Location: /question/list`

### 002-20 서비스, QuestionService 도입 (책 2-09, 14163 34강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-20)
- 작업: `QuestionService.getList()`, 컨트롤러는 리포지터리 대신 서비스 사용
- 이유: 컨트롤러는 요청/응답, 서비스는 비즈니스 로직, 리포지터리는 DB 접근으로 역할 분리. 여러 컨트롤러가 같은 로직 재사용
- 결과: `/question/list` 동일하게 200

### 002-21 상세 페이지, 질문 상세와 없는 질문 404 처리 (책 2-10, 14163 35강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-21)
- 작업: `GET /question/detail/{id}`, `question_detail.html`, 목록 제목에 상세 링크(`@{|/question/detail/${question.id}|}`), `DataNotFoundException`(@ResponseStatus 404)
- 이유: 없는 id 요청 시 500(`No value present`) 대신 404와 `question not found` 로 원인을 분명히 (14163 35강 커스텀 예외)
- 결과: `/question/detail/2` → 200, `/question/detail/999` → 404 `question not found`

### 002-22 URL 프리픽스, QuestionController에 @RequestMapping("/question") (책 2-11, 14163 36강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-22)
- 작업: 클래스에 `@RequestMapping("/question")`, 메서드는 `/list`, `/detail/{id}`
- 이유: 컨트롤러의 공통 URL 접두어를 한 곳에서 관리 (중복 제거, 변경 용이)
- 결과: `/question/list`, `/question/detail/1` 동일하게 200

### 002-23 답변 기능, 답변 등록과 목록 표시 (책 2-12, 14163 37강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-23)
- 작업: `AnswerController`(POST /answer/create/{id}), `AnswerService.create`, 상세 페이지에 답변 개수·목록·등록 폼
- 책과 차이: `answerRepository.save` 대신 `question.addAnswer()` (14163 37강 강사 커스터마이징, OneToMany + PERSIST)
- 결과: 브라우저 폼으로 한글 답변 등록 → 302 → 상세 페이지에 `2개의 답변이 있습니다.` 와 답변 표시

### 002-24 웹 디자인, static 스타일시트 적용 (책 2-13, 14163 38강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-24)
- 작업: `static/style.css`(textarea 100%, 버튼 여백), 상세 템플릿에 `<link th:href="@{/style.css}">`
- 이유: CSS·JS·이미지 같은 정적 파일은 `resources/static` 에 두면 URL 루트(`/style.css`)로 그대로 제공
- 결과: `/style.css` → 200 `text/css`

### 002-25 부트스트랩, 목록·상세 화면 꾸미기 (책 2-14, 14163 39강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-25)
- 작업: 부트스트랩 5.3.3 적용(목록 테이블, 상세 카드, 답변 폼), 작성일시 `#temporals.format`, 프리텐다드 폰트
- 책과 차이: 부트스트랩 파일을 내려받아 static에 넣지 않고 **cdnjs** 링크 사용, 폰트는 프리텐다드 (14163 39강 강사 커스터마이징)
- 결과: 목록 `번호/제목/작성일시(yyyy-MM-dd HH:mm)`, 상세 카드 UI 브라우저 확인

### 002-26 표준 HTML 구조, layout.html 공통 레이아웃과 페이지별 title (책 2-15, 14168 44·45강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-26)
- 작업: `thymeleaf-layout-dialect`, `layout.html`(doctype/head/CSS/JS 공통), 목록·상세는 `layout:decorate="~{layout}"` + `layout:fragment="content"`, `layout:title-pattern="$CONTENT_TITLE | SBB"`
- 이유: 모든 페이지에 표준 HTML 구조를 한 번만 작성. 페이지마다 고유 title (14168 45강)
- 결과: 목록 `<title>질문 목록 | SBB</title>`, 상세 `<title>질문 상세 | SBB</title>`, 부트스트랩 CSS/JS 공통 적용

### 002-27 질문 등록, 등록 폼과 저장 (책 2-16, 14168 4~7강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-27)
- 작업: 목록에 `질문 등록하기` 버튼, `GET /question/create`(폼), `POST /question/create`(저장 후 목록으로 302), `QuestionService.create`, `question_form.html`
- 이유: 폼 조회는 GET, 데이터 생성은 POST (14168 7강, 멱등성). URL이 같아도 HTTP 메서드로 구분 (14168 26강)
- 결과: GET 200, POST → 302 `/question/list`, 목록에 새 질문 표시

### 002-28 질문 등록, 폼 검증과 오류 표시·포커스 (책 2-16, 14168 9~25강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-28)
- 작업: validation 의존성, `QuestionForm`(@NotEmpty, @Size), `@Valid` + `BindingResult`, `th:object`/`th:field`, `form_errors.html` 프래그먼트(`#fields`, `data-error-field-name`), `global.js`(가장 위 오류 필드 focus)
- 이유: 빈 제목/내용 저장 방지. 실패 시 예외 대신 폼을 다시 보여주고 입력값 유지 (14168 9·10·13강). 오류 필드로 커서 이동 (14168 15·25강)
- 결과: 빈 폼 제출 → 200, 오류 2개 표시, 내용 입력값 유지, focus=subject

### 002-29 답변 등록, 폼 검증 적용 (책 2-16)
- [코드 보기](https://github.com/qswd0821/sbb/tree/002-29)
- 작업: `AnswerForm`(@NotEmpty), `AnswerController` 에 `@Valid` + `BindingResult`, 상세 GET에 `AnswerForm`, 상세 템플릿 답변 폼에 `th:object`/`th:field` + 오류 프래그먼트
- 이유: 빈 답변 저장 방지, 질문 폼과 같은 검증·오류 표시 방식 재사용
- 결과: 빈 답변 → 200 `내용은 필수항목입니다.`, 정상 답변 → 302

### 003-01 내비게이션 바, navbar 프래그먼트를 레이아웃에 추가 (책 3-01)
- [코드 보기](https://github.com/qswd0821/sbb/tree/003-01)
- 작업: `navbar.html`(navbarFragment: SBB 로고, 로그인 링크 자리, 모바일 햄버거 버튼), `layout.html` 에 `th:replace` 로 삽입
- 이유: 모든 페이지 상단에 공통 메뉴. 레이아웃에 한 번만 넣으면 전 페이지 적용
- 결과: 목록 페이지에 `<a class="navbar-brand" href="/">SBB</a>` 출력. 레이아웃의 설명 주석은 타임리프 주석으로 바꿔 응답에서 제외

### 003-02 페이징, dev 프로파일에 질문 300개 샘플 데이터 (책 3-02)
- [코드 보기](https://github.com/qswd0821/sbb/tree/003-02)
- 작업: `DevInitData`(@Profile dev, @Order 2)로 질문을 300개까지 생성, `BaseInitData` 는 @Order 1, `QuestionService.count()`
- 책과 차이: 책은 테스트 메서드로 300개를 넣지만 테스트는 메모리 DB(002-05)라 dev 화면에 안 보임 → dev 초기 데이터로 생성
- 결과: 목록에 질문 300개 (페이징 전이라 한 화면에 전부 출력)

### 003-03 페이징, Page 조회와 페이지 이동 UI (책 3-02)
- [코드 보기](https://github.com/qswd0821/sbb/tree/003-03)
- 작업: `QuestionService.getList(int page)` → `PageRequest.of(page, 10, createDate desc)`, 컨트롤러 `?page=` 파라미터(기본 0), 템플릿 `paging` + 부트스트랩 pagination(이전/다음, 현재 ±5페이지)
- 이유: 300개를 한 번에 출력하지 않고 10개씩, 최신 글 먼저
- 결과: 첫 페이지 10건(최신 글부터), `?page=29`(마지막) 10건 + 다음 버튼 disabled

### 003-04 게시물 번호, 전체 개수 기준 역순 번호 (책 3-03)
- [코드 보기](https://github.com/qswd0821/sbb/tree/003-04)
- 작업: 번호 = `전체 개수 - (현재 페이지 × 페이지 크기) - 나열 인덱스`
- 이유: `loop.count` 는 페이지마다 1부터 다시 시작 → 최신 글이 가장 큰 번호를 갖도록
- 결과: 첫 페이지 300 ~ 291, 마지막(?page=29) 10 ~ 1

### 003-05 답변 개수, 목록 제목 옆에 답변 수 표시 (책 3-04)
- [코드 보기](https://github.com/qswd0821/sbb/tree/003-05)
- 작업: 제목 옆 `#lists.size(question.answerList)` 빨간 숫자(답변이 있을 때만), 헤더 가운데 정렬·제목 칸 50%
- 이유: 목록에서 답변이 달린 질문을 한눈에 파악
- 결과: 마지막 페이지 `스프링부트 모델 질문입니다. 1`, `sbb가 무엇인가요? 3`

### 003-06 스프링 시큐리티, 도입과 모든 요청 허용·CSRF 확인 (책 3-05, 14168 46강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/003-06)
- 작업: security 의존성(+ thymeleaf-extras-springsecurity6, security-test), `SecurityConfig`(SecurityFilterChain: 모든 요청 permitAll, H2 콘솔 CSRF 예외, X-Frame-Options SAMEORIGIN)
- 이유: 회원가입·로그인 준비. 처음엔 모두 허용해 기존 기능을 깨지 않고 도입 (14168 46강). `th:action` 폼은 CSRF 토큰이 자동으로 들어감
- 결과: 목록 200, 폼에 `_csrf` hidden, 토큰 없는 POST 403, 토큰 있는 POST 302, H2 콘솔 200

### 003-07 회원가입, SiteUser 엔티티와 가입 폼·검증 (책 3-06)
- [코드 보기](https://github.com/qswd0821/sbb/tree/003-07)
- 작업: `user` 패키지(`SiteUser`, `UserRepository`, `UserService`, `UserCreateForm`, `UserController`), `signup_form.html`, `PasswordEncoder`(BCrypt) 빈, navbar 회원가입 링크
- 이유: 회원 정보 저장. 비밀번호는 BCrypt 해시로 저장. 비밀번호 불일치·중복 가입은 폼 오류로 표시
- 결과: 불일치 → 200 `2개의 패스워드가 일치하지 않습니다.`, 정상 → 302 `/`, 중복 → 200 `이미 등록된 사용자ID 또는 이메일입니다.`

### 003-08 로그인·로그아웃, UserDetailsService와 폼 로그인 (책 3-07)
- [코드 보기](https://github.com/qswd0821/sbb/tree/003-08)
- 작업: `UserRole`, `UserSecurityService`(loadUserByUsername), `findByUsername`, `formLogin`(/user/login), `logout`(/user/logout), `login_form.html`, navbar에 `sec:authorize` 로 로그인 전/후 메뉴
- 책과 차이: 로그아웃을 GET 링크 대신 **POST 폼**(CSRF 토큰 포함). 시큐리티 7은 로그아웃을 POST로 받는 것이 기본이고, GET이면 다른 사이트의 링크로 강제 로그아웃될 수 있음
- 결과: 틀린 비밀번호 → 302 `/user/login?error`, 로그인 → 302 `/` + navbar `user1 로그아웃`, 로그아웃 → 302 `/` + `로그인 회원가입`

### 003-09 글쓴이, 질문·답변 author와 로그인 필요 처리 (책 3-08)
- [코드 보기](https://github.com/qswd0821/sbb/tree/003-09)
- 작업: `Question.author`, `Answer.author`(@ManyToOne LAZY SiteUser), `Principal` 로 작성자 저장, `@PreAuthorize("isAuthenticated()")` + `@EnableMethodSecurity`, 목록 `글쓴이` 칸, 상세 작성자 표시, 비로그인 시 답변 입력창 비활성화
- 이유: 누가 썼는지 기록. 질문 등록·답변 등록은 로그인 사용자만
- 결과: 비로그인 `GET /question/create`·`POST /answer/create/1` → 302 `/user/login`, 로그인 후 등록한 질문의 글쓴이 `user1` 표시, 테스트 12건 통과

### 003-10 수정, 질문 수정과 작성자 확인·수정일시 (책 3-09)
- [코드 보기](https://github.com/qswd0821/sbb/tree/003-10)
- 작업: `modifyDate`(질문·답변), `GET/POST /question/modify/{id}`, 작성자 확인 `checkAuthor`, `question_form` 재사용(th:action 생략 + CSRF hidden), 상세에 본인만 `수정` 버튼과 `modified at` 표시
- 책과 차이: 권한 없음 응답을 400 대신 **403 FORBIDDEN** (의미에 맞는 상태 코드)
- 결과: 본인 질문 수정 폼 200 → 저장 302 → 상세에 수정 내용·`modified at`, 남의 질문 수정 → 403

### 003-11 삭제, 질문 삭제를 DELETE 메서드로 (책 3-09, 14168 41강)
- [코드 보기](https://github.com/qswd0821/sbb/tree/003-11)
- 작업: `spring.mvc.hiddenmethod.filter.enabled: true`, `@DeleteMapping("/delete/{id}")`, `QuestionService.delete`, 상세에 본인만 `삭제` 폼(`th:method="delete"` + confirm)
- 책과 차이: 책은 `GET /question/delete/{id}` 링크 + JS. GET은 조회용이라 링크 미리보기·크롤러로도 삭제될 수 있고 CSRF 보호도 없음 → **DELETE 메서드 + CSRF 토큰** (14168 41강, 14163 37강 URL 표)
- 결과: 본인 삭제 → 302 `/` 후 상세 404, 남의 질문 → 403, GET 방식 요청 → 405

### 003-12 수정·삭제, 답변 수정과 삭제 (책 3-09)
- [코드 보기](https://github.com/qswd0821/sbb/tree/003-12)
- 작업: `AnswerService.getAnswer/modify/delete`, `GET/POST /answer/modify/{id}`, `DELETE /answer/delete/{id}`, `answer_form.html`, 상세에 답변별 본인만 수정·삭제 버튼과 `modified at`
- 이유: 질문과 같은 규칙(작성자만, 서버에서 403 검사, 삭제는 DELETE)을 답변에도 적용
- 결과: 답변 수정 200 → 302 + `modified at`, 남의 답변 삭제 403, 본인 답변 삭제 302 후 목록에서 사라짐

### 003-13 추천, 질문·답변 추천 @ManyToMany (책 3-10)
- [코드 보기](https://github.com/qswd0821/sbb/tree/003-13)
- 작업: `voter`(@ManyToMany `Set<SiteUser>`, `new HashSet<>()`), `QuestionService.vote`, `AnswerService.vote`, `POST /question/vote/{id}`, `POST /answer/vote/{id}`, 상세에 추천 버튼 + 추천 수 배지
- 책과 차이: 추천은 데이터를 바꾸므로 GET 링크 대신 **POST 폼**(CSRF 보호)
- 결과: 같은 사용자가 질문을 2번 추천해도 1, 답변 추천 1, GET 요청은 405

### 003-14 앵커, 답변 등록·수정·추천 후 해당 답변 위치로 이동 (책 3-11)
- [코드 보기](https://github.com/qswd0821/sbb/tree/003-14)
- 작업: 답변 카드에 `<a th:id="|answer_${answer.id}|">`, 답변 등록·수정·추천 리다이렉트에 `#answer_{id}`
- 이유: 답변이 많을 때 처리 후 페이지 맨 위로 돌아가지 않고 방금 다룬 답변으로 스크롤
- 결과: 답변 등록 → 302 `/question/detail/2#answer_7`, 추천 → `#answer_1`

### 003-15 마크다운, commonmark로 질문·답변 내용 렌더링 (책 3-12)
- [코드 보기](https://github.com/qswd0821/sbb/tree/003-15)
- 작업: `org.commonmark:commonmark:0.24.0`, `CommonUtil.markdown()`, `GlobalModelAttributes`(@ControllerAdvice로 모든 템플릿에 `commonUtil` 제공), 상세 내용 `th:utext="${commonUtil.markdown(...)}"`
- 책과 차이: ① 입력된 HTML은 escape, `javascript:` 링크 제거(`escapeHtml`, `sanitizeUrls`) → `th:utext` XSS 방지 ② Thymeleaf 3.1은 `th:utext` 에서 `@빈` 참조를 막으므로(500 발생) 모델 변수로 전달 (14168 19강 공통 모델 변수)
- 결과: `# Title`, `**bold**`, 목록이 HTML로, `<script>` 는 `&lt;script&gt;` 로 표시

### 003-16 검색, 제목·내용·글쓴이·답변으로 검색 (책 3-13)
- [코드 보기](https://github.com/qswd0821/sbb/tree/003-16)
- 작업: `QuestionRepository.findAllByKeyword`(@Query JPQL, left outer join + distinct + Pageable), `getList(page, kw)`, 컨트롤러 `kw` 파라미터, 목록 상단 검색창(GET 폼), 페이지 링크에 `kw` 유지
- 책과 차이: 책은 숨은 searchForm + 자바스크립트로 page/kw를 제출. 여기서는 **GET 검색 폼 + `@{/question/list(page=..., kw=...)}` 링크**로 JS 없이 처리 (검색 결과 URL 공유·새로고침 가능)
- 결과: `kw=markdown` 1건, `kw=user1` 3건(글쓴이), `kw=anchor` → 답변 내용으로 2번 질문, 없는 검색어 0건

### 004-01 추가 기능, 답변 페이징과 최신순·추천순 정렬 (책 3-14)
- [코드 보기](https://github.com/qswd0821/sbb/tree/004-01)
- 작업: `AnswerRepository.findByQuestion(Pageable)`, 추천순 `@Query`(left join voter, group by, count desc), `AnswerService.getList(question, page, sort)`(5개씩), 상세에 `answerPage`·`answerSort` 파라미터, 최신순/추천순 버튼, 답변 페이지 이동
- 이유: 답변이 많아지면 한 번에 모두 출력하기 부담. 추천 많은 답변을 먼저 보고 싶은 경우 (스택오버플로우 방식)
- 결과: 답변 7개 → 1페이지 5개(최신순), 2페이지 2개, 추천순은 추천 1개인 답변(1)이 맨 앞

### 004-02 추가 기능, 질문·답변 댓글 (책 3-14)
- [코드 보기](https://github.com/qswd0821/sbb/tree/004-02)
- 작업: `comment` 패키지(`Comment` 엔티티: question 또는 answer에 연결, `CommentForm`, `CommentService`, `CommentController`), `Question/Answer.commentList`(cascade REMOVE), `comments.html` 파라미터 프래그먼트, 상세의 질문·답변 아래 댓글 목록·작성·본인 삭제
- 이유: 답변까지 달 정도는 아닌 짧은 의견. 질문/답변이 지워지면 댓글도 함께 삭제
- 결과: 질문 댓글 302 `/question/detail/2`, 답변 댓글 302 `#answer_43`, 빈 댓글은 `댓글 내용은 필수항목입니다.`(flash), 본인 댓글 DELETE 302

### 004-03 추가 기능, 카테고리(질문답변·강좌·자유게시판) (책 3-14)
- [코드 보기](https://github.com/qswd0821/sbb/tree/004-03)
- 작업: `category` 패키지(`Category`, `CategoryRepository`, `CategoryService`), `Question.category`, 질문 폼 카테고리 선택(@NotNull), 목록 `?category=` 필터(검색·페이징과 함께 유지), 내비게이션 바 카테고리 메뉴(공통 모델 `categoryList`), 초기 데이터
- 이유: 게시판을 성격별로 분리. 기존 질문은 `질문답변` 으로 이동(일괄 update)
- 결과: 메뉴 `질문답변·강좌·자유게시판`, 카테고리 미선택 → `카테고리는 필수항목입니다.`, 강좌로 등록 → `/question/list?category=2` 에 1건, 질문답변 302건

### 004-04 추가 기능, 비밀번호 변경 (책 3-14)
- [코드 보기](https://github.com/qswd0821/sbb/tree/004-04)
- 작업: `PasswordChangeForm`, `GET/POST /user/password`(로그인 필요), 현재 비밀번호 확인(`passwordEncoder.matches`), 새 비밀번호 일치 확인, 변경 후 flash 메시지, navbar `비밀번호 변경` 링크
- 이유: 로그인한 사용자가 스스로 비밀번호를 바꿀 수 있어야 함
- 미구현: 비밀번호 **찾기**(임시 비밀번호 메일 발송)는 SMTP 메일 서버 계정이 필요해 이번 범위에서 제외
- 결과: 현재 비밀번호 틀림/새 비밀번호 불일치 → 해당 필드 오류, 정상 변경 → 302 + `비밀번호가 변경되었습니다.`, 이전 비밀번호 로그인 실패·새 비밀번호 로그인 성공

### 004-05 추가 기능, 프로필 화면 (책 3-14)
- [코드 보기](https://github.com/qswd0821/sbb/tree/004-05)
- 작업: `ProfileController`(GET /user/profile, 로그인 필요), 질문·답변·댓글 리포지터리에 `findTop10ByAuthorOrderByCreateDateDesc`, `countByAuthor`, `profile.html`(기본 정보, 활동 수, 최근 질문/답변/댓글 링크), navbar 사용자명 → 프로필 링크
- 이유: 내가 쓴 글을 한곳에서 확인
- 결과: user1 프로필 `질문 3 · 답변 6 · 댓글 1` 과 최근 목록, 비로그인 → 로그인 페이지

### 004-06 추가 기능, 최근 답변·최근 댓글 화면 (책 3-14)
- [코드 보기](https://github.com/qswd0821/sbb/tree/004-06)
- 작업: `RecentController`(GET /recent), 답변·댓글 `findTop10ByOrderByCreateDateDesc`, `recent.html`(최근 답변: 내용·질문 제목·작성자·시각, 최근 댓글), navbar `최근 활동` 메뉴
- 이유: 질문 목록만으로는 오래된 질문에 새로 달린 답변·댓글을 알 수 없음
- 결과: `/recent` 200, 최근 답변 `paging answer 5 / 질문: 스프링부트 모델 질문입니다. / user1` 순서로 표시

### 004-07 추가 기능, 질문 조회 수 (책 3-14)
- [코드 보기](https://github.com/qswd0821/sbb/tree/004-07)
- 작업: `Question.viewCount`(@ColumnDefault 0, not null), `increaseViewCount`(@Modifying JPQL `viewCount + 1`), 상세 진입 시 증가, 목록 `조회` 칸·상세 `조회 n`
- 이유: 질문의 관심도 표시. 증가는 DB에서 한 번에 처리해 동시 조회 시 누락 방지
- 결과: 1번 질문 상세 3회 → `조회 1, 2, 3`, 목록에도 3, 없는 질문은 404 유지

### 004-08 추가 기능, 마크다운 에디터 EasyMDE (책 3-14)
- [코드 보기](https://github.com/qswd0821/sbb/tree/004-08)
- 작업: 레이아웃에 EasyMDE 2.18.0 CSS/JS, `global.js` 가 `textarea[data-markdown-editor]` 를 에디터로 변환(`forceSync`), 질문 폼·답변 폼·상세 답변 입력에 적용
- 책과 차이: 책이 추천한 simpleMDE는 관리가 중단(아이콘 CDN도 끊김) → 후속 포크 **EasyMDE**. cdnjs에 없어 jsdelivr 사용
- 결과: 질문 등록 화면에 에디터 1개·툴바 버튼 12개, 에디터로 작성한 내용이 저장되어 상세에서 `<h2>`, 목록, 굵게로 렌더링

### 004-09 추가 기능, 완료 및 README 구현 현황 정리 (책 3-14)
- [코드 보기](https://github.com/qswd0821/sbb/tree/004-09)
- 작업: README에 구현 현황 표(1장 ~ 3-14), 미구현 2건과 사유, 책과 다르게 적용한 개선 요약, 프로젝트 구조 설명을 yaml·도메인 패키지 기준으로 갱신
- 이유: 3-14까지 진행 결과와 남은 일을 한눈에 확인
- 결과: 전체 테스트 12건 통과, 1장 ~ 3-13 완료, 3-14는 9개 중 7개 + 비밀번호 변경 완료(비밀번호 찾기·소셜 로그인 보류)

### 004-10 정리, 책 기준 구현과 개선 구현 비교 문서 (docs/book-vs-improvement.md)
- [코드 보기](https://github.com/qswd0821/sbb/tree/004-10)
- 작업: `docs/book-vs-improvement.md` 추가(한눈에 보기 표, 1장·2장·3장·3-14 단원별 "책 그대로 / 개선(이유·출처)", 버전 차이로 인한 필수 수정), README 구현 현황에 링크
- 이유: 어떤 부분이 책 내용이고 어떤 부분이 강사 수업으로 개선한 내용인지 한 문서에서 구분해 복습
- 결과: GitHub 저장소 공개 전환, 문서에서 각 개선의 커밋 번호·강의 출처 확인 가능

### 004-11 정리, 강사 수업(slog) 코드 대비 기술적 개선 문서
- [코드 보기](https://github.com/qswd0821/sbb/tree/004-11)
- 작업: `docs/improvements-vs-slog.md` 추가 — ① 강사 수업에서 가져와 적용한 기술 ② 강사 수업 범위를 넘어 추가한 기술적 개선(보안·JPA·웹·초기 데이터) ③ 강사 코드에는 있지만 아직 적용하지 않은 것, README 링크. 커밋 작성자 이메일을 GitHub noreply 로 변경(전체 이력 재작성 후 강제 push)
- 이유: 책은 도구가 오래되어 비교가 의미 없이 길어짐 → 강사 코드를 기준선으로 기술적 차이만 정리. 개인 이메일 비공개
- 결과: 개선 22개 항목(근거·커밋 번호), 미적용 강사 기법 8개, GitHub 모든 커밋 작성자 `...@users.noreply.github.com`

### 005-01 강사 기법, BaseEntity + JPA Auditing
- [코드 보기](https://github.com/qswd0821/sbb/tree/005-01)
- 엔티티 5개(Question, Answer, Comment, Category, SiteUser)의 공통 필드 `id`, `createDate`, `modifyDate` 를 `BaseEntity`(@MappedSuperclass)로 이동 (14168)
- `@EnableJpaAuditing(modifyOnCreate = false)` : 생성일시·수정일시 자동 기록, 생성 시 수정일시는 비워 둠
- 서비스·초기 데이터의 `setCreateDate(now())`, `setModifyDate(now())` 7곳 제거
- 테스트 t12(생성·수정 시각), t13(추천만으로는 수정일시 불변) 추가

### 005-02 강사 기법, 엔티티 equals/hashCode + orphanRemoval
- [코드 보기](https://github.com/qswd0821/sbb/tree/005-02)
- `BaseEntity.equals/hashCode` : 같은 행(id)이면 같은 엔티티 (14168 37·38강), LAZY 프록시·저장 전 엔티티까지 고려
- 답변·댓글 컬렉션에 `orphanRemoval = true`, 삭제는 리포지터리 대신 부모 컬렉션에서 제거 (`question.deleteAnswer`, `comment.removeFromParent`)
- 테스트 t14(프록시·저장 전 equals), t15(컬렉션 제거 → DELETE) 추가

### 005-03 강사 기법, 엔티티 @Setter 제거와 도메인 메서드
- [코드 보기](https://github.com/qswd0821/sbb/tree/005-03)
- 엔티티 5개에서 `@Setter` 제거, `@NoArgsConstructor(access = PROTECTED)` + 필수 값을 받는 생성자 (14168 Post)
- 변경은 도메인 메서드로만: `question.modify/vote/addComment`, `answer.modify/vote/addComment`, `siteUser.changePassword`
- 서비스의 수정·추천은 `save()` 대신 `@Transactional` + 더티체킹, 댓글 등록도 답변처럼 부모 컬렉션 + PERSIST cascade

### 005-04 강사 기법, 폼 DTO 를 record 로
- [코드 보기](https://github.com/qswd0821/sbb/tree/005-04)
- 폼 5개(QuestionForm, AnswerForm, CommentForm, UserCreateForm, PasswordChangeForm)를 `class + @Getter/@Setter` → `record` (14168 40강)
- 컨트롤러는 `form.getX()` → `form.x()`, 수정 폼 GET 은 setter 대신 `new QuestionForm(...)` 을 모델에 추가
- 템플릿(`th:object`, `th:field`)은 변경 없음

### 005-05 강사 기법, REST 스타일 URL 과 수정은 PUT
- [코드 보기](https://github.com/qswd0821/sbb/tree/005-05)
- 질문 `/question/list`, `/question/detail/{id}` … → `/questions`, `/questions/{id}`, `/questions/write`, `/questions/{id}/modify` (14163 37강, 14168)
- 답변·댓글은 질문 아래 자원 : `/questions/{questionId}/answers/{id}/modify`, `/questions/{questionId}/comments/{id}/delete`
- 수정은 `@PutMapping` + `th:method="put"`, URL 의 부모 id 와 실제 소속이 다르면 404

### 005-06 강사 기법, 정적 파일을 static/resource 로
- [코드 보기](https://github.com/qswd0821/sbb/tree/005-06)
- `static/style.css`, `static/global.js` → `static/resource/` (주소 `/resource/...`) (14168 46강)
- 시큐리티 : `/favicon.ico`, `/resource/**`, `/h2-console/**` 를 경로 단위로 명시 허용

### 005-07 강사 기법, domain/global 패키지 구조
- [코드 보기](https://github.com/qswd0821/sbb/tree/005-07)
- 자바 파일 38개를 `domain/<그룹>/<엔티티>/<계층>`, `global/<관심사>` 로 이동 (14168 `domain/post/post/controller`, `global/jpa/entity`)
- 그룹 : `domain.question`(질문·답변·댓글·카테고리·최근활동), `domain.user`, `domain.home` / 공통 : `global.jpa`, `security`, `initData`, `exception`, `util`, `web`
- 코드 내용 변경 없음 (package·import 만), README 프로젝트 구조 갱신
