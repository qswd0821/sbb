package com.mysite.sbb.comment;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    public Comment create(Question question, String content, SiteUser author) {
        Comment comment = newComment(content, author);
        comment.setQuestion(question);
        return commentRepository.save(comment);
    }

    public Comment create(Answer answer, String content, SiteUser author) {
        Comment comment = newComment(content, author);
        comment.setAnswer(answer);
        return commentRepository.save(comment);
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

    public void delete(Comment comment) {
        commentRepository.delete(comment);
    }

    private Comment newComment(String content, SiteUser author) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(author);
        comment.setCreateDate(LocalDateTime.now());
        return comment;
    }
}
