package com.mysite.sbb.domain.question.question.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 질문 등록 폼 데이터. 요청 파라미터(subject, content, categoryId)가 같은 이름의 필드에 바인딩된다.
 * record : 요청 파라미터는 생성자(레코드 컴포넌트)로 바인딩되고, th:field 는 접근자 subject() 로 값을 읽는다. (14168 40강)
 * 만든 뒤에는 바뀌지 않는다(불변) → 수정 폼의 기존 값도 생성자로 채운다.
 */
public record QuestionForm(
        @NotNull(message = "카테고리는 필수항목입니다.")
        Integer categoryId,

        @NotEmpty(message = "제목은 필수항목입니다.")
        @Size(max = 200, message = "제목은 200자 이하로 입력해주세요.")
        String subject,

        @NotEmpty(message = "내용은 필수항목입니다.")
        String content
) {
}
