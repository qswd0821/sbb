package com.mysite.sbb.domain.question.answer.service;

import com.mysite.sbb.domain.question.answer.entity.Answer;
import com.mysite.sbb.domain.question.answer.repository.AnswerRepository;
import com.mysite.sbb.domain.question.question.entity.Question;
import com.mysite.sbb.domain.user.user.entity.SiteUser;
import com.mysite.sbb.global.exception.DataNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public static final int ANSWER_PAGE_SIZE = 5;

    // sort : "vote"(추천순) 또는 그 외(최신순)
    public Page<Answer> getList(Question question, int page, String sort) {
        if ("vote".equals(sort)) {
            return answerRepository.findByQuestionOrderByVoterCount(question, PageRequest.of(page, ANSWER_PAGE_SIZE));
        }
        Pageable pageable = PageRequest.of(page, ANSWER_PAGE_SIZE, Sort.by(Sort.Order.desc("createDate")));
        return answerRepository.findByQuestion(question, pageable);
    }

    public List<Answer> getRecentByAuthor(SiteUser author) {
        return answerRepository.findTop10ByAuthorOrderByCreateDateDesc(author);
    }

    public long countByAuthor(SiteUser author) {
        return answerRepository.countByAuthor(author);
    }

    public List<Answer> getRecentList() {
        return answerRepository.findTop10ByOrderByCreateDateDesc();
    }

    public Answer getAnswer(Integer id) {
        return answerRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("answer not found"));
    }

    // URL 이 /questions/{questionId}/answers/{id} 이므로, 답변이 그 질문에 속하는지까지 확인
    // (예: /questions/1/answers/44 인데 44번 답변이 368번 질문 소속이면 404)
    // LAZY 프록시의 getId() 는 추가 조회 없이 FK 값만 사용
    public Answer getAnswer(Integer questionId, Integer id) {
        Answer answer = getAnswer(id);
        if (!answer.getQuestion().getId().equals(questionId)) {
            throw new DataNotFoundException("answer not found");
        }
        return answer;
    }

    @Transactional
    public void modify(Answer answer, String content) {
        answer.modify(content);
    }

    @Transactional
    public void vote(Answer answer, SiteUser siteUser) {
        answer.vote(siteUser);
    }

    // 리포지터리 delete 대신 부모 컬렉션에서 제거 (orphanRemoval). 트랜잭션이 끝날 때 DELETE 실행
    @Transactional
    public void delete(Answer answer) {
        answer.getQuestion().deleteAnswer(answer);
    }
}
