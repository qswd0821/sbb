package com.mysite.sbb;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 모든 엔티티의 공통 부모 (강사 14168 BaseEntity).
 * - @MappedSuperclass : 이 클래스 자체는 테이블이 아니고, 필드만 자식 테이블의 칼럼이 된다
 * - AuditingEntityListener : 저장 시 @CreatedDate, 수정(UPDATE) 시 @LastModifiedDate 를 자동으로 채운다
 *   (SbbApplication 의 @EnableJpaAuditing 필요)
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @CreatedDate
    private LocalDateTime createDate;

    // 수정일시. modifyOnCreate = false 라서 처음 저장할 때는 null → 화면의 "수정됨" 표시 조건(!= null)이 그대로 동작
    @LastModifiedDate
    private LocalDateTime modifyDate;
}
