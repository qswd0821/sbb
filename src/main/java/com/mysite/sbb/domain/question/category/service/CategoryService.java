package com.mysite.sbb.domain.question.category.service;

import com.mysite.sbb.domain.question.category.entity.Category;
import com.mysite.sbb.domain.question.category.repository.CategoryRepository;
import com.mysite.sbb.global.exception.DataNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<Category> getList() {
        return categoryRepository.findAll(Sort.by("id")); // 생성 순서(질문답변, 강좌, 자유게시판)
    }

    public Category getCategory(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("category not found"));
    }
}
