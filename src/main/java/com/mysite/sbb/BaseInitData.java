package com.mysite.sbb;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 샘플 데이터 생성기 (dev, test 공통). 데이터가 하나라도 있으면 건너뛴다.
 * 테스트는 메모리 DB라 항상 같은 데이터(질문 2개, 답변 1개)에서 출발하고, dev는 화면 확인용 데이터가 생긴다.
 */
@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    @Autowired
    @Lazy
    private BaseInitData self; // @Transactional 이 프록시를 거쳐 적용되도록 자기 자신을 프록시로 주입
    private final QuestionRepository questionRepository;

    @Bean
    @Order(1) // DevInitData(2) 보다 먼저 실행
    public ApplicationRunner baseInitDataApplicationRunner() {
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
        q2.addAnswer("네 자동으로 생성됩니다.", null); // CascadeType.PERSIST 로 질문 저장 시 답변도 저장
        questionRepository.save(q2);
    }
}
