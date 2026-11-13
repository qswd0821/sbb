package com.mysite.sbb.user;

import com.mysite.sbb.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 회원 엔티티. user 는 DB 예약어인 경우가 많아 SiteUser 로 명명 (테이블명 site_user).
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 가 조회 결과로 객체를 만들 때만 사용
@Entity
public class SiteUser extends BaseEntity {
    @Column(unique = true)
    private String username;

    private String password; // BCrypt 로 암호화된 값

    @Column(unique = true)
    private String email;

    // password 는 암호화된 값을 받는다 (암호화는 PasswordEncoder 를 가진 UserService 책임)
    public SiteUser(String username, String email, String encodedPassword) {
        this.username = username;
        this.email = email;
        this.password = encodedPassword;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
