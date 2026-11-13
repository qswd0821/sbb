package com.mysite.sbb.answer;

import jakarta.validation.constraints.NotEmpty;

public record AnswerForm(
        @NotEmpty(message = "내용은 필수항목입니다.")
        String content
) {
}
