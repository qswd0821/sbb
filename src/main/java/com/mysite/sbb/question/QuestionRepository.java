package com.mysite.sbb.question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    // 메서드 이름 규칙으로 쿼리 자동 생성
    Optional<Question> findBySubject(String subject);                               // WHERE subject = ?

    Optional<Question> findBySubjectAndContent(String subject, String content);     // WHERE subject = ? AND content = ?

    List<Question> findBySubjectLike(String subject);                               // WHERE subject LIKE ?

    // 검색 : 질문 제목/내용/글쓴이 + 답변 내용/글쓴이 중 하나라도 kw 를 포함하면 결과에 포함
    // 답변이 여러 개면 같은 질문이 여러 줄 나오므로 distinct
    @Query("""
            select distinct q
            from Question q
            left outer join SiteUser u1 on q.author = u1
            left outer join Answer a on a.question = q
            left outer join SiteUser u2 on a.author = u2
            where
                q.subject like %:kw%
                or q.content like %:kw%
                or u1.username like %:kw%
                or a.content like %:kw%
                or u2.username like %:kw%
            """)
    Page<Question> findAllByKeyword(@Param("kw") String kw, Pageable pageable);
}
