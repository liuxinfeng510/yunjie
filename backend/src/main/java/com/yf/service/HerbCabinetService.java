package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yf.dto.ImportExecuteResponse;
import com.yf.entity.Drug;
import com.yf.entity.DrugCategory;
import com.yf.entity.Herb;
import com.yf.entity.HerbCabinet;
import com.yf.entity.HerbCabinetCell;
import com.yf.mapper.DrugCategoryMapper;
import com.yf.mapper.DrugMapper;
import com.yf.mapper.HerbCabinetCellMapper;
import com.yf.mapper.HerbCabinetMapper;
import com.yf.mapper.HerbMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 中药斗柜服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HerbCabinetService {
    
    private final HerbCabinetMapper herbCabinetMapper;
    private final HerbCabinetCellMapper herbCabinetCellMapper;
    private final HerbMapper herbMapper;
    private final DrugMapper drugMapper;
    private final DrugCategoryMapper drugCategoryMapper;
    private final ObjectMapper objectMapper;
    
    private static final String[] SUB_LETTERS = {"A", "B", "C", "D"};
    
    /**
     * 查询列表（按门店）
     */
    public List<HerbCabinet> listByStoreId(Long storeId) {
        LambdaQueryWrapper<HerbCabinet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HerbCabinet::getStoreId, storeId)
               .orderByAsc(HerbCabinet::getName);
        return herbCabinetMapper.selectList(wrapper);
    }
    
    /**
     * 查询所有已分配药材的名称集合（跨所有药柜，按名称去重）
     */
    public Set<String> getAssignedHerbNames() {
        LambdaQueryWrapper<HerbCabinetCell> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(HerbCabinetCell::getHerbId)
               .select(HerbCabinetCell::getHerbId);
        List<HerbCabinetCell> cells = herbCabinetCellMapper.selectList(wrapper);
        
        List<Long> herbIds = cells.stream()
                .map(HerbCabinetCell::getHerbId)
                .distinct()
                .collect(Collectors.toList());
        
        if (herbIds.isEmpty()) {
            return Collections.emptySet();
        }
        
        List<Drug> drugs = drugMapper.selectBatchIds(herbIds);
        return drugs.stream()
                .map(Drug::getGenericName)
                .filter(name -> name != null)
                .collect(Collectors.toSet());
    }
    
    /**
     * 查询中药饮片列表（从drug表，按generic_name去重）
     */
    public List<Map<String, Object>> getHerbDrugList() {
        Set<Long> herbCategoryIds = collectHerbCategoryIds();
        if (herbCategoryIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        LambdaQueryWrapper<Drug> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Drug::getCategoryId, herbCategoryIds)
               .select(Drug::getId, Drug::getGenericName, Drug::getPinyin, Drug::getPinyinShort, Drug::getCategoryId)
               .orderByAsc(Drug::getPinyinShort, Drug::getGenericName);
        List<Drug> drugs = drugMapper.selectList(wrapper);
        
        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!drugs.isEmpty()) {
            Set<Long> catIds = drugs.stream().map(Drug::getCategoryId).collect(Collectors.toSet());
            List<DrugCategory> categories = drugCategoryMapper.selectBatchIds(catIds);
            for (DrugCategory cat : categories) {
                categoryNameMap.put(cat.getId(), cat.getName());
            }
        }
        
        // 按generic_name去重，每个名称只保留第一条
        Map<String, Map<String, Object>> uniqueMap = new LinkedHashMap<>();
        for (Drug drug : drugs) {
            String name = drug.getGenericName();
            if (name != null && !uniqueMap.containsKey(name)) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", drug.getId());
                item.put("name", name);
                item.put("pinyin", drug.getPinyin());
                item.put("pinyinShort", drug.getPinyinShort());
                item.put("category", categoryNameMap.getOrDefault(drug.getCategoryId(), ""));
                uniqueMap.put(name, item);
            }
        }
        
        return new ArrayList<>(uniqueMap.values());
    }
    
    /**
     * 收集所有中药饮片分类ID（根+子分类）
     */
    private Set<Long> collectHerbCategoryIds() {
        LambdaQueryWrapper<DrugCategory> rootWrapper = new LambdaQueryWrapper<>();
        rootWrapper.eq(DrugCategory::getName, "中药饮片")
                   .eq(DrugCategory::getParentId, 0L);
        List<DrugCategory> roots = drugCategoryMapper.selectList(rootWrapper);
        
        Set<Long> allIds = new HashSet<>();
        for (DrugCategory root : roots) {
            allIds.add(root.getId());
            LambdaQueryWrapper<DrugCategory> childWrapper = new LambdaQueryWrapper<>();
            childWrapper.eq(DrugCategory::getParentId, root.getId());
            List<DrugCategory> children = drugCategoryMapper.selectList(childWrapper);
            for (DrugCategory child : children) {
                allIds.add(child.getId());
            }
        }
        return allIds;
    }
    
    /**
     * 根据ID查询
     */
    public HerbCabinet getById(Long id) {
        return herbCabinetMapper.selectById(id);
    }
    
    /**
     * 获取斗柜所有斗格及药材信息（药材从drug表查询）
     */
    public List<Map<String, Object>> getCellsByShelfId(Long cabinetId) {
        LambdaQueryWrapper<HerbCabinetCell> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HerbCabinetCell::getCabinetId, cabinetId)
               .orderByAsc(HerbCabinetCell::getRowNum, HerbCabinetCell::getColumnNum, HerbCabinetCell::getSubIndex);
        List<HerbCabinetCell> cells = herbCabinetCellMapper.selectList(wrapper);
        
        // 批量查询药材信息（从drug表）
        List<Long> herbIds = cells.stream()
                .map(HerbCabinetCell::getHerbId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, Drug> drugMap = new HashMap<>();
        if (!herbIds.isEmpty()) {
            List<Drug> drugs = drugMapper.selectBatchIds(herbIds);
            drugMap = drugs.stream().collect(Collectors.toMap(Drug::getId, d -> d));
        }
        
        // 组装结果，herb字段返回统一格式 {id, name, category}
        final Map<Long, Drug> finalDrugMap = drugMap;
        return cells.stream().map(cell -> {
            Map<String, Object> result = new HashMap<>();
            result.put("cell", cell);
            if (cell.getHerbId() != null) {
                Drug drug = finalDrugMap.get(cell.getHerbId());
                if (drug != null) {
                    Map<String, Object> herbInfo = new HashMap<>();
                    herbInfo.put("id", drug.getId());
                    herbInfo.put("name", drug.getGenericName());
                    result.put("herb", herbInfo);
                }
            }
            return result;
        }).collect(Collectors.toList());
    }
    
    /**
     * 新增药柜并自动生成所有格位
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean save(HerbCabinet cabinet) {
        herbCabinetMapper.insert(cabinet);
        
        Map<Integer, Integer> rowConfig = parseRowCellConfig(cabinet.getRowCellConfig(), cabinet.getRowCount());
        
        for (int row = 1; row <= cabinet.getRowCount(); row++) {
            int subCount = rowConfig.getOrDefault(row, 1);
            for (int col = 1; col <= cabinet.getColumnCount(); col++) {
                for (int sub = 1; sub <= subCount; sub++) {
                    HerbCabinetCell cell = new HerbCabinetCell();
                    cell.setCabinetId(cabinet.getId());
                    cell.setRowNum(row);
                    cell.setColumnNum(col);
                    cell.setSubIndex(sub);
                    cell.setLabel(row + "-" + col + "-" + subIndexToLetter(sub));
                    cell.setStatus("active");
                    herbCabinetCellMapper.insert(cell);
                }
            }
        }
        return true;
    }
    
    /**
     * 更新药柜并差异化同步格位
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(HerbCabinet cabinet) {
        herbCabinetMapper.updateById(cabinet);
        
        // 查询现有cells
        LambdaQueryWrapper<HerbCabinetCell> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HerbCabinetCell::getCabinetId, cabinet.getId());
        List<HerbCabinetCell> existingCells = herbCabinetCellMapper.selectList(wrapper);
        
        // 构建现有cell索引: "row-col-sub" -> cell
        Map<String, HerbCabinetCell> existingMap = new HashMap<>();
        for (HerbCabinetCell cell : existingCells) {
            String key = cell.getRowNum() + "-" + cell.getColumnNum() + "-" + cell.getSubIndex();
            existingMap.put(key, cell);
        }
        
        // 根据新配置计算需要的keys
        Map<Integer, Integer> rowConfig = parseRowCellConfig(cabinet.getRowCellConfig(), cabinet.getRowCount());
        Set<String> requiredKeys = new HashSet<>();
        
        for (int row = 1; row <= cabinet.getRowCount(); row++) {
            int subCount = rowConfig.getOrDefault(row, 1);
            for (int col = 1; col <= cabinet.getColumnCount(); col++) {
                for (int sub = 1; sub <= subCount; sub++) {
                    requiredKeys.add(row + "-" + col + "-" + sub);
                }
            }
        }
        
        // 删除多余的cells
        for (Map.Entry<String, HerbCabinetCell> entry : existingMap.entrySet()) {
            if (!requiredKeys.contains(entry.getKey())) {
                herbCabinetCellMapper.deleteById(entry.getValue().getId());
            }
        }
        
        // 新增缺少的cells，更新已有cells的label
        for (String key : requiredKeys) {
            String[] parts = key.split("-");
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            int sub = Integer.parseInt(parts[2]);
            String newLabel = row + "-" + col + "-" + subIndexToLetter(sub);
            
            HerbCabinetCell existing = existingMap.get(key);
            if (existing == null) {
                // 新增
                HerbCabinetCell cell = new HerbCabinetCell();
                cell.setCabinetId(cabinet.getId());
                cell.setRowNum(row);
                cell.setColumnNum(col);
                cell.setSubIndex(sub);
                cell.setLabel(newLabel);
                cell.setStatus("active");
                herbCabinetCellMapper.insert(cell);
            } else if (!newLabel.equals(existing.getLabel())) {
                // 更新label
                existing.setLabel(newLabel);
                herbCabinetCellMapper.updateById(existing);
            }
        }
        
        return true;
    }
    
    /**
     * 删除药柜及其所有格位
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        // 先删除所有关联的cells
        LambdaQueryWrapper<HerbCabinetCell> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HerbCabinetCell::getCabinetId, id);
        herbCabinetCellMapper.delete(wrapper);
        
        // 再删除药柜
        return herbCabinetMapper.deleteById(id) > 0;
    }
    
    /**
     * 分配药材到格位
     */
    public boolean assignHerb(Long cellId, Long herbId, BigDecimal minStock, BigDecimal currentStock) {
        HerbCabinetCell cell = herbCabinetCellMapper.selectById(cellId);
        if (cell == null) {
            return false;
        }
        cell.setHerbId(herbId);
        cell.setMinStock(minStock);
        cell.setCurrentStock(currentStock);
        return herbCabinetCellMapper.updateById(cell) > 0;
    }
    
    /**
     * 清斗 - 清除格位药材
     */
    public boolean cleanCell(Long cellId) {
        HerbCabinetCell cell = herbCabinetCellMapper.selectById(cellId);
        if (cell == null) {
            return false;
        }
        // updateById 默认不更新 null 字段，需用 UpdateWrapper 显式置空
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<HerbCabinetCell> updateWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        updateWrapper.eq(HerbCabinetCell::getId, cellId)
                     .set(HerbCabinetCell::getHerbId, null)
                     .set(HerbCabinetCell::getCurrentStock, BigDecimal.ZERO);
        return herbCabinetCellMapper.update(null, updateWrapper) > 0;
    }
    
    /**
     * 补货 - 增加格位库存
     */
    public boolean refillCell(Long cellId, BigDecimal amount) {
        HerbCabinetCell cell = herbCabinetCellMapper.selectById(cellId);
        if (cell == null || cell.getHerbId() == null) {
            return false;
        }
        BigDecimal current = cell.getCurrentStock() != null ? cell.getCurrentStock() : BigDecimal.ZERO;
        cell.setCurrentStock(current.add(amount));
        return herbCabinetCellMapper.updateById(cell) > 0;
    }
    
    /**
     * 解析每行子格数配置JSON
     */
    private Map<Integer, Integer> parseRowCellConfig(String json, int rowCount) {
        Map<Integer, Integer> config = new HashMap<>();
        if (StringUtils.hasText(json)) {
            try {
                Map<String, Integer> raw = objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
                for (Map.Entry<String, Integer> entry : raw.entrySet()) {
                    int row = Integer.parseInt(entry.getKey());
                    int count = Math.max(1, Math.min(4, entry.getValue()));
                    config.put(row, count);
                }
            } catch (Exception e) {
                log.warn("解析rowCellConfig失败: {}", json, e);
            }
        }
        // 未配置的行默认1格
        for (int i = 1; i <= rowCount; i++) {
            config.putIfAbsent(i, 1);
        }
        return config;
    }
    
    /**
     * 子格序号转字母: 1->A, 2->B, 3->C, 4->D
     */
    private String subIndexToLetter(int subIndex) {
        if (subIndex >= 1 && subIndex <= SUB_LETTERS.length) {
            return SUB_LETTERS[subIndex - 1];
        }
        return String.valueOf(subIndex);
    }
    
    /**
     * 生成格位分配导入模板Excel
     */
    public byte[] generateImportTemplate(Long cabinetId) {
        HerbCabinet cabinet = herbCabinetMapper.selectById(cabinetId);
        if (cabinet == null) {
            throw new RuntimeException("药柜不存在");
        }
        
        LambdaQueryWrapper<HerbCabinetCell> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HerbCabinetCell::getCabinetId, cabinetId)
               .orderByAsc(HerbCabinetCell::getRowNum, HerbCabinetCell::getColumnNum, HerbCabinetCell::getSubIndex);
        List<HerbCabinetCell> cells = herbCabinetCellMapper.selectList(wrapper);
        
        // 查已分配药材名称
        List<Long> herbIds = cells.stream()
                .map(HerbCabinetCell::getHerbId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> drugNameMap = new HashMap<>();
        if (!herbIds.isEmpty()) {
            List<Drug> drugs = drugMapper.selectBatchIds(herbIds);
            for (Drug drug : drugs) {
                drugNameMap.put(drug.getId(), drug.getGenericName());
            }
        }
        
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("格位分配");
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"位置编号", "药材名称", "最小库存(g)", "当前库存(g)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            for (int i = 0; i < cells.size(); i++) {
                HerbCabinetCell cellData = cells.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(cellData.getLabel());
                if (cellData.getHerbId() != null) {
                    String herbName = drugNameMap.get(cellData.getHerbId());
                    if (herbName != null) {
                        row.createCell(1).setCellValue(herbName);
                    }
                }
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 4000));
            }
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("生成导入模板失败", e);
            throw new RuntimeException("生成模板失败");
        }
    }
    
    /**
     * 批量导入格位分配
     */
    public ImportExecuteResponse batchImportAssignment(Long cabinetId, MultipartFile file, boolean overwrite) {
        ImportExecuteResponse response = new ImportExecuteResponse();
        
        // 预加载格位 Map（label -> cell）
        LambdaQueryWrapper<HerbCabinetCell> cellWrapper = new LambdaQueryWrapper<>();
        cellWrapper.eq(HerbCabinetCell::getCabinetId, cabinetId);
        List<HerbCabinetCell> allCells = herbCabinetCellMapper.selectList(cellWrapper);
        Map<String, HerbCabinetCell> cellMap = new HashMap<>();
        for (HerbCabinetCell cell : allCells) {
            cellMap.put(cell.getLabel(), cell);
        }
        
        // 预加载药材 Map（name -> drugId）
        Set<Long> herbCategoryIds = collectHerbCategoryIds();
        Map<String, Long> herbNameToIdMap = new HashMap<>();
        if (!herbCategoryIds.isEmpty()) {
            LambdaQueryWrapper<Drug> drugWrapper = new LambdaQueryWrapper<>();
            drugWrapper.in(Drug::getCategoryId, herbCategoryIds)
                       .select(Drug::getId, Drug::getGenericName);
            List<Drug> drugs = drugMapper.selectList(drugWrapper);
            for (Drug drug : drugs) {
                herbNameToIdMap.putIfAbsent(drug.getGenericName(), drug.getId());
            }
        }
        
        // 解析Excel
        List<Map<Integer, String>> dataRows = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), new ReadListener<Map<Integer, String>>() {
                @Override
                public void invoke(Map<Integer, String> row, AnalysisContext context) {
                    dataRows.add(row);
                }
                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {}
            }).headRowNumber(1).sheet().doRead();
        } catch (Exception e) {
            log.error("解析Excel失败", e);
            response.setFail(1);
            response.getErrors().add("Excel文件解析失败: " + e.getMessage());
            return response;
        }
        
        response.setTotal(dataRows.size());
        
        for (int i = 0; i < dataRows.size(); i++) {
            Map<Integer, String> row = dataRows.get(i);
            int rowNum = i + 2;
            
            String label = row.getOrDefault(0, "").trim();
            String herbName = row.getOrDefault(1, "").trim();
            String minStockStr = row.getOrDefault(2, "").trim();
            String currentStockStr = row.getOrDefault(3, "").trim();
            
            if (!StringUtils.hasText(label) || !StringUtils.hasText(herbName)) {
                response.setSkip(response.getSkip() + 1);
                continue;
            }
            
            HerbCabinetCell cell = cellMap.get(label);
            if (cell == null) {
                response.setFail(response.getFail() + 1);
                response.getErrors().add("第" + rowNum + "行: 位置[" + label + "]不存在");
                continue;
            }
            
            Long drugId = herbNameToIdMap.get(herbName);
            if (drugId == null) {
                response.setFail(response.getFail() + 1);
                response.getErrors().add("第" + rowNum + "行: 药材[" + herbName + "]未找到");
                continue;
            }
            
            if (cell.getHerbId() != null && !overwrite) {
                response.setSkip(response.getSkip() + 1);
                continue;
            }
            
            BigDecimal minStock = parseBigDecimal(minStockStr, BigDecimal.ZERO);
            BigDecimal currentStock = parseBigDecimal(currentStockStr, BigDecimal.ZERO);
            
            assignHerb(cell.getId(), drugId, minStock, currentStock);
            response.setSuccess(response.getSuccess() + 1);
        }
        
        return response;
    }
    
    private BigDecimal parseBigDecimal(String str, BigDecimal defaultVal) {
        if (!StringUtils.hasText(str)) return defaultVal;
        try {
            return new BigDecimal(str);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
