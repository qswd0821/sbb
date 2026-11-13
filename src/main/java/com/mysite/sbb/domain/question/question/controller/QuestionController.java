package com.mysite.sbb.domain.question.question.controller;

import com.mysite.sbb.domain.question.answer.controller.AnswerForm;
import com.mysite.sbb.domain.question.answer.service.AnswerService;
import com.mysite.sbb.domain.question.category.entity.Category;
import com.mysite.sbb.domain.question.category.service.CategoryService;
import com.mysite.sbb.domain.question.question.entity.Question;
import com.mysite.sbb.domain.question.question.service.QuestionService;
import com.mysite.sbb.domain.user.user.entity.SiteUser;
import com.mysite.sbb.domain.user.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

// 자원(질문 목록) 중심 URL (14163 37강 URL 표, 14168) : /questions, /questions/{id}, /questions/{id}/modify ...
@RequestMapping("/questions")
@Controller
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;
    private final UserService userService;
    private final AnswerService answerService;
    private final CategoryService categoryService;

    // /questions?page=0 형태, page 파라미터가 없으면 0 (첫 페이지)
    @GetMapping
    // category : 카테고리 id (없으면 전체)
    public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "kw", defaultValue = "") String kw,
                       @RequestParam(value = "category", required = false) Integer categoryId) {
        Category category = categoryId == null ? null : categoryService.getCategory(categoryId);
        Page<Question> paging = questionService.getList(page, kw, category);
        model.addAttribute("paging", paging); // 템플릿에서 ${paging} 로 사용 (내용 + 페이지 정보)
        model.addAttribute("kw", kw);         // 검색창과 페이지 링크에 검색어 유지
        model.addAttribute("category", category);
        return "question_list"; // templates/question_list.html
    }

    // answerPage : 답변 페이지(0부터), answerSort : latest(최신순, 기본) / vote(추천순)
    @GetMapping("/{id}")
    public String detail(Model model, @PathVariable("id") Integer id, AnswerForm answerForm,
                         @RequestParam(value = "answerPage", defaultValue = "0") int answerPage,
                         @RequestParam(value = "answerSort", defaultValue = "latest") String answerSort) {
        // 조회 수 +1 (3-14) 을 먼저 실행 : 일괄 update 는 영속성 컨텍스트를 거치지 않으므로
        // 엔티티를 먼저 읽어 두면 화면에는 증가 전 값이 보인다. 없는 id 면 0건 update 후 아래에서 404
        questionService.increaseViewCount(id);
        Question question = questionService.getQuestion(id);
        model.addAttribute("question", question);
        model.addAttribute("answerPaging", answerService.getList(question, answerPage, answerSort));
        model.addAttribute("answerSort", answerSort);
        return "question_detail";
    }

    // 빈 QuestionForm 을 모델에 넣어야 템플릿의 th:object="${questionForm}" 이 동작한다
    // 로그인하지 않은 사용자가 접근하면 시큐리티가 로그인 페이지로 보낸다
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/write")
    public String questionCreate(QuestionForm questionForm) {
        return "question_form";
    }

    // @Valid : QuestionForm 의 @NotEmpty, @Size 검사
    // BindingResult : 검사 실패 시 예외를 던지지 않고 결과를 담아 둔다 → 폼을 다시 보여줌 (14168 13강)
    // Principal : 현재 로그인한 사용자 정보 (getName() = username)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/write")
    public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "question_form"; // 입력값은 questionForm 에 남아 있어 그대로 다시 표시된다
        }
        SiteUser siteUser = userService.getUser(principal.getName());
        Category category = categoryService.getCategory(questionForm.categoryId());
        questionService.create(questionForm.subject(), questionForm.content(), siteUser, category);
        return "redirect:/questions?category=%d".formatted(category.getId());
    }

    // 수정 폼 : 기존 값을 폼 객체에 채워서 question_form 재사용
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/modify")
    public String questionModify(Model model, @PathVariable("id") Integer id, Principal principal) {
        Question question = questionService.getQuestion(id);
        checkAuthor(question.getAuthor(), principal, "수정권한이 없습니다.");
        model.addAttribute("question", question); // 폼이 등록(POST)인지 수정(PUT)인지 구분
        Integer categoryId = question.getCategory() != null ? question.getCategory().getId() : null;
        model.addAttribute("questionForm", new QuestionForm(categoryId, question.getSubject(), question.getContent()));
        return "question_form";
    }

    // 수정은 PUT (폼의 hidden _method=put → HiddenHttpMethodFilter, 14168 26강)
    // 권한 검사를 검증보다 먼저 : 남의 글에 잘못된 값을 보내도 오류 폼이 아니라 403
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/modify")
    public String questionModify(Model model, @Valid QuestionForm questionForm, BindingResult bindingResult,
                                 @PathVariable("id") Integer id, Principal principal) {
        Question question = questionService.getQuestion(id);
        checkAuthor(question.getAuthor(), principal, "수정권한이 없습니다.");
        if (bindingResult.hasErrors()) {
            model.addAttribute("question", question);
            return "question_form";
        }
        questionService.modify(question, questionForm.subject(), questionForm.content(),
                categoryService.getCategory(questionForm.categoryId()));
        return "redirect:/questions/%d".formatted(id);
    }

    // 책은 GET /question/delete/{id} 이지만, 삭제는 DELETE 메서드로 (14168 41강, 14163 37강 URL 표)
    // 폼의 hidden _method=delete 를 HiddenHttpMethodFilter 가 DELETE 요청으로 바꿔 준다
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/delete")
    public String questionDelete(@PathVariable("id") Integer id, Principal principal) {
        Question question = questionService.getQuestion(id);
        checkAuthor(question.getAuthor(), principal, "삭제권한이 없습니다.");
        questionService.delete(question);
        return "redirect:/";
    }

    // 책은 GET /question/vote/{id}. 데이터를 바꾸는 요청이므로 POST (CSRF 보호)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/vote")
    public String questionVote(@PathVariable("id") Integer id, Principal principal) {
        Question question = questionService.getQuestion(id);
        SiteUser siteUser = userService.getUser(principal.getName());
        questionService.vote(question, siteUser);
        return "redirect:/questions/%d".formatted(id);
    }

    // 작성자 본인만 허용. 책은 400(BAD_REQUEST), 여기서는 의미에 맞게 403(FORBIDDEN)
    private void checkAuthor(SiteUser author, Principal principal, String message) {
        if (author == null || !author.getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
    }
}
