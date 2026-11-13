package com.mysite.sbb.question;

import com.mysite.sbb.answer.AnswerForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/question") // 이 컨트롤러의 모든 URL 앞에 /question 이 붙는다
@Controller
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

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
    @GetMapping("/create")
    public String questionCreate(QuestionForm questionForm) {
        return "question_form";
    }

    // @Valid : QuestionForm 의 @NotEmpty, @Size 검사
    // BindingResult : 검사 실패 시 예외를 던지지 않고 결과를 담아 둔다 → 폼을 다시 보여줌 (14168 13강)
    @PostMapping("/create")
    public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "question_form"; // 입력값은 questionForm 에 남아 있어 그대로 다시 표시된다
        }
        questionService.create(questionForm.getSubject(), questionForm.getContent());
        return "redirect:/question/list";
    }
}
