package com.mysite.sbb.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/user")
@Controller
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            // 특정 필드 오류로 등록 → form_errors 프래그먼트에 함께 표시, password2 에 focus
            bindingResult.rejectValue("password2", "passwordInCorrect", "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }

        try {
            userService.create(userCreateForm.getUsername(), userCreateForm.getEmail(), userCreateForm.getPassword1());
        } catch (DataIntegrityViolationException e) {
            // username, email 의 unique 제약 위반
            bindingResult.reject("signupFailed", "이미 등록된 사용자ID 또는 이메일입니다.");
            return "signup_form";
        }

        return "redirect:/";
    }

    // 로그인 화면만 보여준다. POST /user/login 처리는 스프링 시큐리티가 담당
    @GetMapping("/login")
    public String login() {
        return "login_form";
    }
}
