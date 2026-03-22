package com.yf.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.Inventory;
import com.yf.entity.Drug;
import com.yf.entity.HerbCleanLog;
import com.yf.entity.HerbFillLog;
import com.yf.entity.StockIn;
import com.yf.entity.StockInDetail;
import com.yf.entity.Store;
import com.yf.entity.Supplier;
import com.yf.entity.SysUser;
import com.yf.exception.BusinessException;
import com.yf.mapper.StockInDetailMapper;
import com.yf.mapper.StockInMapper;
import com.yf.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 入库单服务
 */
@Service
@RequiredArgsConstructor
public class StockInService {
    
    private final StockInMapper stockInMapper;
    private final StockInDetailMapper stockInDetailMapper;
    private final InventoryService inventoryService;
    private final StoreService storeService;
    private final SupplierService supplierService;
    private final DrugTraceCodeService drugTraceCodeService;
    private final DrugService drugService;
    private final HerbFillLogService herbFillLogService;
    private final HerbCleanLogService herbCleanLogService;
    private final SysUserMapper sysUserMapper;
    
    /**
     * 分页查询入库单
     */
    public Page<StockIn> page(Long storeId, String status, int pageNum, int pageSize) {
        Page<StockIn> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StockIn> wrapper = new LambdaQueryWrapper<>();
        
        if (storeId != null) {
            wrapper.eq(StockIn::getStoreId, storeId);
        }
        if (status != null && StringUtils.hasText(status)) {
            wrapper.eq(StockIn::getStatus, status);
        }
        
        wrapper.orderByDesc(StockIn::getCreatedAt);
        return stockInMapper.selectPage(page, wrapper);
    }
    
    /**
     * 根据ID获取入库单
     */
    public StockIn getById(Long id) {
        return stockInMapper.selectById(id);
    }
    
