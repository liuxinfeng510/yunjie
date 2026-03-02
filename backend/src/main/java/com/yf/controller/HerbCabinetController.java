package com.yf.controller;

import com.yf.entity.HerbCabinet;
import com.yf.service.HerbCabinetService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 中药斗柜控制器
 */
@RestController
@RequestMapping("/herb-cabinet")
@RequiredArgsConstructor
public class HerbCabinetController {
    
    private final HerbCabinetService herbCabinetService;
    
    /**
     * 查询列表（按门店）
     */
    @GetMapping("/list")
    public ApiResponse<List<HerbCabinet>> list(@RequestParam Long storeId) {
        List<HerbCabinet> cabinets = herbCabinetService.listByStoreId(storeId);
        return ApiResponse.success(cabinets);
    }
    
    /**
     * 获取斗柜所有斗格及药材信息
     */
    @GetMapping("/{id}/cells")
    public ApiResponse<List<Map<String, Object>>> getCells(@PathVariable Long id) {
        List<Map<String, Object>> cells = herbCabinetService.getCellsByShelfId(id);
        return ApiResponse.success(cells);
    }
    
    /**
     * 新增
     */
    @PostMapping
    public ApiResponse<Void> save(@RequestBody HerbCabinet cabinet) {
        boolean success = herbCabinetService.save(cabinet);
        return success ? ApiResponse.success() : ApiResponse.error("新增失败");
    }
    
    /**
     * 更新
     */
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody HerbCabinet cabinet) {
        cabinet.setId(id);
        boolean success = herbCabinetService.updateById(cabinet);
        return success ? ApiResponse.success() : ApiResponse.error("更新失败");
    }
    
    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        boolean success = herbCabinetService.deleteById(id);
        return success ? ApiResponse.success() : ApiResponse.error("删除失败");
    }
}
