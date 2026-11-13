package com.mysite.sbb.question;

import com.mysite.sbb.answer.AnswerForm;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RequestMapping("/question") // 이 컨트롤러의 모든 URL 앞에 /question 이 붙는다
@Controller
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;
    private final UserService userService;

    // /question/list?page=0 형태, page 파라미터가 없으면 0 (첫 페이지)
    @GetMapping("/list")
    public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page) {
        Page<Question> paging = questionService.getList(page);
        model.addAttribute("paging", paging); // 템플릿에서 ${paging} 로 사용 (내용 + 페이지 정보)
        return "question_list"; // templates/question_list.html
    }

    @GetMapping("/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id, AnswerForm answerForm) {
        Question question = questionService.getQuestion(id);
        model.addAttribute("question", question);
        return "question_detail";
    }

    // 빈 QuestionForm 을 모델에 넣어야 템플릿의 th:object="${questionForm}" 이 동작한다
    // 로그인하지 않은 사용자가 접근하면 시큐리티가 로그인 페이지로 보낸다
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String questionCreate(QuestionForm questionForm) {
        return "question_form";
    }

    // @Valid : QuestionForm 의 @NotEmpty, @Size 검사
    // BindingResult : 검사 실패 시 예외를 던지지 않고 결과를 담아 둔다 → 폼을 다시 보여줌 (14168 13강)
    // Principal : 현재 로그인한 사용자 정보 (getName() = username)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "question_form"; // 입력값은 questionForm 에 남아 있어 그대로 다시 표시된다
        }
        SiteUser siteUser = userService.getUser(principal.getName());
        questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser);
        return "redirect:/question/list";
    }

    // 수정 폼 : 기존 값을 폼 객체에 채워서 question_form 재사용
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm, @PathVariable("id") Integer id, Principal principal) {
        Question question = questionService.getQuestion(id);
        checkAuthor(question.getAuthor(), principal, "수정권한이 없습니다.");
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult,
                                 @PathVariable("id") Integer id, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "question_form";
        }
        Question question = questionService.getQuestion(id);
        checkAuthor(question.getAuthor(), principal, "수정권한이 없습니다.");
        questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
        return "redirect:/question/detail/%d".formatted(id);
    }

    // 책은 GET /question/delete/{id} 이지만, 삭제는 DELETE 메서드로 (14168 41강, 14163 37강 URL 표)
    // 폼의 hidden _method=delete 를 HiddenHttpMethodFilter 가 DELETE 요청으로 바꿔 준다
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete/{id}")
    public String questionDelete(@PathVariable("id") Integer id, Principal principal) {
        Question question = questionService.getQuestion(id);
        checkAuthor(question.getAuthor(), principal, "삭제권한이 없습니다.");
        questionService.delete(question);
        return "redirect:/";
    }

    // 작성자 본인만 허용. 책은 400(BAD_REQUEST), 여기서는 의미에 맞게 403(FORBIDDEN)
    private void checkAuthor(SiteUser author, Principal principal, String message) {
        if (author == null || !author.getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
    }
}
