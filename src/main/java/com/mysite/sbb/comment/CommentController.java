package com.mysite.sbb.comment;

import com.mysite.sbb.DataNotFoundException;
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

// 댓글은 질문 상세 화면에 속한 자원 : /questions/{questionId}/comments/..., /questions/{questionId}/answers/{answerId}/comments/...
@RequestMapping("/questions/{questionId}")
@Controller
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/comments/write")
    public String createForQuestion(@PathVariable("questionId") Integer id, @Valid CommentForm commentForm,
                                    BindingResult bindingResult, Principal principal, RedirectAttributes ra) {
        Question question = questionService.getQuestion(id);
        if (!rejectIfInvalid(bindingResult, ra)) {
            commentService.create(question, commentForm.content(), currentUser(principal));
        }
        return "redirect:/questions/%d".formatted(id);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/answers/{answerId}/comments/write")
    public String createForAnswer(@PathVariable("questionId") Integer questionId,
                                  @PathVariable("answerId") Integer id, @Valid CommentForm commentForm,
                                  BindingResult bindingResult, Principal principal, RedirectAttributes ra) {
        Answer answer = answerService.getAnswer(questionId, id);
        if (!rejectIfInvalid(bindingResult, ra)) {
            commentService.create(answer, commentForm.content(), currentUser(principal));
        }
        return "redirect:/questions/%d#answer_%d".formatted(questionId, id);
    }

    @PreAuthorize("isAuthenticated()")
    // 질문 댓글·답변 댓글 모두 상세 화면(질문) 기준 주소. 다른 질문의 댓글 id 면 404
    @DeleteMapping("/comments/{id}/delete")
    public String delete(@PathVariable("questionId") Integer questionId,
                         @PathVariable("id") Integer id, Principal principal) {
        Comment comment = commentService.getComment(id);
        if (!comment.getQuestionId().equals(questionId)) {
            throw new DataNotFoundException("comment not found");
        }
        if (comment.getAuthor() == null || !comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제권한이 없습니다.");
        }
        commentService.delete(comment);
        return "redirect:/questions/%d".formatted(questionId);
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
