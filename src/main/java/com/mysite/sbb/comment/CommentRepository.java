package com.mysite.sbb.comment;

import com.mysite.sbb.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    // 프로필 : 특정 사용자가 쓴 최근 댓글 10개, 전체 개수
    List<Comment> findTop10ByAuthorOrderByCreateDateDesc(SiteUser author);

    long countByAuthor(SiteUser author);

    // 최근 댓글 10개 (전체 사용자)
    List<Comment> findTop10ByOrderByCreateDateDesc();
}
