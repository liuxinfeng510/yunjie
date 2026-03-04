package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.config.tenant.TenantContext;
import com.yf.entity.Drug;
import com.yf.entity.DrugBarcode;
import com.yf.entity.Inventory;
import com.yf.mapper.DrugMapper;
import com.yf.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 药品服务类
 */
@Service
@RequiredArgsConstructor
public class DrugService {

    private final DrugMapper drugMapper;
    private final InventoryMapper inventoryMapper;
    private final DrugBarcodeService drugBarcodeService;

    /**
     * 分页查询药品
     *
     * @param page       分页对象
     * @param name       商品名称（支持通用名/商品名/拼音/拼音简码/条形码/批准文号）
     * @param categoryId 分类ID
     * @param otcType    OTC类型
     * @param status     状态
     * @return 分页结果
     */
    public Page<Drug> page(Page<Drug> page, String name, Long categoryId, String otcType, String status) {
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();

        // 多字段模糊查询（含批准文号）
        if (StringUtils.hasText(name)) {
            wrapper.and(w -> w
                    .like(Drug::getGenericName, name)
                    .or().like(Drug::getTradeName, name)
                    .or().like(Drug::getPinyin, name)
                    .or().like(Drug::getPinyinShort, name)
                    .or().like(Drug::getBarcode, name)
                    .or().like(Drug::getApprovalNo, name)
            );
        }

        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(Drug::getCategoryId, categoryId);
        }

        // OTC类型筛选
        if (StringUtils.hasText(otcType)) {
            wrapper.eq(Drug::getOtcType, otcType);
        }

        // 状态筛选
        if (StringUtils.hasText(status)) {
            wrapper.eq(Drug::getStatus, status);
        }

        // 按创建时间降序
        wrapper.orderByDesc(Drug::getCreatedAt);

        return drugMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询药品
     *
     * @param id 药品ID
     * @return 药品信息
     */
    public Drug getById(Long id) {
        return drugMapper.selectById(id);
    }

    /**
     * 创建药品
     *
     * @param drug 药品信息
     * @return 创建成功的记录数
     */
    public int create(Drug drug) {
        return drugMapper.insert(drug);
    }

    /**
     * 创建药品（带条形码列表）
     *
     * @param drug     药品信息
     * @param barcodes 条形码列表
     * @return 药品信息
     */
    @Transactional
    public Drug createWithBarcodes(Drug drug, List<DrugBarcode> barcodes) {
        drugMapper.insert(drug);
        Long tenantId = TenantContext.getTenantId();
        if (barcodes != null && !barcodes.isEmpty()) {
            drugBarcodeService.saveAll(drug.getId(), tenantId, barcodes);
        }
        return drug;
    }

    /**
     * 更新药品
     *
     * @param drug 药品信息
     * @return 更新成功的记录数
     */
    public int update(Drug drug) {
        return drugMapper.updateById(drug);
    }

    /**
     * 更新药品（带条形码列表）
     *
     * @param drug     药品信息
     * @param barcodes 条形码列表
     */
    @Transactional
    public void updateWithBarcodes(Drug drug, List<DrugBarcode> barcodes) {
        drugMapper.updateById(drug);
        Long tenantId = TenantContext.getTenantId();
        drugBarcodeService.saveAll(drug.getId(), tenantId, barcodes);
    }

    /**
     * 删除药品
     *
     * @param id 药品ID
     * @return 删除成功的记录数
     */
    public int delete(Long id) {
        return drugMapper.deleteById(id);
    }

    /**
     * 获取所有药品列表(用于下拉选择)
     */
    public List<Drug> list() {
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Drug::getStatus, "正常")
               .orderByAsc(Drug::getGenericName);
        return drugMapper.selectList(wrapper);
    }

    /**
     * 分页查询药品(带库存信息，用于POS)
     */
    public Page<Map<String, Object>> pageWithStock(Page<Drug> page, String name, Long categoryId, String otcType, String status) {
        // 先查询药品
        Page<Drug> drugPage = this.page(page, name, categoryId, otcType, status);
        
        // 获取药品ID列表
        List<Long> drugIds = drugPage.getRecords().stream()
                .map(Drug::getId)
                .collect(Collectors.toList());
        
        // 查询库存信息
        Map<Long, BigDecimal> stockMap = new java.util.HashMap<>();
        if (!drugIds.isEmpty()) {
            LambdaQueryWrapper<Inventory> invWrapper = new LambdaQueryWrapper<>();
            invWrapper.in(Inventory::getDrugId, drugIds);
            List<Inventory> inventories = inventoryMapper.selectList(invWrapper);
            
            // 按drugId汇总库存
            stockMap = inventories.stream()
                    .collect(Collectors.groupingBy(
                            Inventory::getDrugId,
                            Collectors.reducing(BigDecimal.ZERO, Inventory::getQuantity, BigDecimal::add)
                    ));
        }
        
        // 构建返回结果
        Map<Long, BigDecimal> finalStockMap = stockMap;
        List<Map<String, Object>> records = drugPage.getRecords().stream()
                .map(drug -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", drug.getId());
                    map.put("genericName", drug.getGenericName());
                    map.put("tradeName", drug.getTradeName());
                    map.put("specification", drug.getSpecification());
                    map.put("manufacturer", drug.getManufacturer());
                    map.put("unit", drug.getUnit());
                    map.put("retailPrice", drug.getRetailPrice());
                    map.put("memberPrice", drug.getMemberPrice());
                    map.put("medicalInsurance", drug.getMedicalInsurance());
                    map.put("otcType", drug.getOtcType());
                    map.put("barcode", drug.getBarcode());
                    map.put("purchasePrice", drug.getPurchasePrice());
                    map.put("approvalNo", drug.getApprovalNo());
                    map.put("stock", finalStockMap.getOrDefault(drug.getId(), BigDecimal.ZERO));
                    return map;
                })
                .collect(Collectors.toList());
        
        Page<Map<String, Object>> resultPage = new Page<>(drugPage.getCurrent(), drugPage.getSize(), drugPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }
}
