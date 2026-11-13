package com.mysite.sbb.answer;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
