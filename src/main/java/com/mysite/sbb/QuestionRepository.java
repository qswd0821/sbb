package com.mysite.sbb;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    // 메서드 이름 규칙으로 쿼리 자동 생성
    Optional<Question> findBySubject(String subject);                               // WHERE subject = ?

    Optional<Question> findBySubjectAndContent(String subject, String content);     // WHERE subject = ? AND content = ?

    List<Question> findBySubjectLike(String subject);                               // WHERE subject LIKE ?
}
