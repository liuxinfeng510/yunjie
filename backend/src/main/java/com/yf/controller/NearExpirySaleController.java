package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.NearExpirySaleRecord;
import com.yf.service.NearExpirySaleService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/near-expiry-sale")
@RequiredArgsConstructor
public class NearExpirySaleController {

    private final NearExpirySaleService service;

    @PostMapping("/generate")
    public ApiResponse<Integer> generate(@RequestParam String month) {
        int count = service.generateMonthly(month);
        return ApiResponse.success(count);
    }

    @GetMapping("/page")
    public ApiResponse<Page<NearExpirySaleRecord>> page(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(service.page(month, status, pageNum, pageSize));
    }

    @GetMapping("/list")
    public ApiResponse<List<NearExpirySaleRecord>> list(@RequestParam(required = false) String month) {
        return ApiResponse.success(service.listByMonth(month));
    }

    @PutMapping("/{id}/measure")
    public ApiResponse<Void> updateMeasure(@PathVariable Long id, @RequestBody Map<String, String> body) {
        service.updateMeasure(id, body.get("saleMeasure"), body.get("measureDetail"));
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/complete")
    public ApiResponse<Void> complete(@PathVariable Long id, @RequestBody Map<String, String> body) {
        service.complete(id, body.get("resultRemark"));
        return ApiResponse.success(null);
    }
}
