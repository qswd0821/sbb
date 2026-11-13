package com.mysite.sbb.question;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 질문 등록 폼 데이터. 요청 파라미터(subject, content, categoryId)가 같은 이름의 필드에 바인딩된다.
 * th:object / th:field 가 getter/setter 로 값을 읽고 쓰므로 class + Lombok 사용.
 */
@Getter
@Setter
public class QuestionForm {
    @NotNull(message = "카테고리는 필수항목입니다.")
    private Integer categoryId;

    @NotEmpty(message = "제목은 필수항목입니다.")
    @Size(max = 200, message = "제목은 200자 이하로 입력해주세요.")
    private String subject;

    @NotEmpty(message = "내용은 필수항목입니다.")
    private String content;
}
