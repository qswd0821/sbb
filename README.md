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
- 작업: git init, .gitignore, README 생성
- 이유: 작업 이력을 커밋과 README 일지로 남기기 위한 기반
- 결과: 첫 커밋 생성

### 001-02 프로젝트 세팅, 스프링부트 프로젝트 생성
- 작업: Spring Initializr로 프로젝트 생성 (Boot 4.1.2, Java 25, Gradle Kotlin DSL, `com.mysite.sbb`)
- 의존성: Spring Web, Lombok, DevTools (나머지는 필요한 단원에서 추가)
- 이유: 책의 SBB 구현을 시작할 기반, 강사(14163 2강)와 동일한 빌드 구성
- 결과: `./gradlew build` 성공

### 001-03 프로젝트 세팅, 서버 실행 및 브라우저 접속 확인
- 작업: JDK 25로 `./gradlew bootRun` 실행, `http://localhost:8080/` 접속
- 이유: 내장 톰캣이 뜨고 요청을 받는지 확인 (컨트롤러가 없으니 404가 정상)
- 결과: `Starting SbbApplication using Java 25.0.4.1`, 브라우저에 Whitelabel Error Page(404) 표시

### 001-04 프로젝트 세팅, HelloController로 첫 응답 확인
- 작업: `GET /hello` → `"Hello World"` 를 반환하는 `HelloController` 추가 (책 1장 스프링부트 맛보기)
- 이유: 요청 → 컨트롤러 → 응답 흐름과 DevTools 자동 재시작이 동작하는지 확인
- 결과: `./gradlew classes` 후 DevTools가 서버를 자동 재시작(1.96초), 브라우저에 `Hello World` 표시

### 001-05 프로젝트 세팅, 완료
- 작업: README에 실행 방법(JDK 25 지정 방법) 정리
- 이유: 시스템 Java가 8이라 그냥 `gradlew`를 실행하면 실패함, 다시 와도 바로 실행할 수 있도록 기록
- 결과: 작업환경 세팅(001) 완료 — git, 프로젝트 생성, 빌드, 서버 실행, 첫 응답까지 확인

### 002-01 프로젝트 구조, 디렉터리별 역할 정리 (책 2-01)
- 작업: README에 `## 프로젝트 구조` 섹션 추가
- 이유: 2장부터 파일이 늘어나므로 각 파일을 어디에 둘지 기준을 먼저 정리, 특히 컴포넌트 스캔 범위(`com.mysite.sbb` 하위) 확인
- 책과 차이: `build.gradle` → `build.gradle.kts`(강사 방식), STS → 인텔리제이
- 결과: 코드 변경 없음, 문서 정리
