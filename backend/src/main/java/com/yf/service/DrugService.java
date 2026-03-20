package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.config.tenant.TenantContext;
import com.yf.entity.*;
import com.yf.mapper.DrugBatchMapper;
import com.yf.mapper.DrugCategoryMapper;
import com.yf.mapper.DrugMapper;
import com.yf.mapper.InventoryMapper;
import com.yf.util.PinyinUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 药品服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrugService {

    private final DrugMapper drugMapper;
    private final InventoryMapper inventoryMapper;
    private final DrugCategoryMapper drugCategoryMapper;
    private final DrugBatchMapper drugBatchMapper;
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
        return page(page, name, categoryId, null, otcType, status);
    }

    public Page<Drug> page(Page<Drug> page, String name, Long categoryId, List<Long> categoryIds, String otcType, String status) {
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();

        // 多字段模糊查询（含批准文号、别名）
        if (StringUtils.hasText(name)) {
            wrapper.and(w -> w
                    .like(Drug::getGenericName, name)
                    .or().like(Drug::getTradeName, name)
                    .or().like(Drug::getPinyin, name)
                    .or().like(Drug::getPinyinShort, name)
                    .or().like(Drug::getBarcode, name)
                    .or().like(Drug::getApprovalNo, name)
                    .or().like(Drug::getAlias, name)
                    .or().like(Drug::getDrugCode, name)
            );
        }

        // 分类筛选（支持多分类）
        if (categoryIds != null && !categoryIds.isEmpty()) {
            wrapper.in(Drug::getCategoryId, categoryIds);
        } else if (categoryId != null) {
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
     * 自动生成药品编码: YP + yyyyMMdd + 4位序号
     */
    public String generateDrugCode() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "YP" + dateStr;
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Drug::getDrugCode, prefix)
               .orderByDesc(Drug::getDrugCode)
               .last("LIMIT 1");
        Drug last = drugMapper.selectOne(wrapper);
        int seq = 1;
        if (last != null && last.getDrugCode() != null && last.getDrugCode().length() >= 14) {
            try {
                seq = Integer.parseInt(last.getDrugCode().substring(10)) + 1;
            } catch (NumberFormatException ignored) {}
        }
        return prefix + String.format("%04d", seq);
    }

    /**
     * 创建药品
     *
     * @param drug 药品信息
     * @return 创建成功的记录数
     */
    public int create(Drug drug) {
        if (!StringUtils.hasText(drug.getDrugCode())) {
            drug.setDrugCode(generateDrugCode());
        }
        fillPinyin(drug);
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
        if (!StringUtils.hasText(drug.getDrugCode())) {
            drug.setDrugCode(generateDrugCode());
        }
        fillPinyin(drug);
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
        fillPinyin(drug);
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
        fillPinyin(drug);
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
        return pageWithStock(page, name, categoryId, null, otcType, status, true);
    }

    public Page<Map<String, Object>> pageWithStock(Page<Drug> page, String name, Long categoryId, String otcType, String status, boolean showZeroStock) {
        return pageWithStock(page, name, categoryId, null, otcType, status, showZeroStock);
    }

    /**
     * 分页查询药品(带库存+批次信息，用于POS)
     * @param categoryIds  多分类ID列表（中药饮片映射多个分类）
     * @param showZeroStock 是否显示零库存药品
     */
    public Page<Map<String, Object>> pageWithStock(Page<Drug> page, String name, Long categoryId, List<Long> categoryIds, String otcType, String status, boolean showZeroStock) {
        // 先查询药品
        Page<Drug> drugPage = this.page(page, name, categoryId, categoryIds, otcType, status);
        
        // 获取药品ID列表
        List<Long> drugIds = drugPage.getRecords().stream()
                .map(Drug::getId)
                .collect(Collectors.toList());
        
        // 查询库存信息
        Map<Long, BigDecimal> stockMap = new HashMap<>();
        Map<Long, List<Inventory>> drugInventoryMap = new HashMap<>();
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
            
            // 按drugId分组库存记录（用于查找批次）
            drugInventoryMap = inventories.stream()
                    .collect(Collectors.groupingBy(Inventory::getDrugId));
        }
        
        // 查询批次信息（通过库存关联的batchId）
        Set<Long> batchIds = drugInventoryMap.values().stream()
                .flatMap(List::stream)
                .map(Inventory::getBatchId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, DrugBatch> batchMap = new HashMap<>();
        if (!batchIds.isEmpty()) {
            LambdaQueryWrapper<DrugBatch> batchWrapper = new LambdaQueryWrapper<>();
            batchWrapper.in(DrugBatch::getId, batchIds);
            List<DrugBatch> batches = drugBatchMapper.selectList(batchWrapper);
            batchMap = batches.stream()
                    .collect(Collectors.toMap(DrugBatch::getId, b -> b, (a, b) -> a));
        }
        
        // 构建分类ID->名称缓存
        Map<Long, String> categoryNameMap = new HashMap<>();
        List<Long> catIds = drugPage.getRecords().stream()
                .map(Drug::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!catIds.isEmpty()) {
            LambdaQueryWrapper<DrugCategory> catWrapper = new LambdaQueryWrapper<>();
            catWrapper.in(DrugCategory::getId, catIds);
            List<DrugCategory> catList = drugCategoryMapper.selectList(catWrapper);
            for (DrugCategory cat : catList) {
                categoryNameMap.put(cat.getId(), cat.getName());
            }
        }

        // 构建返回结果
        Map<Long, BigDecimal> finalStockMap = stockMap;
        Map<Long, String> finalCategoryNameMap = categoryNameMap;
        Map<Long, List<Inventory>> finalDrugInvMap = drugInventoryMap;
        Map<Long, DrugBatch> finalBatchMap = batchMap;
        List<Map<String, Object>> records = drugPage.getRecords().stream()
                .map(drug -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", drug.getId());
                    map.put("drugCode", drug.getDrugCode());
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
                    map.put("dosageForm", drug.getDosageForm());
                    map.put("status", drug.getStatus());
                    BigDecimal stock = finalStockMap.getOrDefault(drug.getId(), BigDecimal.ZERO);
                    map.put("stock", stock);
                    map.put("categoryId", drug.getCategoryId());
                    map.put("categoryName", drug.getCategoryId() != null
                            ? finalCategoryNameMap.getOrDefault(drug.getCategoryId(), "") : "");
                    
                    // 查找近效期批次（FEFO：先到期先出）
                    List<Inventory> invList = finalDrugInvMap.getOrDefault(drug.getId(), Collections.emptyList());
                    String batchNo = null;
                    LocalDate expireDate = null;
                    Long batchId = null;
                    for (Inventory inv : invList) {
                        if (inv.getQuantity() != null && inv.getQuantity().compareTo(BigDecimal.ZERO) > 0
                                && inv.getBatchId() != null) {
                            DrugBatch batch = finalBatchMap.get(inv.getBatchId());
                            if (batch != null) {
                                if (batch.getExpireDate() != null) {
                                    if (expireDate == null || batch.getExpireDate().isBefore(expireDate)) {
                                        expireDate = batch.getExpireDate();
                                        batchNo = batch.getBatchNo();
                                        batchId = inv.getBatchId();
                                    }
                                } else if (batchNo == null) {
                                    batchNo = batch.getBatchNo();
                                    batchId = inv.getBatchId();
                                }
                            }
                        }
                    }
                    // 如果没有通过batchId找到，尝试从Inventory.batchNo获取
                    if (batchNo == null) {
                        for (Inventory inv : invList) {
                            if (inv.getQuantity() != null && inv.getQuantity().compareTo(BigDecimal.ZERO) > 0
                                    && StringUtils.hasText(inv.getBatchNo())) {
                                batchNo = inv.getBatchNo();
                                break;
                            }
                        }
                    }
                    map.put("batchId", batchId);
                    map.put("batchNo", batchNo);
                    map.put("expireDate", expireDate);
                    
                    // 中药饮片字段
                    map.put("isHerb", drug.getIsHerb());
                    map.put("alias", drug.getAlias());
                    map.put("nature", drug.getNature());
                    map.put("flavor", drug.getFlavor());
                    map.put("meridian", drug.getMeridian());
                    map.put("efficacy", drug.getEfficacy());
                    map.put("origin", drug.getOrigin());
                    map.put("dosageMin", drug.getDosageMin());
                    map.put("dosageMax", drug.getDosageMax());
                    map.put("isToxic", drug.getIsToxic());
                    map.put("toxicLevel", drug.getToxicLevel());
                    // 新增字段
                    map.put("validPeriod", drug.getValidPeriod());
                    map.put("stockQuantity", drug.getStockQuantity());
                    map.put("marketingAuthHolder", drug.getMarketingAuthHolder());
                    map.put("storageCondition", drug.getStorageCondition());
                    map.put("manufacturerId", drug.getManufacturerId());
                    map.put("isKeyMaintenance", drug.getIsKeyMaintenance());
                    map.put("isImported", drug.getIsImported());
                    map.put("isSplit", drug.getIsSplit());
                    map.put("splitRatio", drug.getSplitRatio());
                    map.put("splitPriority", drug.getSplitPriority());
                    map.put("herbType", drug.getHerbType());
                    map.put("processingMethod", drug.getProcessingMethod());
                    map.put("isPrecious", drug.getIsPrecious());
                    map.put("requireRealName", drug.getRequireRealName());
                    map.put("stockUpperLimit", drug.getStockUpperLimit());
                    map.put("stockLowerLimit", drug.getStockLowerLimit());
                    map.put("allowPriceAdjust", drug.getAllowPriceAdjust());
                    map.put("maintenanceMethod", drug.getMaintenanceMethod());
                    return map;
                })
                .collect(Collectors.toList());
        
        // 过滤零库存
        if (!showZeroStock) {
            records = records.stream()
                    .filter(m -> {
                        BigDecimal s = (BigDecimal) m.get("stock");
                        return s != null && s.compareTo(BigDecimal.ZERO) > 0;
                    })
                    .collect(Collectors.toList());
        }
        
        Page<Map<String, Object>> resultPage = new Page<>(drugPage.getCurrent(), drugPage.getSize(), drugPage.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    // ========== 导入字段转换公共方法 ==========

    private static final Map<String, String> UNIT_MAPPING = new HashMap<>();
    private static final Pattern UNIT_PREFIX_PATTERN = Pattern.compile("^[\\d.]+\\s*(.+)$");

    static {
        // 精确映射：标准值 <- 变体
        String[][] mappings = {
            {"盒", "盒", "HE", "he", "He", "合"},
            {"瓶", "瓶", "PING", "ping"},
            {"支", "支", "ZHI", "zhi", "枝", "管", "筒"},
            {"条", "条", "TIAO", "tiao"},
            {"克", "克", "G", "g", "KE", "ke", "Ke"},
            {"片", "片", "PIAN", "pian"},
            {"粒", "粒", "LI", "li", "颗"},
            {"袋", "袋", "DAI", "dai", "包", "小包"},
            {"罐", "罐", "GUAN", "guan", "桶"},
            {"板", "板", "BAN", "ban"},
        };
        for (String[] row : mappings) {
            String standard = row[0];
            for (int i = 1; i < row.length; i++) {
                UNIT_MAPPING.put(row[i], standard);
            }
        }
    }

    /**
     * 单位标准化：将各种混乱的单位值映射到系统标准单位
     */
    public String normalizeUnit(String rawUnit) {
        if (!StringUtils.hasText(rawUnit)) return "盒";
        String trimmed = rawUnit.trim();

        // 第一层：精确映射
        String mapped = UNIT_MAPPING.get(trimmed);
        if (mapped != null) return mapped;

        // 第二层：去掉数量前缀再匹配（如 1G -> G -> 克）
        Matcher m = UNIT_PREFIX_PATTERN.matcher(trimmed);
        if (m.matches()) {
            String unitPart = m.group(1).trim();
            mapped = UNIT_MAPPING.get(unitPart);
            if (mapped != null) return mapped;
        }

        // 第三层：保留原值（台/个/只/套等合理单位不强制转换）
        return trimmed;
    }

    /**
     * 查找或自动创建药品分类，返回分类ID
     */
    public Long getOrCreateCategoryId(String categoryName, Map<String, Long> cache) {
        if (!StringUtils.hasText(categoryName)) return null;
        String name = categoryName.trim();

        // 先查缓存
        Long catId = cache.get(name);
        if (catId != null) return catId;

        // 缓存未命中，查数据库
        DrugCategory existing = drugCategoryMapper.selectOne(
                new LambdaQueryWrapper<DrugCategory>()
                        .eq(DrugCategory::getName, name)
                        .eq(DrugCategory::getDeleted, 0)
                        .last("LIMIT 1"));
        if (existing != null) {
            cache.put(name, existing.getId());
            return existing.getId();
        }

        // 数据库也没有，自动创建为一级分类
        DrugCategory newCat = new DrugCategory();
        newCat.setName(name);
        newCat.setParentId(0L);
        newCat.setSortOrder(100);
        newCat.setIsSystem(false);
        Long tenantId = TenantContext.getTenantId();
        newCat.setTenantId(tenantId != null ? tenantId : 0L);
        drugCategoryMapper.insert(newCat);
        cache.put(name, newCat.getId());
        return newCat.getId();
    }

    /**
     * 储存条件转换：2-8度对应冷藏，其余默认密封常温保存
     */
    public String convertStorageCondition(String val) {
        if (StringUtils.hasText(val)) {
            String v = val.trim();
            if (v.contains("2-8") || v.contains("冷藏")) {
                return "冷藏保存(2-8℃)";
            }
            if (v.contains("-20") || v.contains("冷冻")) {
                return "冷冻保存(-20℃以下)";
            }
        }
        return "密封常温保存";
    }

    // ========== 拼音相关 ==========

    /**
     * 为药品自动填充拼音字段（基于通用名）
     */
    private void fillPinyin(Drug drug) {
        String name = drug.getGenericName();
        if (!StringUtils.hasText(name)) return;
        drug.setPinyin(PinyinUtil.toPinyin(name));
        drug.setPinyinShort(PinyinUtil.toPinyinShort(name));
    }

    /**
     * 批量生成所有药品的拼音数据（一次性修复历史数据）
     * @return 更新的记录数
     */
    @Transactional
    public int generateAllPinyin() {
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(Drug::getGenericName);
        List<Drug> drugs = drugMapper.selectList(wrapper);
        int count = 0;
        for (Drug drug : drugs) {
            String name = drug.getGenericName();
            if (!StringUtils.hasText(name)) continue;
            String pinyin = PinyinUtil.toPinyin(name);
            String pinyinShort = PinyinUtil.toPinyinShort(name);
            // 只在数据有变化时更新
            if (!pinyin.equals(drug.getPinyin()) || !pinyinShort.equals(drug.getPinyinShort())) {
                Drug update = new Drug();
                update.setId(drug.getId());
                update.setPinyin(pinyin);
                update.setPinyinShort(pinyinShort);
                drugMapper.updateById(update);
                count++;
            }
        }
        log.info("拼音数据生成完成，共更新 {} 条药品记录", count);
        return count;
    }
}
