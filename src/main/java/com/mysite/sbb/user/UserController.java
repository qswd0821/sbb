package com.mysite.sbb.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

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

    // 비밀번호 변경 (3-14) : 로그인한 사용자가 현재 비밀번호를 확인한 뒤 변경
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/password")
    public String passwordChange(PasswordChangeForm passwordChangeForm) {
        return "password_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/password")
    public String passwordChange(@Valid PasswordChangeForm passwordChangeForm, BindingResult bindingResult,
                                 Principal principal, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return "password_form";
        }
        SiteUser user = userService.getUser(principal.getName());
        if (!userService.isPasswordMatch(user, passwordChangeForm.getCurrentPassword())) {
            bindingResult.rejectValue("currentPassword", "passwordInCorrect", "현재 비밀번호가 일치하지 않습니다.");
            return "password_form";
        }
        if (!passwordChangeForm.getNewPassword1().equals(passwordChangeForm.getNewPassword2())) {
            bindingResult.rejectValue("newPassword2", "passwordInCorrect", "2개의 새 비밀번호가 일치하지 않습니다.");
            return "password_form";
        }
        userService.changePassword(user, passwordChangeForm.getNewPassword1());
        ra.addFlashAttribute("message", "비밀번호가 변경되었습니다.");
        return "redirect:/user/password";
    }

    // 로그인 화면만 보여준다. POST /user/login 처리는 스프링 시큐리티가 담당
    @GetMapping("/login")
    public String login() {
        return "login_form";
    }
}
