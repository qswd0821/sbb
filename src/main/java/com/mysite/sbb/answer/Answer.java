package com.mysite.sbb.answer;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createDate;

    // LAZY : answer 조회 시 question 은 실제로 쓰는 순간에 조회 (기본값 EAGER 는 항상 JOIN/추가 조회)
    @ManyToOne(fetch = FetchType.LAZY)
    private Question question;

    // 글쓴이
    @ManyToOne(fetch = FetchType.LAZY)
    private SiteUser author;
}
