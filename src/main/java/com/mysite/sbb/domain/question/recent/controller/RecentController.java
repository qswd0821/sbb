package com.mysite.sbb.domain.question.recent.controller;

import com.mysite.sbb.domain.question.answer.service.AnswerService;
import com.mysite.sbb.domain.question.comment.service.CommentService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 최근 답변 / 최근 댓글 (3-14) : 질문 목록과 별개로 최근 활동을 모아 보기
 */
@Controller
@RequiredArgsConstructor
public class RecentController {
    private final AnswerService answerService;
    private final CommentService commentService;

    @GetMapping("/recent")
    public String recent(Model model) {
        model.addAttribute("answerList", answerService.getRecentList());
        model.addAttribute("commentList", commentService.getRecentList());
        return "recent";
    }
}
