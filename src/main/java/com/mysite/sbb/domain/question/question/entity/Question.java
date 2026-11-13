package com.mysite.sbb.domain.question.question.entity;

import com.mysite.sbb.domain.question.answer.entity.Answer;
import com.mysite.sbb.domain.question.category.entity.Category;
import com.mysite.sbb.domain.question.comment.entity.Comment;
import com.mysite.sbb.domain.user.user.entity.SiteUser;
import com.mysite.sbb.global.jpa.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 가 조회 결과로 객체를 만들 때만 사용
@Entity
public class Question extends BaseEntity {
    @Column(length = 200)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 조회 수 (3-14). 기존 행에도 0 이 채워지도록 DB 기본값 지정
    @ColumnDefault("0")
    @Column(nullable = false)
    private int viewCount;

    // 게시판 분류 (3-14 카테고리), 기존 데이터는 null 허용
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    // 글쓴이 (여러 질문 → 한 사용자), 기존 데이터는 null 허용
    @ManyToOne(fetch = FetchType.LAZY)
    private SiteUser author;

    // 추천인 (한 사용자는 한 번만 → Set). 중간 테이블(question_voter / answer_voter)이 생긴다
    @ManyToMany
    private Set<SiteUser> voter = new HashSet<>();

    // 이 질문에 달린 댓글 (질문 삭제 시 함께 삭제, 3-14 댓글)
    // orphanRemoval : 컬렉션에서 빼기만 해도(commentList.remove) DELETE
    @OneToMany(mappedBy = "question", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @OrderBy("createDate asc")
    private List<Comment> commentList = new ArrayList<>();

    // mappedBy = "question" : 이 관계의 주인은 Answer.question (answer.question_id 칼럼을 그대로 사용)
    // PERSIST : question 이 저장(또는 영속 상태)일 때 answerList 에 추가된 새 답변도 함께 저장
    // REMOVE  : 질문 삭제 시 답변들도 함께 삭제
    // new ArrayList<>() : 새로 만든 Question 에서도 answerList.add() 가 NPE 없이 동작하도록
    // orphanRemoval : answerList 에서 빠진 답변(고아)은 DELETE (강사 14168 Post.comments)
    @OneToMany(mappedBy = "question", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    private List<Answer> answerList = new ArrayList<>();

    public Question(String subject, String content, SiteUser author, Category category) {
        this.subject = subject;
        this.content = content;
        this.author = author;
        this.category = category;
    }

    // 수정 : 바꿀 수 있는 필드만 한 메서드로 (수정일시는 @LastModifiedDate 가 자동 기록)
    public void modify(String subject, String content, Category category) {
        this.subject = subject;
        this.content = content;
        this.category = category;
    }

    // 추천 : Set 이라 같은 사용자가 여러 번 추천해도 1번만 반영 (새로 추가됐으면 true)
    public boolean vote(SiteUser siteUser) {
        return voter.add(siteUser);
    }

    public Answer addAnswer(String content, SiteUser author) {
        Answer answer = new Answer(this, content, author);
        answerList.add(answer);

        return answer;
    }

    // 답변 삭제 : 컬렉션에서 빼면 orphanRemoval 로 DELETE (List.remove 는 equals 로 찾는다 → BaseEntity.equals)
    public boolean deleteAnswer(Answer answer) {
        return answerList.remove(answer);
    }

    // 댓글 등록 : commentList 의 PERSIST cascade 로 저장
    public Comment addComment(String content, SiteUser author) {
        Comment comment = new Comment(this, content, author);
        commentList.add(comment);

        return comment;
    }

    public boolean deleteComment(Comment comment) {
        return commentList.remove(comment);
    }
}
