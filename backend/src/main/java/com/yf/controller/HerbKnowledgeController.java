package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.HerbKnowledge;
import com.yf.service.HerbKnowledgeService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 中药知识库控制器
 */
@RestController
@RequestMapping("/herb-knowledge")
@RequiredArgsConstructor
public class HerbKnowledgeController {

    private final HerbKnowledgeService herbKnowledgeService;

    @GetMapping("/page")
    public ApiResponse<Page<HerbKnowledge>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String nature) {
        return ApiResponse.success(herbKnowledgeService.page(pageNum, pageSize, name, nature));
    }

    @GetMapping("/{id}")
    public ApiResponse<HerbKnowledge> getById(@PathVariable Long id) {
        return ApiResponse.success(herbKnowledgeService.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> add(@RequestBody HerbKnowledge herb) {
        herbKnowledgeService.add(herb);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody HerbKnowledge herb) {
        herb.setId(id);
        herbKnowledgeService.update(herb);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        herbKnowledgeService.delete(id);
        return ApiResponse.success(null);
    }
}
