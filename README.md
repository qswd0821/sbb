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
| `src/main/java/com/mysite/sbb` | 컨트롤러, 폼, DTO, 엔티티, 서비스 등 자바 코드 |
| `SbbApplication.java` | 시작점. `@SpringBootApplication` → 이 패키지 하위를 스캔하므로 모든 클래스는 `com.mysite.sbb` 아래에 둔다 |
| `src/main/resources/templates` | 타임리프 템플릿 (HTML + 자바 데이터) |
| `src/main/resources/static` | CSS, JS, 이미지 등 정적 파일 |
| `src/main/resources/application.properties` | 환경 설정 (DB 등) |
| `src/test/java` | 서버 없이 실행하는 JUnit 테스트 |
| `build.gradle.kts` | 플러그인·의존성·빌드 설정 (책은 Groovy `build.gradle`, 우리는 강사 방식 Kotlin DSL) |

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
