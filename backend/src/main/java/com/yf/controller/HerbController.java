package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Herb;
import com.yf.service.HerbService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 中药饮片控制器
 */
@RestController
@RequestMapping("/herb")
@RequiredArgsConstructor
public class HerbController {
    
    private final HerbService herbService;
    
    /**
     * 查询全部中药列表（用于下拉选项）
     */
    @GetMapping("/list")
    public ApiResponse<List<Herb>> list() {
        return ApiResponse.success(herbService.listAll());
    }
    
    /**
     * 分页查询
     */
    @GetMapping("/page")
    public ApiResponse<Page<Herb>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String pinyin,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String nature,
            @RequestParam(required = false) Boolean isToxic,
            @RequestParam(required = false) String status) {
        Page<Herb> page = new Page<>(current, size);
        Page<Herb> result = herbService.page(page, name, pinyin, category, nature, isToxic, status);
        return ApiResponse.success(result);
    }
    
    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public ApiResponse<Herb> getById(@PathVariable Long id) {
        Herb herb = herbService.getById(id);
        return ApiResponse.success(herb);
    }
    
    /**
     * 新增
     */
    @PostMapping
    public ApiResponse<Void> save(@RequestBody Herb herb) {
        boolean success = herbService.save(herb);
        return success ? ApiResponse.success() : ApiResponse.error("新增失败");
    }
    
    /**
     * 更新
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Herb herb) {
        herb.setId(id);
        boolean success = herbService.updateById(herb);
        return success ? ApiResponse.success() : ApiResponse.error("更新失败");
    }
    
    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        boolean success = herbService.deleteById(id);
        return success ? ApiResponse.success() : ApiResponse.error("删除失败");
    }
}
