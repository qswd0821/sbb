package com.mysite.sbb.question;

import com.mysite.sbb.category.Category;
import com.mysite.sbb.user.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    // 메서드 이름 규칙으로 쿼리 자동 생성
    Optional<Question> findBySubject(String subject);                               // WHERE subject = ?

    Optional<Question> findBySubjectAndContent(String subject, String content);     // WHERE subject = ? AND content = ?

    List<Question> findBySubjectLike(String subject);                               // WHERE subject LIKE ?

    // 프로필 : 특정 사용자가 쓴 최근 질문 10개, 전체 개수
    List<Question> findTop10ByAuthorOrderByCreateDateDesc(SiteUser author);

    long countByAuthor(SiteUser author);

    // 조회 수 +1 : 읽고-더하고-저장하는 대신 DB 에서 한 번에 증가 → 동시에 여러 명이 봐도 누락되지 않음
    @Modifying
    @Query("update Question q set q.viewCount = q.viewCount + 1 where q.id = :id")
    int increaseViewCount(@Param("id") Integer id);

    // 카테고리 도입 전에 만들어진 질문(category null)을 기본 카테고리로 일괄 변경
    @Modifying
    @Query("update Question q set q.category = :category where q.category is null")
    int assignCategoryWhereNull(@Param("category") Category category);

    // 검색 : 질문 제목/내용/글쓴이 + 답변 내용/글쓴이 중 하나라도 kw 를 포함하면 결과에 포함
    // 답변이 여러 개면 같은 질문이 여러 줄 나오므로 distinct
    // category 가 null 이면 전체, 있으면 해당 카테고리만 (3-14 카테고리)
    @Query("""
            select distinct q
            from Question q
            left outer join SiteUser u1 on q.author = u1
            left outer join Answer a on a.question = q
            left outer join SiteUser u2 on a.author = u2
            where
                (:category is null or q.category = :category)
                and (
                    q.subject like %:kw%
                    or q.content like %:kw%
                    or u1.username like %:kw%
                    or a.content like %:kw%
                    or u2.username like %:kw%
                )
            """)
    Page<Question> findAllByKeyword(@Param("kw") String kw, @Param("category") Category category, Pageable pageable);
}
