package com.mysite.sbb.domain.question.category.entity;

import com.mysite.sbb.global.jpa.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게시판 분류 (질문답변, 강좌, 자유게시판)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Category extends BaseEntity {
    @Column(unique = true, length = 50)
    private String name;

    public Category(String name) {
        this.name = name;
    }
}
