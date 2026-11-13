package com.mysite.sbb.domain.question.answer.controller;

import jakarta.validation.constraints.NotEmpty;

public record AnswerForm(
        @NotEmpty(message = "내용은 필수항목입니다.")
        String content
) {
}
