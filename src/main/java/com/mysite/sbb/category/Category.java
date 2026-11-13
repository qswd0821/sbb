package com.mysite.sbb.category;

import com.mysite.sbb.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 게시판 분류 (질문답변, 강좌, 자유게시판)
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
public class Category extends BaseEntity {
    @Column(unique = true, length = 50)
    private String name;

    public Category(String name) {
        this.name = name;
    }
}