    /**
     * 获取入库单明细
     */
    public List<StockInDetail> getDetails(Long stockInId) {
        LambdaQueryWrapper<StockInDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockInDetail::getStockInId, stockInId);
        return stockInDetailMapper.selectList(wrapper);
    }
    
    /**
     * 创建入库单
     */
    @Transactional(rollbackFor = Exception.class)
    public StockIn create(StockIn stockIn, List<StockInDetail> details) {
        // 生成入库单号
        String orderNo = "SI" + DateUtil.format(LocalDateTime.now(), "yyyyMMddHHmmss");
        stockIn.setOrderNo(orderNo);
        stockIn.setStatus("待审核");
        
        // 自动填充门店ID（默认使用总部/主店）
        if (stockIn.getStoreId() == null) {
            Store hq = storeService.getHeadquarter();
            if (hq != null) {
                stockIn.setStoreId(hq.getId());
            } else {
                List<Store> stores = storeService.listAll();
                if (!stores.isEmpty()) {
                    stockIn.setStoreId(stores.get(0).getId());
                }
            }
        }
        
        // 计算总金额
        BigDecimal totalAmount = details.stream()
                .map(StockInDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stockIn.setTotalAmount(totalAmount);
        
        // 快照：填充供应商名称和门店名称
        fillStockInSnapshot(stockIn);
        
        stockInMapper.insert(stockIn);
        
        // 保存明细
        for (StockInDetail detail : details) {
            detail.setStockInId(stockIn.getId());
            fillDetailSnapshot(detail);
            stockInDetailMapper.insert(detail);
            // 保存追溯码
            if (detail.getTraceCodes() != null && !detail.getTraceCodes().isEmpty()) {
                drugTraceCodeService.batchCreateForDetail(
                        stockIn.getId(), detail.getId(), detail.getDrugId(),
                        detail.getBatchNo(), detail.getProduceDate(), detail.getExpireDate(),
                        stockIn.getSupplierId(), detail.getTraceCodes());
            }
        }
        
        return stockIn;
    }
    
    /**
     * 更新入库单（仅待审核状态可编辑）
     */
    @Transactional(rollbackFor = Exception.class)
    public StockIn update(Long id, StockIn stockInParam, List<StockInDetail> details) {
        StockIn stockIn = stockInMapper.selectById(id);
        if (stockIn == null) {
            throw new BusinessException("入库单不存在");
        }
        if (!"待审核".equals(stockIn.getStatus())) {
            throw new BusinessException("只有待审核状态的入库单才能编辑");
        }

        // 更新表头
        stockIn.setType(stockInParam.getType());
        stockIn.setSupplierId(stockInParam.getSupplierId());
        stockIn.setRemark(stockInParam.getRemark());

        // 快照：更新供应商名称
        fillStockInSnapshot(stockIn);

        // 计算总金额
        BigDecimal totalAmount = details.stream()
                .map(StockInDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stockIn.setTotalAmount(totalAmount);

        stockInMapper.updateById(stockIn);

        // 删除旧明细和追溯码
        drugTraceCodeService.deleteByStockInId(id);
        LambdaQueryWrapper<StockInDetail> delWrapper = new LambdaQueryWrapper<>();
        delWrapper.eq(StockInDetail::getStockInId, id);
        stockInDetailMapper.delete(delWrapper);

        // 插入新明细
        for (StockInDetail detail : details) {
            detail.setId(null);
            detail.setStockInId(id);
            fillDetailSnapshot(detail);
            stockInDetailMapper.insert(detail);
            // 保存追溯码
            if (detail.getTraceCodes() != null && !detail.getTraceCodes().isEmpty()) {
                drugTraceCodeService.batchCreateForDetail(
                        id, detail.getId(), detail.getDrugId(),
                        detail.getBatchNo(), detail.getProduceDate(), detail.getExpireDate(),
                        stockIn.getSupplierId(), detail.getTraceCodes());
            }
        }

        return stockIn;
    }

    /**
     * 审核入库单
     */
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, Long approvedBy) {
        StockIn stockIn = stockInMapper.selectById(id);
        if (stockIn == null) {
            throw new BusinessException("入库单不存在");
        }
        if (!"待审核".equals(stockIn.getStatus())) {
            throw new BusinessException("入库单状态不正确");
        }
        
        stockIn.setStatus("已审核");
        stockIn.setApprovedBy(approvedBy);
        stockIn.setApprovedAt(LocalDateTime.now());
        stockInMapper.updateById(stockIn);
    }
    
    /**
     * 驳回入库单
     */
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String reason) {
        StockIn stockIn = stockInMapper.selectById(id);
        if (stockIn == null) {
            throw new BusinessException("入库单不存在");
        }
        if (!"待审核".equals(stockIn.getStatus())) {
            throw new BusinessException("只有待审核状态的入库单才能驳回");
        }
        stockIn.setStatus("已驳回");
        stockIn.setRemark(reason);
        stockInMapper.updateById(stockIn);
    }

    /**
     * 完成入库（更新库存）
     */
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        StockIn stockIn = stockInMapper.selectById(id);
        if (stockIn == null) {
            throw new BusinessException("入库单不存在");
        }
        if (!"已审核".equals(stockIn.getStatus())) {
            throw new BusinessException("入库单未审核");
        }
        
        // 获取明细并更新库存
        List<StockInDetail> details = getDetails(id);
        for (StockInDetail detail : details) {
            Inventory inventory = inventoryService.getByStoreAndDrug(
                    stockIn.getStoreId(), detail.getDrugId(), null);
            
            if (inventory == null) {
                // 创建新的库存记录
                inventory = new Inventory();
                inventory.setStoreId(stockIn.getStoreId());
                inventory.setDrugId(detail.getDrugId());
                inventory.setBatchNo(detail.getBatchNo());
                inventory.setQuantity(detail.getQuantity());
                inventory.setUnit(detail.getUnit());
                inventory.setCostPrice(detail.getPurchasePrice());
                inventoryService.create(inventory);
            } else {
                // 更新库存数量
                inventoryService.adjustStock(stockIn.getStoreId(), detail.getDrugId(), 
                        inventory.getBatchId(), detail.getQuantity(), "IN");
            }
        }
        
        // 自动生成中药饮片装斗/清斗记录
        generateHerbLogs(stockIn, details);
        
        stockIn.setStatus("已入库");
        stockInMapper.updateById(stockIn);
        
        // 激活追溯码 pending -> in_stock
        drugTraceCodeService.activateByStockInId(id);
    }

    /**
     * 为中药饮片自动生成装斗记录和清斗记录
     */
    private void generateHerbLogs(StockIn stockIn, List<StockInDetail> details) {
        // 查询入库操作人姓名
        String operatorName = "";
        if (stockIn.getCreatedBy() != null) {
            SysUser user = sysUserMapper.selectById(stockIn.getCreatedBy());
            if (user != null && user.getRealName() != null) {
                operatorName = user.getRealName();
            }
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        LocalDate logDate = stockIn.getCreatedAt() != null
                ? stockIn.getCreatedAt().toLocalDate() : LocalDate.now();

        List<HerbFillLog> fillLogs = new ArrayList<>();
        List<HerbCleanLog> cleanLogs = new ArrayList<>();
        int seq = 1;

        for (StockInDetail detail : details) {
            Drug drug = drugService.getById(detail.getDrugId());
            if (drug == null || !Boolean.TRUE.equals(drug.getIsHerb())) {
                continue;
            }

            String seqStr = String.format("%03d", seq);

            // 装斗记录
            HerbFillLog fill = new HerbFillLog();
            fill.setRecordNo("ZD" + timestamp + seqStr);
            fill.setStockInId(stockIn.getId());
            fill.setDrugId(detail.getDrugId());
            fill.setDrugName(detail.getDrugName());
            fill.setSpecification(detail.getSpecification());
            fill.setUnit(detail.getUnit());
            fill.setBatchNo(detail.getBatchNo());
            fill.setFillQuantity(detail.getQuantity());
            fill.setFillDate(logDate);
            fill.setOrigin(drug.getOrigin());
            fill.setManufacturer(detail.getManufacturer());
            fill.setSupplierName(stockIn.getSupplierName());
            fill.setQualityStatus("合格");
            fill.setAcceptanceResult("合格");
            fill.setFillPerson(operatorName);
            fill.setReviewer("执业药师");
            fillLogs.add(fill);

            // 清斗记录
            HerbCleanLog clean = new HerbCleanLog();
            clean.setRecordNo("QD" + timestamp + seqStr);
            clean.setStockInId(stockIn.getId());
            clean.setDrugId(detail.getDrugId());
            clean.setDrugName(detail.getDrugName());
            clean.setSpecification(detail.getSpecification());
            clean.setUnit(detail.getUnit());
            clean.setBatchNo(detail.getBatchNo());
            // 剩余数量默认为当前库存数量
            BigDecimal remainingQty = BigDecimal.ZERO;
            Inventory inv = inventoryService.getByStoreAndDrug(stockIn.getStoreId(), detail.getDrugId(), null);
            if (inv != null && inv.getQuantity() != null) {
                remainingQty = inv.getQuantity();
            }
            clean.setRemainingQuantity(remainingQty);
            clean.setCleanDate(logDate);
            clean.setOrigin(drug.getOrigin());
            clean.setManufacturer(detail.getManufacturer());
            clean.setReviewStatus("已复核");
            clean.setCleanPerson(operatorName);
            cleanLogs.add(clean);

            seq++;
        }

        if (!fillLogs.isEmpty()) {
            herbFillLogService.batchCreate(fillLogs);
        }
        if (!cleanLogs.isEmpty()) {
            herbCleanLogService.batchCreate(cleanLogs);
        }
    }

    /**
     * 获取入库单明细（含追溯码）
     */
    public List<StockInDetail> getDetailsWithTraceCodes(Long stockInId) {
        List<StockInDetail> details = getDetails(stockInId);
        if (!details.isEmpty()) {
            List<Long> detailIds = details.stream()
                    .map(StockInDetail::getId)
                    .collect(java.util.stream.Collectors.toList());
            java.util.Map<Long, List<String>> codesMap = drugTraceCodeService.getCodesByDetailIds(detailIds);
            for (StockInDetail detail : details) {
                List<String> codes = codesMap.getOrDefault(detail.getId(), java.util.Collections.emptyList());
                detail.setTraceCodes(codes);
                detail.setTraceCodeCount(codes.size());
            }
        }
        return details;
    }

    /** 填充入库单主表快照字段 */
    private void fillStockInSnapshot(StockIn stockIn) {
        if (stockIn.getSupplierId() != null) {
            Supplier supplier = supplierService.getById(stockIn.getSupplierId());
            if (supplier != null) {
                stockIn.setSupplierName(supplier.getName());
            }
        }
        if (stockIn.getStoreId() != null) {
            Store store = storeService.getById(stockIn.getStoreId());
            if (store != null) {
                stockIn.setStoreName(store.getName());
            }
        }
    }

    /** 填充入库单明细快照字段 */
    private void fillDetailSnapshot(StockInDetail detail) {
        if (detail.getDrugId() != null) {
            Drug drug = drugService.getById(detail.getDrugId());
            if (drug != null) {
                detail.setDrugName(drug.getGenericName());
                detail.setSpecification(drug.getSpecification());
                detail.setManufacturer(drug.getManufacturer());
            }
        }
    }
}
