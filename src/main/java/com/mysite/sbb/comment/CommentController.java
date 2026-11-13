package com.mysite.sbb.comment;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@RequestMapping("/comment")
@Controller
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/question/{id}")
    public String createForQuestion(@PathVariable("id") Integer id, @Valid CommentForm commentForm,
                                    BindingResult bindingResult, Principal principal, RedirectAttributes ra) {
        Question question = questionService.getQuestion(id);
        if (!rejectIfInvalid(bindingResult, ra)) {
            commentService.create(question, commentForm.content(), currentUser(principal));
        }
        return "redirect:/question/detail/%d".formatted(id);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/answer/{id}")
    public String createForAnswer(@PathVariable("id") Integer id, @Valid CommentForm commentForm,
                                  BindingResult bindingResult, Principal principal, RedirectAttributes ra) {
        Answer answer = answerService.getAnswer(id);
        if (!rejectIfInvalid(bindingResult, ra)) {
            commentService.create(answer, commentForm.content(), currentUser(principal));
        }
        return "redirect:/question/detail/%d#answer_%d".formatted(answer.getQuestion().getId(), id);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id, Principal principal) {
        Comment comment = commentService.getComment(id);
        if (comment.getAuthor() == null || !comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제권한이 없습니다.");
        }
        Integer questionId = comment.getQuestionId();
        commentService.delete(comment);
        return "redirect:/question/detail/%d".formatted(questionId);
    }

    // 댓글은 상세 페이지 안의 작은 폼이라 오류 시 폼을 다시 그리지 않고, 메시지만 1회성으로 전달 (flash attribute)
    private boolean rejectIfInvalid(BindingResult bindingResult, RedirectAttributes ra) {
        if (!bindingResult.hasErrors()) return false;
        ra.addFlashAttribute("commentError", bindingResult.getAllErrors().getFirst().getDefaultMessage());
        return true;
    }

    private SiteUser currentUser(Principal principal) {
        return userService.getUser(principal.getName());
    }
}
