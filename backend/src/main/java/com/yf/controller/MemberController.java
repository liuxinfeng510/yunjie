package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.dto.ImportExecuteRequest;
import com.yf.dto.ImportExecuteResponse;
import com.yf.dto.ImportParseResponse;
import com.yf.entity.Member;
import com.yf.entity.MemberPointsLog;
import com.yf.service.BatchImportService;
import com.yf.service.MemberService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 会员控制器
 */
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberService memberService;
    private final BatchImportService batchImportService;
    
    /**
     * 分页查询会员
     */
    @GetMapping("/page")
    public ApiResponse<Page<Member>> page(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        Page<Member> result = memberService.page(name, phone, pageNum, pageSize);
        return ApiResponse.success(result);
    }
    
    /**
     * 获取会员列表(用于下拉选择)
     */
    @GetMapping("/list")
    public ApiResponse<java.util.List<Member>> list() {
        java.util.List<Member> result = memberService.list();
        return ApiResponse.success(result);
    }
    
    /**
     * 搜索会员(用于自动补全)
     */
    @GetMapping("/search")
    public ApiResponse<java.util.List<Member>> search(@RequestParam String keyword) {
        java.util.List<Member> result = memberService.search(keyword);
        return ApiResponse.success(result);
    }
    
    /**
     * 根据ID获取会员
     */
    @GetMapping("/{id}")
    public ApiResponse<Member> getById(@PathVariable Long id) {
        Member member = memberService.getById(id);
        return ApiResponse.success(member);
    }
    
    /**
     * 创建会员
     */
    @PostMapping
    public ApiResponse<Member> create(@RequestBody Member member) {
        Member result = memberService.create(member);
        return ApiResponse.success(result);
    }
    
    /**
     * 更新会员
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Member member) {
        member.setId(id);
        memberService.update(member);
        return ApiResponse.success();
    }
    
    /**
     * 获取会员积分日志
     */
    @GetMapping("/{id}/points-log")
    public ApiResponse<Page<MemberPointsLog>> getPointsLog(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        Page<MemberPointsLog> result = memberService.getPointsLogs(id, pageNum, pageSize);
        return ApiResponse.success(result);
    }
    
    /**
     * 增加积分
     */
    @PostMapping("/{id}/add-points")
    public ApiResponse<Void> addPoints(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Integer points = Integer.valueOf(request.get("points").toString());
        String type = request.getOrDefault("type", "手动调整").toString();
        Long relatedOrderId = request.get("relatedOrderId") != null ? 
                Long.valueOf(request.get("relatedOrderId").toString()) : null;
        
        memberService.addPoints(id, points, type, relatedOrderId);
        return ApiResponse.success();
    }
    
    /**
     * 扣减积分
     */
    @PostMapping("/{id}/deduct-points")
    public ApiResponse<Void> deductPoints(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Integer points = Integer.valueOf(request.get("points").toString());
        String type = request.getOrDefault("type", "手动调整").toString();
        Long relatedOrderId = request.get("relatedOrderId") != null ? 
                Long.valueOf(request.get("relatedOrderId").toString()) : null;
        
        memberService.deductPoints(id, points, type, relatedOrderId);
        return ApiResponse.success();
    }
    
    /**
     * 导入解析 - 上传Excel并自动映射字段
     */
    @PostMapping("/import/parse")
    public ApiResponse<ImportParseResponse> importParse(@RequestParam("file") MultipartFile file) {
        ImportParseResponse result = batchImportService.parseExcel(file, "member");
        return ApiResponse.success(result);
    }
    
    /**
     * 导入执行 - 按映射关系批量导入会员
     */
    @PostMapping("/import/execute")
    public ApiResponse<ImportExecuteResponse> importExecute(@RequestBody ImportExecuteRequest request) {
        ImportExecuteResponse result = batchImportService.executeMemberImport(request);
        return ApiResponse.success(result);
    }
    
    /**
     * 批量生成会员拼音（修复历史数据）
     */
    @PostMapping("/generate-pinyin")
    public ApiResponse<Map<String, Object>> generatePinyin() {
        int count = memberService.generateAllPinyin();
        return ApiResponse.success(Map.of("updated", count, "message", "已更新" + count + "条会员拼音数据"));
    }
}
