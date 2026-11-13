package com.mysite.sbb;

import com.mysite.sbb.category.Category;
import com.mysite.sbb.category.CategoryRepository;
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

import java.util.List;

/**
 * 샘플 데이터 생성기 (dev, test 공통). 이미 데이터가 있으면 해당 부분은 건너뛴다.
 * 테스트는 메모리 DB라 항상 같은 데이터(카테고리 3개, 질문 2개, 답변 1개)에서 출발하고, dev는 화면 확인용 데이터가 생긴다.
 */
@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    public static final List<String> CATEGORY_NAMES = List.of("질문답변", "강좌", "자유게시판");

    @Autowired
    @Lazy
    private BaseInitData self; // @Transactional 이 프록시를 거쳐 적용되도록 자기 자신을 프록시로 주입
    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;

    @Bean
    @Order(1) // DevInitData(2) 보다 먼저 실행
    public ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.work0();
            self.work1();
        };
    }

    // 카테고리 (3-14) : 기존 dev DB 에도 생기도록 질문 데이터와 별도로 검사
    // 카테고리가 없던 시절의 질문은 "질문답변" 으로 옮긴다 (이미 옮겼으면 0건 변경)
    @Transactional
    public void work0() {
        if (categoryRepository.count() == 0) {
            CATEGORY_NAMES.forEach(name -> categoryRepository.save(new Category(name)));
        }
        questionRepository.assignCategoryWhereNull(categoryRepository.findByName("질문답변").orElseThrow());
    }

    @Transactional
    public void work1() {
        if (questionRepository.count() > 0) return;

        Category qna = categoryRepository.findByName("질문답변").orElseThrow();

        Question q1 = new Question("sbb가 무엇인가요?", "sbb에 대해서 알고 싶습니다.", null, qna);
        questionRepository.save(q1);

        Question q2 = new Question("스프링부트 모델 질문입니다.", "id는 자동으로 생성되나요?", null, qna);
        q2.addAnswer("네 자동으로 생성됩니다.", null); // CascadeType.PERSIST 로 질문 저장 시 답변도 저장
        questionRepository.save(q2);
    }
}
