package com.mysite.sbb;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * test 프로파일에서만 동작하는 샘플 데이터 생성기.
 * 테스트가 항상 같은 데이터(질문 2개, 답변 1개)에서 출발하도록 한다.
 */
@Profile("test")
@Configuration
@RequiredArgsConstructor
public class TestInitData {
    @Autowired
    @Lazy
    private TestInitData self; // @Transactional 이 프록시를 거쳐 적용되도록 자기 자신을 프록시로 주입
    private final QuestionRepository questionRepository;

    @Bean
    public ApplicationRunner testInitDataApplicationRunner() {
        return args -> self.work1();
    }

    @Transactional
    public void work1() {
        if (questionRepository.count() > 0) return;

        Question q1 = new Question();
        q1.setSubject("sbb가 무엇인가요?");
        q1.setContent("sbb에 대해서 알고 싶습니다.");
        q1.setCreateDate(LocalDateTime.now());
        questionRepository.save(q1);

        Question q2 = new Question();
        q2.setSubject("스프링부트 모델 질문입니다.");
        q2.setContent("id는 자동으로 생성되나요?");
        q2.setCreateDate(LocalDateTime.now());
        q2.addAnswer("네 자동으로 생성됩니다."); // CascadeType.PERSIST 로 질문 저장 시 답변도 저장
        questionRepository.save(q2);
    }
}
