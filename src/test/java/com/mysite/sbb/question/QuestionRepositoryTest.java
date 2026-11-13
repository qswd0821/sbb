package com.mysite.sbb.question;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerRepository;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class QuestionRepositoryTest {
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("findAll")
    void t1() {
        List<Question> questions = questionRepository.findAll();

        assertThat(questions).hasSize(2);
        assertThat(questions.get(0).getSubject()).isEqualTo("sbb가 무엇인가요?");
    }

    @Test
    @DisplayName("findById")
    void t2() {
        Question question = questionRepository.findById(1).get();

        assertThat(question.getSubject()).isEqualTo("sbb가 무엇인가요?");
    }

    @Test
    @DisplayName("findBySubject")
    void t3() {
        Question question = questionRepository.findBySubject("sbb가 무엇인가요?").get();

        assertThat(question.getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("findBySubjectAndContent")
    void t4() {
        Question question = questionRepository
                .findBySubjectAndContent("sbb가 무엇인가요?", "sbb에 대해서 알고 싶습니다.").get();

        assertThat(question.getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("findBySubjectLike")
    void t5() {
        List<Question> questions = questionRepository.findBySubjectLike("sbb%");

        assertThat(questions.get(0).getSubject()).isEqualTo("sbb가 무엇인가요?");
    }

    @Test
    @DisplayName("수정")
    void t6() {
        Question question = questionRepository.findById(1).get();
        question.setSubject("수정된 제목");
        questionRepository.save(question);

        assertThat(questionRepository.findBySubject("수정된 제목")).isPresent();
    }

    @Test
    @DisplayName("삭제")
    void t7() {
        // @Transactional 덕분에 t6의 수정, t7의 삭제는 각 테스트가 끝나면 롤백된다 → 테스트 간 독립성
        assertThat(questionRepository.count()).isEqualTo(2);

        Question question = questionRepository.findById(1).get();
        questionRepository.delete(question);

        assertThat(questionRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("답변 생성 by answerRepository")
    void t8() {
        Question question = questionRepository.findById(2).get();

        Answer answer = new Answer();
        answer.setContent("네 자동으로 생성됩니다.");
        answer.setQuestion(question);
        answerRepository.save(answer);

        assertThat(answer.getId()).isNotNull();
    }

    @Test
    @DisplayName("답변 조회 by answerRepository")
    void t9() {
        Answer answer = answerRepository.findById(1).get();

        assertThat(answer.getQuestion().getId()).isEqualTo(2);
    }

    @Test
    @DisplayName("답변 생성 by oneToMany")
    void t10() {
        Question question = questionRepository.findById(2).get();
        int beforeCount = question.getAnswerList().size();

        Answer newAnswer = question.addAnswer("네 자동으로 생성됩니다.", null);

        // answerRepository.save 를 호출하지 않았다.
        // 아직 INSERT 전이라 id 는 null, flush(또는 커밋) 시점에 PERSIST cascade + 더티체킹으로 INSERT 된다.
        assertThat(newAnswer.getId()).isNull();
        questionRepository.flush();
        assertThat(newAnswer.getId()).isNotNull();

        assertThat(question.getAnswerList()).hasSize(beforeCount + 1);
    }

    @Test
    @DisplayName("답변 조회 by oneToMany")
    void t11() {
        // LAZY 라서 getAnswerList() 시점에 SELECT 가 실행된다.
        // 클래스의 @Transactional 덕분에 DB 연결(영속성 컨텍스트)이 유지되어 LazyInitializationException 이 나지 않는다.
        Question question = questionRepository.findById(2).get();

        List<Answer> answerList = question.getAnswerList();

        assertThat(answerList).hasSize(1);
        assertThat(answerList.get(0).getContent()).isEqualTo("네 자동으로 생성됩니다.");
    }

    @Test
    @DisplayName("JPA Auditing : 저장 시 createDate 자동, modifyDate 는 수정할 때만")
    void t12() {
        Question question = new Question();
        question.setSubject("auditing");
        question.setContent("createDate 를 직접 넣지 않는다");
        questionRepository.saveAndFlush(question);

        assertThat(question.getCreateDate()).isNotNull();
        assertThat(question.getModifyDate()).isNull(); // modifyOnCreate = false

        question.setContent("수정");
        questionRepository.flush(); // 더티체킹 UPDATE → @LastModifiedDate

        assertThat(question.getModifyDate()).isNotNull();
    }

    @Test
    @DisplayName("JPA Auditing : 추천(중간 테이블 INSERT)만으로는 modifyDate 가 바뀌지 않는다")
    void t13() {
        SiteUser user = new SiteUser();
        user.setUsername("voter");
        user.setEmail("voter@test.com");
        user.setPassword("x");
        userRepository.save(user);

        Question question = questionRepository.findById(1).get();
        question.getVoter().add(user);
        questionRepository.flush();

        assertThat(question.getModifyDate()).isNull();
    }

    @Test
    @DisplayName("equals/hashCode : 같은 행이면 다른 객체·LAZY 프록시여도 같다, 저장 전 엔티티끼리는 다르다")
    void t14() {
        Question loaded = questionRepository.findById(2).get();
        em.clear(); // 영속성 컨텍스트를 비워 같은 행을 다른 객체로 다시 얻는다

        Question proxy = questionRepository.getReferenceById(2); // 실제 조회 없이 프록시
        assertThat(Hibernate.isInitialized(proxy)).isFalse();
        assertThat(proxy).isNotSameAs(loaded);
        assertThat(proxy).isEqualTo(loaded);
        assertThat(loaded).isEqualTo(proxy);
        assertThat(Set.of(loaded)).contains(proxy);

        // 저장 전(id null) 엔티티 두 개가 HashSet 에서 하나로 합쳐지지 않는다
        Set<Question> set = new HashSet<>();
        set.add(new Question());
        set.add(new Question());
        assertThat(set).hasSize(2);
    }

    @Test
    @DisplayName("orphanRemoval : answerList 에서 빼기만 해도 답변이 DELETE 된다")
    void t15() {
        Question question = questionRepository.findById(2).get();
        Answer answer = answerRepository.findById(1).get();

        assertThat(question.deleteAnswer(answer)).isTrue();
        questionRepository.flush();

        assertThat(answerRepository.findById(1)).isEmpty();
    }
}
