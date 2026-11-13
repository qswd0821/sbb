package com.mysite.sbb.answer;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnswerService {

    // 강사 방식: answerRepository.save 대신 question.addAnswer() (OneToMany + CascadeType.PERSIST)
    // 트랜잭션 안에서 영속 상태인 question 의 answerList 에 추가 → 커밋 시 INSERT
    @Transactional
    public Answer create(Question question, String content, SiteUser author) {
        return question.addAnswer(content, author);
    }
}
