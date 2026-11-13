package com.mysite.sbb.domain.user.user.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record PasswordChangeForm(
        @NotEmpty(message = "현재 비밀번호는 필수항목입니다.")
        String currentPassword,

        @NotEmpty(message = "새 비밀번호는 필수항목입니다.")
        @Size(min = 4, message = "새 비밀번호는 4자 이상으로 입력해주세요.")
        String newPassword1,

        @NotEmpty(message = "새 비밀번호 확인은 필수항목입니다.")
        String newPassword2
) {
}
