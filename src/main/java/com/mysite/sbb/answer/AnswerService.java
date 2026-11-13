package com.mysite.sbb.answer;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;

    // 강사 방식: answerRepository.save 대신 question.addAnswer() (OneToMany + CascadeType.PERSIST)
    // 트랜잭션 안에서 영속 상태인 question 의 answerList 에 추가 → 커밋 시 INSERT
    @Transactional
    public Answer create(Question question, String content, SiteUser author) {
        return question.addAnswer(content, author);
    }

    public Answer getAnswer(Integer id) {
        return answerRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("answer not found"));
    }

    public void modify(Answer answer, String content) {
        answer.setContent(content);
        answer.setModifyDate(LocalDateTime.now());
        answerRepository.save(answer);
    }

    public void vote(Answer answer, SiteUser siteUser) {
        answer.getVoter().add(siteUser);
        answerRepository.save(answer);
    }

    public void delete(Answer answer) {
        answerRepository.delete(answer);
    }
}
