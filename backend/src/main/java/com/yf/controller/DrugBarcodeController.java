package com.yf.controller;

import com.yf.entity.DrugBarcode;
import com.yf.service.DrugBarcodeService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 药品条形码控制器
 */
@RestController
@RequestMapping("/drug-barcode")
@RequiredArgsConstructor
public class DrugBarcodeController {

    private final DrugBarcodeService barcodeService;

    /**
     * 根据药品ID查询条形码列表
     *
     * @param drugId 药品ID
     * @return 条形码列表
     */
    @GetMapping("/drug/{drugId}")
    public ApiResponse listByDrugId(@PathVariable Long drugId) {
        List<DrugBarcode> list = barcodeService.listByDrugId(drugId);
        return ApiResponse.success(list);
    }

    /**
     * 根据条形码查询（用于扫码识别）
     *
     * @param barcode 条形码
     * @return 条形码记录
     */
    @GetMapping("/find")
    public ApiResponse findByBarcode(@RequestParam String barcode) {
        DrugBarcode result = barcodeService.findByBarcode(barcode);
        if (result == null) {
            return ApiResponse.error("条形码不存在");
        }
        return ApiResponse.success(result);
    }

    /**
     * 检查条形码是否重复
     *
     * @param barcode      条形码
     * @param excludeDrugId 排除的药品ID
     * @return 如果重复返回药品信息，否则返回null
     */
    @GetMapping("/check-duplicate")
    public ApiResponse checkDuplicate(
            @RequestParam String barcode,
            @RequestParam(required = false) Long excludeDrugId) {
        Map<String, Object> duplicate = barcodeService.checkDuplicate(barcode, excludeDrugId);
        return ApiResponse.success(duplicate);
    }

    /**
     * 新增条形码
     *
     * @param barcode 条形码信息
     * @return 创建结果
     */
    @PostMapping
    public ApiResponse create(@RequestBody DrugBarcode barcode) {
        try {
            barcodeService.create(barcode);
            return ApiResponse.success();
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除条形码
     *
     * @param id 条形码ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        try {
            barcodeService.delete(id);
            return ApiResponse.success();
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
