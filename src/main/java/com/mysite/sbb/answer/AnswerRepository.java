package com.mysite.sbb.answer;

import com.mysite.sbb.question.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
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
