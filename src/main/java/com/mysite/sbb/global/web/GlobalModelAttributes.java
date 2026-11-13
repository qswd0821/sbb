package com.mysite.sbb.global.web;

import com.mysite.sbb.domain.question.category.entity.Category;
import com.mysite.sbb.domain.question.category.service.CategoryService;
import com.mysite.sbb.global.util.CommonUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * 모든 컨트롤러의 템플릿에 공통으로 넣을 변수 (14168 19강: @ModelAttribute 메서드를 공통으로 두는 방법).
 *
 * 책은 템플릿에서 ${@commonUtil.markdown(...)} 처럼 빈을 직접 참조하지만,
 * Thymeleaf 3.1 은 보안상 th:utext 등에서 @빈 참조를 제한하므로 모델 변수로 넘긴다.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {
    private final CommonUtil commonUtil;
    private final CategoryService categoryService;

    @ModelAttribute("commonUtil")
    public CommonUtil commonUtil() {
        return commonUtil;
    }

    // 내비게이션 바 카테고리 메뉴, 질문 폼의 카테고리 선택에서 사용 (3-14 카테고리)
    @ModelAttribute("categoryList")
    public List<Category> categoryList() {
        return categoryService.getList();
    }
}
