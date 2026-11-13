package com.mysite.sbb.category;

import com.mysite.sbb.DataNotFoundException;
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
