package com.mysite.sbb.domain.user.user.service;

import com.mysite.sbb.domain.user.user.entity.SiteUser;
import com.mysite.sbb.domain.user.user.repository.UserRepository;
import com.mysite.sbb.global.exception.DataNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SiteUser create(String username, String email, String password) {
        return userRepository.save(new SiteUser(username, email, passwordEncoder.encode(password))); // 평문 저장 금지
    }

    public boolean isPasswordMatch(SiteUser user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword()); // 해시끼리 비교 (평문 복원 불가)
    }

    public void changePassword(SiteUser user, String newPassword) {
        user.changePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public SiteUser getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("siteuser not found"));
    }
}
