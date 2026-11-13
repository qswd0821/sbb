package com.mysite.sbb.user;

import com.mysite.sbb.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

/**
 * 회원 엔티티. user 는 DB 예약어인 경우가 많아 SiteUser 로 명명 (테이블명 site_user).
 */
@Getter
@Setter
@Entity
public class SiteUser extends BaseEntity {
    @Column(unique = true)
    private String username;

    private String password; // BCrypt 로 암호화된 값

    @Column(unique = true)
    private String email;
}
