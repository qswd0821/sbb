package com.mysite.sbb.question;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;

    // page: 0부터 시작하는 페이지 번호, 한 페이지 10개, 최신 글이 먼저
    public Page<Question> getList(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Order.desc("createDate")));
        return questionRepository.findAll(pageable);
    }

    public Question getQuestion(Integer id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("question not found"));
    }

    public long count() {
        return questionRepository.count();
    }

    public Question create(String subject, String content, SiteUser author) {
        Question q = new Question();
        q.setSubject(subject);
        q.setContent(content);
        q.setCreateDate(LocalDateTime.now());
        q.setAuthor(author);
        return questionRepository.save(q);
    }

    public void modify(Question question, String subject, String content) {
        question.setSubject(subject);
        question.setContent(content);
        question.setModifyDate(LocalDateTime.now());
        questionRepository.save(question);
    }

    public void vote(Question question, SiteUser siteUser) {
        question.getVoter().add(siteUser); // Set 이라 같은 사용자가 여러 번 추천해도 1번만 반영
        questionRepository.save(question);
    }

    public void delete(Question question) {
        questionRepository.delete(question); // cascade REMOVE 로 답변도 함께 삭제
    }
}
