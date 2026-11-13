package com.mysite.sbb.question;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.category.Category;
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
public class QuestionService {
    private final QuestionRepository questionRepository;

    // page: 0부터 시작하는 페이지 번호, 한 페이지 10개, 최신 글이 먼저
    // kw : 검색어 (빈 문자열이면 like '%%' 로 전체), category : null 이면 전체 카테고리
    public Page<Question> getList(int page, String kw, Category category) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Order.desc("createDate")));
        return questionRepository.findAllByKeyword(kw, category, pageable);
    }

    public Question getQuestion(Integer id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("question not found"));
    }

    @Transactional // @Modifying 쿼리는 트랜잭션 안에서 실행해야 한다
    public void increaseViewCount(Integer id) {
        questionRepository.increaseViewCount(id);
    }

    public List<Question> getRecentByAuthor(SiteUser author) {
        return questionRepository.findTop10ByAuthorOrderByCreateDateDesc(author);
    }

    public long countByAuthor(SiteUser author) {
        return questionRepository.countByAuthor(author);
    }

    public long count() {
        return questionRepository.count();
    }

    public Question create(String subject, String content, SiteUser author, Category category) {
        return questionRepository.save(new Question(subject, content, author, category));
    }

    // save 없이 더티체킹 : 트랜잭션이 끝날 때 바뀐 필드만 UPDATE
    @Transactional
    public void modify(Question question, String subject, String content, Category category) {
        question.modify(subject, content, category);
    }

    @Transactional
    public void vote(Question question, SiteUser siteUser) {
        question.vote(siteUser);
    }

    public void delete(Question question) {
        questionRepository.delete(question); // cascade REMOVE 로 답변도 함께 삭제
    }
}
