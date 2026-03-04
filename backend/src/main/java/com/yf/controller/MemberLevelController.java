package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.MemberLevel;
import com.yf.service.MemberLevelService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member-level")
@RequiredArgsConstructor
public class MemberLevelController {

    private final MemberLevelService memberLevelService;

    @GetMapping("/page")
    public ApiResponse<Page<MemberLevel>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(memberLevelService.page(pageNum, pageSize));
    }

    @GetMapping("/list")
    public ApiResponse<List<MemberLevel>> list() {
        return ApiResponse.success(memberLevelService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<MemberLevel> getById(@PathVariable Long id) {
        return ApiResponse.success(memberLevelService.getById(id));
    }

    @PostMapping
    public ApiResponse<MemberLevel> create(@RequestBody MemberLevel level) {
        return ApiResponse.success(memberLevelService.create(level));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody MemberLevel level) {
        memberLevelService.update(id, level);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        memberLevelService.delete(id);
        return ApiResponse.success();
    }
}
