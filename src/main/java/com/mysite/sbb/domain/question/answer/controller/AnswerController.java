package com.mysite.sbb.domain.question.answer.controller;

import com.mysite.sbb.domain.question.answer.entity.Answer;
import com.mysite.sbb.domain.question.answer.service.AnswerService;
import com.mysite.sbb.domain.question.question.entity.Question;
import com.mysite.sbb.domain.question.question.service.QuestionService;
import com.mysite.sbb.domain.user.user.entity.SiteUser;
import com.mysite.sbb.domain.user.user.service.UserService;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

// 답변은 질문에 속한 자원 : /questions/{questionId}/answers/{id}/... (강사 /posts/{postId}/comments/{id})
@RequestMapping("/questions/{questionId}/answers")
@Controller
@RequiredArgsConstructor
public class AnswerController {
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/write")
    public String createAnswer(Model model, @PathVariable("questionId") Integer id,
                               @Valid AnswerForm answerForm, BindingResult bindingResult, Principal principal) {
        Question question = questionService.getQuestion(id);
        SiteUser siteUser = userService.getUser(principal.getName());
        if (bindingResult.hasErrors()) {
            // 상세 템플릿을 다시 그리려면 질문과 답변 페이지 정보도 필요
            model.addAttribute("question", question);
            model.addAttribute("answerPaging", answerService.getList(question, 0, "latest"));
            model.addAttribute("answerSort", "latest");
            return "question_detail";
        }
        Answer answer = answerService.create(question, answerForm.content(), siteUser);
        // #answer_{id} : 페이지 이동 후 방금 등록한 답변 위치로 스크롤 (앵커)
        return "redirect:/questions/%d#answer_%d".formatted(id, answer.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/modify")
    public String answerModify(Model model, @PathVariable("questionId") Integer questionId,
                               @PathVariable("id") Integer id, Principal principal) {
        Answer answer = answerService.getAnswer(questionId, id);
        checkAuthor(answer.getAuthor(), principal, "수정권한이 없습니다.");
        model.addAttribute("answer", answer); // 폼의 PUT 주소에 사용
        model.addAttribute("answerForm", new AnswerForm(answer.getContent()));
        return "answer_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/modify")
    public String answerModify(Model model, @Valid AnswerForm answerForm, BindingResult bindingResult,
                               @PathVariable("questionId") Integer questionId,
                               @PathVariable("id") Integer id, Principal principal) {
        Answer answer = answerService.getAnswer(questionId, id);
        checkAuthor(answer.getAuthor(), principal, "수정권한이 없습니다.");
        if (bindingResult.hasErrors()) {
            model.addAttribute("answer", answer);
            return "answer_form";
        }
        answerService.modify(answer, answerForm.content());
        return "redirect:/questions/%d#answer_%d".formatted(questionId, id);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/delete")
    public String answerDelete(@PathVariable("questionId") Integer questionId,
                               @PathVariable("id") Integer id, Principal principal) {
        Answer answer = answerService.getAnswer(questionId, id);
        checkAuthor(answer.getAuthor(), principal, "삭제권한이 없습니다.");
        answerService.delete(answer);
        return "redirect:/questions/%d".formatted(questionId);
    }

    // 책은 GET /answer/vote/{id}. 데이터를 바꾸는 요청이므로 POST (CSRF 보호)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/vote")
    public String answerVote(@PathVariable("questionId") Integer questionId,
                             @PathVariable("id") Integer id, Principal principal) {
        Answer answer = answerService.getAnswer(questionId, id);
        SiteUser siteUser = userService.getUser(principal.getName());
        answerService.vote(answer, siteUser);
        return "redirect:/questions/%d#answer_%d".formatted(questionId, id);
    }

    private void checkAuthor(SiteUser author, Principal principal, String message) {
        if (author == null || !author.getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
        }
    }
}
