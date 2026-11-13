package com.mysite.sbb.user;

import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.comment.CommentService;
import com.mysite.sbb.question.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

/**
 * 프로필 화면 (3-14) : 로그인한 사용자의 기본 정보와 작성한 질문·답변·댓글
 */
@Controller
@RequiredArgsConstructor
public class ProfileController {
    private final UserService userService;
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final CommentService commentService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/profile")
    public String profile(Model model, Principal principal) {
        SiteUser user = userService.getUser(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("questionCount", questionService.countByAuthor(user));
        model.addAttribute("answerCount", answerService.countByAuthor(user));
        model.addAttribute("commentCount", commentService.countByAuthor(user));
        model.addAttribute("questionList", questionService.getRecentByAuthor(user));
        model.addAttribute("answerList", answerService.getRecentByAuthor(user));
        model.addAttribute("commentList", commentService.getRecentByAuthor(user));
        return "profile";
    }
}
