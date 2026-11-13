package com.mysite.sbb.comment;

import com.mysite.sbb.BaseEntity;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * 댓글. 질문에 달리면 question, 답변에 달리면 answer 가 채워진다 (둘 중 하나).
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 가 조회 결과로 객체를 만들 때만 사용
@Entity
public class Comment extends BaseEntity {
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private SiteUser author;

    @ManyToOne(fetch = FetchType.LAZY)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    private Answer answer;

    // 질문 댓글 / 답변 댓글 : 생성자에서 부모를 하나만 받아 "둘 중 하나" 규칙을 지킨다
    public Comment(Question question, String content, SiteUser author) {
        this.question = question;
        this.content = content;
        this.author = author;
    }

    public Comment(Answer answer, String content, SiteUser author) {
        this.answer = answer;
        this.content = content;
        this.author = author;
    }

    // 부모(질문 또는 답변)의 댓글 목록에서 자신을 뺀다 → orphanRemoval 로 DELETE
    public boolean removeFromParent() {
        return question != null ? question.deleteComment(this) : answer.deleteComment(this);
    }

    // 댓글이 속한 질문의 id (답변 댓글이면 답변의 질문) → 처리 후 돌아갈 상세 페이지
    public Integer getQuestionId() {
        return question != null ? question.getId() : answer.getQuestion().getId();
    }
}
