package com.mysite.sbb.domain.question.answer.repository;

import com.mysite.sbb.domain.question.answer.entity.Answer;
import com.mysite.sbb.domain.question.question.entity.Question;
import com.mysite.sbb.domain.user.user.entity.SiteUser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    // 프로필 : 특정 사용자가 쓴 최근 답변 10개, 전체 개수
    List<Answer> findTop10ByAuthorOrderByCreateDateDesc(SiteUser author);

    long countByAuthor(SiteUser author);

    // 최근 답변 10개 (전체 사용자)
    List<Answer> findTop10ByOrderByCreateDateDesc();

    // 최신순 등 Pageable 의 Sort 로 정렬 가능한 경우
    Page<Answer> findByQuestion(Question question, Pageable pageable);

    // 추천순 : 추천인(voter) 수로 정렬해야 하므로 group by + count 를 직접 작성
    @Query(value = """
            select a
            from Answer a
            left join a.voter v
            where a.question = :question
            group by a
            order by count(v) desc, a.createDate desc
            """,
            countQuery = "select count(a) from Answer a where a.question = :question")
    Page<Answer> findByQuestionOrderByVoterCount(@Param("question") Question question, Pageable pageable);
}
