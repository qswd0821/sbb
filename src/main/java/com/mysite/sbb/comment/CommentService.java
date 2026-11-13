package com.mysite.sbb.comment;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;
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
