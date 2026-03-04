package com.yf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Inventory;
import com.yf.mapper.InventoryMapper;
import com.yf.service.InventoryService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存控制器
 */
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    
    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;
    
    /**
     * 分页查询库存
     */
    @GetMapping("/page")
    public ApiResponse page(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long drugId,
            @RequestParam(required = false) Boolean lowStock,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer current,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer size) {
        
        int pn = current != null ? current : (pageNum != null ? pageNum : 1);
        int ps = size != null ? size : (pageSize != null ? pageSize : 10);
        
        var result = inventoryService.page(storeId, drugId, lowStock, pn, ps);
        return ApiResponse.success(result);
    }
    
    /**
     * 库存预警（低库存 + 近效期）
     */
    @GetMapping("/warnings")
    public ApiResponse<Map<String, Object>> warnings(
            @RequestParam Long storeId,
            @RequestParam(defaultValue = "30") int expireDays) {
        
        // 查询低库存
        LambdaQueryWrapper<Inventory> lowStockWrapper = new LambdaQueryWrapper<>();
        lowStockWrapper.eq(Inventory::getStoreId, storeId)
                       .apply("quantity <= safe_stock");
        List<Inventory> lowStockList = inventoryMapper.selectList(lowStockWrapper);
        
        // 查询近效期（需要关联批次表，这里简化处理）
        // 实际项目中需要关联stock_in_detail表获取expireDate
        Map<String, Object> warnings = new HashMap<>();
        warnings.put("lowStockCount", lowStockList.size());
        warnings.put("lowStockList", lowStockList);
        warnings.put("nearExpireCount", 0);
        warnings.put("nearExpireList", List.of());
        
        return ApiResponse.success(warnings);
    }
    
    /**
     * 调整库存
     */
    @PostMapping("/adjust")
    public ApiResponse<Void> adjustStock(@RequestBody Map<String, Object> request) {
        Long storeId = Long.valueOf(request.get("storeId").toString());
        Long drugId = Long.valueOf(request.get("drugId").toString());
        Long batchId = request.get("batchId") != null ? Long.valueOf(request.get("batchId").toString()) : null;
        BigDecimal quantity = new BigDecimal(request.get("quantity").toString());
        String type = request.get("type").toString();
        
        inventoryService.adjustStock(storeId, drugId, batchId, quantity, type);
        return ApiResponse.success();
    }
}
