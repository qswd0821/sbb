package com.mysite.sbb.domain.question.comment.service;

import com.mysite.sbb.domain.question.answer.entity.Answer;
import com.mysite.sbb.domain.question.comment.entity.Comment;
import com.mysite.sbb.domain.question.comment.repository.CommentRepository;
import com.mysite.sbb.domain.question.question.entity.Question;
import com.mysite.sbb.domain.user.user.entity.SiteUser;
import com.mysite.sbb.global.exception.DataNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    // 부모 컬렉션에 추가 → PERSIST cascade 로 저장 (답변 등록 question.addAnswer 와 같은 방식)
    @Transactional
    public Comment create(Question question, String content, SiteUser author) {
        return question.addComment(content, author);
    }

    @Transactional
    public Comment create(Answer answer, String content, SiteUser author) {
        return answer.addComment(content, author);
    }

    public List<Comment> getRecentByAuthor(SiteUser author) {
        return commentRepository.findTop10ByAuthorOrderByCreateDateDesc(author);
    }

    public long countByAuthor(SiteUser author) {
        return commentRepository.countByAuthor(author);
    }

    public List<Comment> getRecentList() {
        return commentRepository.findTop10ByOrderByCreateDateDesc();
    }

    public Comment getComment(Integer id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("comment not found"));
    }

    @Transactional
    public void delete(Comment comment) {
        comment.removeFromParent();
    }
}
