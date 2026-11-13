package com.mysite.sbb.question;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.category.Category;
import com.mysite.sbb.comment.Comment;
import com.mysite.sbb.user.SiteUser;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 200)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createDate;

    private LocalDateTime modifyDate; // 수정일시 (수정한 적이 없으면 null)

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
    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
    @OrderBy("createDate asc")
    private List<Comment> commentList = new ArrayList<>();

    // mappedBy = "question" : 이 관계의 주인은 Answer.question (answer.question_id 칼럼을 그대로 사용)
    // PERSIST : question 이 저장(또는 영속 상태)일 때 answerList 에 추가된 새 답변도 함께 저장
    // REMOVE  : 질문 삭제 시 답변들도 함께 삭제
    // new ArrayList<>() : 새로 만든 Question 에서도 answerList.add() 가 NPE 없이 동작하도록
    @OneToMany(mappedBy = "question", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<Answer> answerList = new ArrayList<>();

    public Answer addAnswer(String content, SiteUser author) {
        Answer answer = new Answer();
        answer.setContent(content);
        answer.setQuestion(this);
        answer.setAuthor(author);
        answer.setCreateDate(LocalDateTime.now());
        answerList.add(answer);

        return answer;
    }
}
