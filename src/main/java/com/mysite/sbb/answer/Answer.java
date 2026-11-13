package com.mysite.sbb.answer;

import com.mysite.sbb.BaseEntity;
import com.mysite.sbb.comment.Comment;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
public class Answer extends BaseEntity {
    @Column(columnDefinition = "TEXT")
    private String content;

    // LAZY : answer 조회 시 question 은 실제로 쓰는 순간에 조회 (기본값 EAGER 는 항상 JOIN/추가 조회)
    @ManyToOne(fetch = FetchType.LAZY)
    private Question question;

    // 글쓴이
    @ManyToOne(fetch = FetchType.LAZY)
    private SiteUser author;

    // 추천인 (한 사용자는 한 번만 → Set). 중간 테이블(question_voter / answer_voter)이 생긴다
    @ManyToMany
    private Set<SiteUser> voter = new HashSet<>();

    // 이 답변에 달린 댓글 (답변 삭제 시 함께 삭제, 3-14 댓글)
    @OneToMany(mappedBy = "answer", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @OrderBy("createDate asc")
    private List<Comment> commentList = new ArrayList<>();

    public boolean deleteComment(Comment comment) {
        return commentList.remove(comment);
    }
}
