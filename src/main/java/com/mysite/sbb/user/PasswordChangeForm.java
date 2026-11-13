package com.mysite.sbb.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeForm {
    @NotEmpty(message = "현재 비밀번호는 필수항목입니다.")
    private String currentPassword;

    @NotEmpty(message = "새 비밀번호는 필수항목입니다.")
    @Size(min = 4, message = "새 비밀번호는 4자 이상으로 입력해주세요.")
    private String newPassword1;

    @NotEmpty(message = "새 비밀번호 확인은 필수항목입니다.")
    private String newPassword2;
}
