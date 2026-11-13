package com.mysite.sbb.global.jpa.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.Hibernate;
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

    // 같은 테이블의 같은 행(id)이면 같은 엔티티 (14168 37·38강)
    // 강사 코드와 다른 점
    // - getClass() 대신 Hibernate.getClass() : LAZY 프록시(Question$HibernateProxy)와 실제 객체도 같은 클래스로 본다
    // - 필드 id 대신 getId() : 프록시는 필드가 비어 있고, getter 를 호출해야 식별자를 돌려준다
    // - 저장 전(id == null) 엔티티는 자기 자신하고만 같다 (강사의 int id 는 저장 전 모두 0 → 서로 같다고 판단)
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Integer id = getId();
        return id != null && id.equals(((BaseEntity) o).getId());
    }

    // id 대신 클래스 기준 : 저장 전후로 id 가 null → 값 으로 바뀌어도 HashSet 안에서 해시가 변하지 않도록
    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
