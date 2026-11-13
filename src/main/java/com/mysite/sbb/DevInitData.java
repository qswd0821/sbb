package com.mysite.sbb;

import com.mysite.sbb.question.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

/**
 * dev 프로파일 전용: 페이징 확인용 질문을 300개까지 채운다.
 * (책 3-02는 테스트 코드로 300개를 넣지만, 테스트는 메모리 DB라 dev 화면에는 보이지 않으므로 dev 초기 데이터로 생성)
 */
@Profile("dev")
@Configuration
@RequiredArgsConstructor
public class DevInitData {
    private static final int TARGET_COUNT = 300;

    private final QuestionService questionService;

    @Bean
    @Order(2)
    public ApplicationRunner devInitDataApplicationRunner() {
        return args -> {
            long count = questionService.count();
            for (long i = count + 1; i <= TARGET_COUNT; i++) {
                questionService.create("테스트 데이터입니다:[%03d]".formatted(i), "내용무");
            }
        };
    }
}
