package com.yf.controller;

import com.yf.dto.ImportExecuteResponse;
import com.yf.entity.HerbCabinet;
import com.yf.service.HerbCabinetService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * 查询有绑定药材的药柜ID集合
     */
    @GetMapping("/bound-ids")
    public ApiResponse<Set<Long>> getBoundCabinetIds() {
        return ApiResponse.success(herbCabinetService.getBoundCabinetIds());
    }
    
    /**
     * 查询所有已分配药材的名称集合
     */
    @GetMapping("/assigned-herb-ids")
    public ApiResponse<Set<String>> getAssignedHerbNames() {
        return ApiResponse.success(herbCabinetService.getAssignedHerbNames());
    }
    
    /**
     * 查询中药饮片列表（从drug表，用于快速分配面板）
     */
    @GetMapping("/herb-drug-list")
    public ApiResponse<List<Map<String, Object>>> getHerbDrugList() {
        return ApiResponse.success(herbCabinetService.getHerbDrugList());
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
        try {
            boolean success = herbCabinetService.deleteById(id);
            return success ? ApiResponse.success() : ApiResponse.error("删除失败");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 分配药材到格位
     */
    @PutMapping("/cell/{cellId}/assign")
    public ApiResponse<Void> assignHerb(@PathVariable Long cellId, @RequestBody Map<String, Object> body) {
        Long herbId = Long.valueOf(body.get("herbId").toString());
        BigDecimal minStock = new BigDecimal(body.get("minStock").toString());
        BigDecimal currentStock = new BigDecimal(body.get("currentStock").toString());
        boolean success = herbCabinetService.assignHerb(cellId, herbId, minStock, currentStock);
        return success ? ApiResponse.success() : ApiResponse.error("分配失败");
    }
    
    /**
     * 补货
     */
    @PutMapping("/cell/{cellId}/refill")
    public ApiResponse<Void> refillCell(@PathVariable Long cellId, @RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        boolean success = herbCabinetService.refillCell(cellId, amount);
        return success ? ApiResponse.success() : ApiResponse.error("补货失败");
    }
    
    /**
     * 清斗
     */
    @PutMapping("/cell/{cellId}/clean")
    public ApiResponse<Void> cleanCell(@PathVariable Long cellId) {
        boolean success = herbCabinetService.cleanCell(cellId);
        return success ? ApiResponse.success() : ApiResponse.error("清斗失败");
    }
    
    /**
     * 下载格位分配导入模板
     */
    @GetMapping("/{cabinetId}/import-template")
    public ResponseEntity<byte[]> downloadImportTemplate(@PathVariable Long cabinetId) {
        byte[] data = herbCabinetService.generateImportTemplate(cabinetId);
        String filename = URLEncoder.encode("格位分配模板.xlsx", StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        return ResponseEntity.ok().headers(headers).body(data);
    }
    
    /**
     * 批量导入格位分配
     */
    @PostMapping("/{cabinetId}/batch-import")
    public ApiResponse<ImportExecuteResponse> batchImport(
            @PathVariable Long cabinetId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        ImportExecuteResponse result = herbCabinetService.batchImportAssignment(cabinetId, file, overwrite);
        return ApiResponse.success(result);
    }
}
