package com.mysite.sbb;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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

    // mappedBy = "question" : 이 관계의 주인은 Answer.question (answer.question_id 칼럼을 그대로 사용)
    // PERSIST : question 이 저장(또는 영속 상태)일 때 answerList 에 추가된 새 답변도 함께 저장
    // REMOVE  : 질문 삭제 시 답변들도 함께 삭제
    // new ArrayList<>() : 새로 만든 Question 에서도 answerList.add() 가 NPE 없이 동작하도록
    @OneToMany(mappedBy = "question", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<Answer> answerList = new ArrayList<>();

    public Answer addAnswer(String content) {
        Answer answer = new Answer();
        answer.setContent(content);
        answer.setQuestion(this);
        answer.setCreateDate(LocalDateTime.now());
        answerList.add(answer);

        return answer;
    }
}
