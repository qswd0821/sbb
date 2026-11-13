package com.mysite.sbb.comment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CommentForm(
        @NotEmpty(message = "댓글 내용은 필수항목입니다.")
        @Size(max = 500, message = "댓글은 500자 이하로 입력해주세요.")
        String content
) {
}
