package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DrugKnowledge;
import com.yf.service.DrugKnowledgeService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 药品知识库控制器
 */
@RestController
@RequestMapping("/drug-knowledge")
@RequiredArgsConstructor
public class DrugKnowledgeController {

    private final DrugKnowledgeService drugKnowledgeService;

    @GetMapping("/page")
    public ApiResponse<Page<DrugKnowledge>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {
        return ApiResponse.success(drugKnowledgeService.page(pageNum, pageSize, name, category));
    }

    @GetMapping("/{id}")
    public ApiResponse<DrugKnowledge> getById(@PathVariable Long id) {
        return ApiResponse.success(drugKnowledgeService.getById(id));
    }

    @PostMapping
    public ApiResponse<Void> add(@RequestBody DrugKnowledge drug) {
        drugKnowledgeService.add(drug);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody DrugKnowledge drug) {
        drug.setId(id);
        drugKnowledgeService.update(drug);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        drugKnowledgeService.delete(id);
        return ApiResponse.success(null);
    }
}
