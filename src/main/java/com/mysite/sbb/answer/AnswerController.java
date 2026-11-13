package com.mysite.sbb.answer;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RequestMapping("/answer")
@Controller
@RequiredArgsConstructor
public class AnswerController {
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{id}")
    public String createAnswer(Model model, @PathVariable("id") Integer id,
                               @Valid AnswerForm answerForm, BindingResult bindingResult, Principal principal) {
        Question question = questionService.getQuestion(id);
        SiteUser siteUser = userService.getUser(principal.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("question", question); // 상세 템플릿을 다시 그리려면 질문도 필요
            return "question_detail";
        }
        Answer answer = answerService.create(question, answerForm.getContent(), siteUser);
        // #answer_{id} : 페이지 이동 후 방금 등록한 답변 위치로 스크롤 (앵커)
        return "redirect:/question/detail/%d#answer_%d".formatted(id, answer.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String answerModify(AnswerForm answerForm, @PathVariable("id") Integer id, Principal principal) {
        Answer answer = answerService.getAnswer(id);
        checkAuthor(answer.getAuthor(), principal, "수정권한이 없습니다.");
        answerForm.setContent(answer.getContent());
        return "answer_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String answerModify(@Valid AnswerForm answerForm, BindingResult bindingResult,
                               @PathVariable("id") Integer id, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "answer_form";
        }
        Answer answer = answerService.getAnswer(id);
        checkAuthor(answer.getAuthor(), principal, "수정권한이 없습니다.");
        answerService.modify(answer, answerForm.getContent());
        return "redirect:/question/detail/%d#answer_%d".formatted(answer.getQuestion().getId(), answer.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete/{id}")
    public String answerDelete(@PathVariable("id") Integer id, Principal principal) {
        Answer answer = answerService.getAnswer(id);
        checkAuthor(answer.getAuthor(), principal, "삭제권한이 없습니다.");
        Integer questionId = answer.getQuestion().getId(); // LAZY 프록시라도 id 는 추가 조회 없이 얻을 수 있다
        answerService.delete(answer);
        return "redirect:/question/detail/%d".formatted(questionId);
    }

    // 책은 GET /answer/vote/{id}. 데이터를 바꾸는 요청이므로 POST (CSRF 보호)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/vote/{id}")
    public String answerVote(@PathVariable("id") Integer id, Principal principal) {
        Answer answer = answerService.getAnswer(id);
        SiteUser siteUser = userService.getUser(principal.getName());
        answerService.vote(answer, siteUser);
        return "redirect:/question/detail/%d#answer_%d".formatted(answer.getQuestion().getId(), answer.getId());
    }

    private void checkAuthor(SiteUser author, Principal principal, String message) {
        if (author == null || !author.getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
    }
}
