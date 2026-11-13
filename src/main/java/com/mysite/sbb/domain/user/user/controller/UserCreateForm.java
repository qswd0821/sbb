package com.mysite.sbb.domain.user.user.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UserCreateForm(
        @Size(min = 3, max = 25, message = "사용자ID는 3자 이상 25자 이하로 입력해주세요.")
        @NotEmpty(message = "사용자ID는 필수항목입니다.")
        String username,

        @NotEmpty(message = "비밀번호는 필수항목입니다.")
        String password1,

        @NotEmpty(message = "비밀번호 확인은 필수항목입니다.")
        String password2,

        @NotEmpty(message = "이메일은 필수항목입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email
) {
}
