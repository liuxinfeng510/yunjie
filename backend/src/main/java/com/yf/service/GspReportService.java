package com.yf.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.yf.entity.*;
import com.yf.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * GSP报表导出服务 - 支持Excel和PDF格式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GspReportService {

    private final DrugAcceptanceMapper acceptanceMapper;
    private final DrugMaintenanceMapper maintenanceMapper;
    private final TemperatureHumidityLogMapper tempLogMapper;
    private final DefectiveDrugMapper defectiveMapper;
    private final DrugDestructionMapper destructionMapper;
    private final StaffTrainingMapper trainingMapper;
    private final GspComplianceCheckMapper complianceMapper;

    // ==================== Excel 导出方法 ====================

    /**
     * 导出验收记录报表(Excel)
     */
    public byte[] exportAcceptanceReport(Long storeId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<DrugAcceptance> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) wrapper.eq(DrugAcceptance::getStoreId, storeId);
        if (startDate != null) wrapper.ge(DrugAcceptance::getAcceptTime, startDate.atStartOfDay());
        if (endDate != null) wrapper.le(DrugAcceptance::getAcceptTime, endDate.plusDays(1).atStartOfDay());
        wrapper.orderByDesc(DrugAcceptance::getAcceptTime);
        
        List<DrugAcceptance> records = acceptanceMapper.selectList(wrapper);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("验收记录");
            
            Row header = sheet.createRow(0);
            String[] headers = {"验收时间", "商品名称", "批号", "数量", "外观检查", "综合结果"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (DrugAcceptance record : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.getAcceptTime() != null ? 
                        record.getAcceptTime().format(formatter) : "");
                row.createCell(1).setCellValue(record.getDrugName() != null ? record.getDrugName() : "");
                row.createCell(2).setCellValue(record.getBatchNo() != null ? record.getBatchNo() : "");
                row.createCell(3).setCellValue(record.getQuantity() != null ? record.getQuantity().toString() : "");
                row.createCell(4).setCellValue(record.getAppearanceCheck() != null ? record.getAppearanceCheck() : "");
                row.createCell(5).setCellValue(record.getOverallResult() != null ? record.getOverallResult() : "");
            }
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("导出验收报表失败", e);
            throw new RuntimeException("导出失败");
        }
    }

    /**
     * 导出养护记录报表(Excel)
     */
    public byte[] exportMaintenanceReport(Long storeId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<DrugMaintenance> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) wrapper.eq(DrugMaintenance::getStoreId, storeId);
        if (startDate != null) wrapper.ge(DrugMaintenance::getMaintenanceTime, startDate.atStartOfDay());
        if (endDate != null) wrapper.le(DrugMaintenance::getMaintenanceTime, endDate.plusDays(1).atStartOfDay());
        wrapper.orderByDesc(DrugMaintenance::getMaintenanceTime);
        
        List<DrugMaintenance> records = maintenanceMapper.selectList(wrapper);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("养护记录");
            
            Row header = sheet.createRow(0);
            String[] headers = {"养护日期", "商品名称", "养护类型", "外观检查", "包装检查", "综合结果", "异常描述"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (DrugMaintenance record : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.getMaintenanceTime() != null ? 
                        record.getMaintenanceTime().format(formatter) : "");
                row.createCell(1).setCellValue(record.getDrugName() != null ? record.getDrugName() : "");
                row.createCell(2).setCellValue(record.getMaintenanceType() != null ? record.getMaintenanceType() : "");
                row.createCell(3).setCellValue(record.getAppearanceCheck() != null ? record.getAppearanceCheck() : "");
                row.createCell(4).setCellValue(record.getPackageCheck() != null ? record.getPackageCheck() : "");
                row.createCell(5).setCellValue(record.getOverallResult() != null ? record.getOverallResult() : "");
                row.createCell(6).setCellValue(record.getAbnormalDesc() != null ? record.getAbnormalDesc() : "");
            }
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("导出养护报表失败", e);
            throw new RuntimeException("导出失败");
        }
    }

    /**
     * 导出温湿度记录报表(Excel)
     */
    public byte[] exportTemperatureReport(Long storeId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<TemperatureHumidityLog> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) wrapper.eq(TemperatureHumidityLog::getStoreId, storeId);
        if (startDate != null) wrapper.ge(TemperatureHumidityLog::getRecordTime, startDate.atStartOfDay());
        if (endDate != null) wrapper.le(TemperatureHumidityLog::getRecordTime, endDate.plusDays(1).atStartOfDay());
        wrapper.orderByDesc(TemperatureHumidityLog::getRecordTime);
        
        List<TemperatureHumidityLog> records = tempLogMapper.selectList(wrapper);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("温湿度记录");
            
            Row header = sheet.createRow(0);
            String[] headers = {"记录时间", "存储位置", "温度(℃)", "湿度(%)", "是否异常", "处理状态"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (TemperatureHumidityLog record : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.getRecordTime() != null ? 
                        record.getRecordTime().format(formatter) : "");
                row.createCell(1).setCellValue(record.getLocation() != null ? record.getLocation() : "");
                row.createCell(2).setCellValue(record.getTemperature() != null ? record.getTemperature().toString() : "");
                row.createCell(3).setCellValue(record.getHumidity() != null ? record.getHumidity().toString() : "");
                row.createCell(4).setCellValue(Boolean.TRUE.equals(record.getIsAbnormal()) ? "是" : "否");
                row.createCell(5).setCellValue(record.getHandleStatus() != null ? record.getHandleStatus() : "");
            }
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("导出温湿度报表失败", e);
            throw new RuntimeException("导出失败");
        }
    }

    /**
     * 导出培训记录报表(Excel)
     */
    public byte[] exportTrainingReport(Long storeId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<StaffTraining> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) wrapper.eq(StaffTraining::getStoreId, storeId);
        if (startDate != null) wrapper.ge(StaffTraining::getTrainingDate, startDate);
        if (endDate != null) wrapper.le(StaffTraining::getTrainingDate, endDate);
        wrapper.eq(StaffTraining::getStatus, "completed");
        wrapper.orderByDesc(StaffTraining::getTrainingDate);
        
        List<StaffTraining> records = trainingMapper.selectList(wrapper);
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("培训记录");
            
            Row header = sheet.createRow(0);
            String[] headers = {"培训日期", "培训主题", "培训类型", "讲师", "时长(h)", "参训人数", "地点"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            
            int rowNum = 1;
            for (StaffTraining record : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.getTrainingDate() != null ? 
                        record.getTrainingDate().toString() : "");
                row.createCell(1).setCellValue(record.getTitle() != null ? record.getTitle() : "");
                row.createCell(2).setCellValue(getTrainingTypeName(record.getTrainingType()));
                row.createCell(3).setCellValue(record.getTrainer() != null ? record.getTrainer() : "");
                row.createCell(4).setCellValue(record.getDuration() != null ? record.getDuration() : 0);
                row.createCell(5).setCellValue(record.getAttendeeCount() != null ? record.getAttendeeCount() : 0);
                row.createCell(6).setCellValue(record.getLocation() != null ? record.getLocation() : "");
            }
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("导出培训报表失败", e);
            throw new RuntimeException("导出失败");
        }
    }

    private String getTrainingTypeName(String type) {
        if (type == null) return "";
        return switch (type) {
            case "gsp" -> "GSP法规";
            case "drug_knowledge" -> "药品知识";
            case "service" -> "服务技能";
            case "safety" -> "安全管理";
            default -> "其他";
        };
    }

    // ==================== PDF 导出方法 ====================

    private PdfFont getChineseFont() {
        try {
            return PdfFontFactory.createFont("C:/Windows/Fonts/simsun.ttc,0", PdfEncodings.IDENTITY_H);
        } catch (Exception e) {
            try {
                return PdfFontFactory.createFont("C:/Windows/Fonts/msyh.ttc,0", PdfEncodings.IDENTITY_H);
            } catch (Exception ex) {
                log.warn("无法加载中文字体，使用默认字体");
                return null;
            }
        }
    }

    private Cell createHeaderCell(String text, PdfFont font) {
        Cell cell = new Cell().add(new Paragraph(text));
        cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
        cell.setTextAlignment(TextAlignment.CENTER);
        if (font != null) cell.setFont(font);
        return cell;
    }

    private Cell createDataCell(String text, PdfFont font) {
        Cell cell = new Cell().add(new Paragraph(text != null ? text : ""));
        cell.setTextAlignment(TextAlignment.CENTER);
        if (font != null) cell.setFont(font);
        return cell;
    }

    /**
     * 导出验收记录PDF报表
     */
    public byte[] exportAcceptanceReportPdf(Long storeId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<DrugAcceptance> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) wrapper.eq(DrugAcceptance::getStoreId, storeId);
        if (startDate != null) wrapper.ge(DrugAcceptance::getAcceptTime, startDate.atStartOfDay());
        if (endDate != null) wrapper.le(DrugAcceptance::getAcceptTime, endDate.plusDays(1).atStartOfDay());
        wrapper.orderByDesc(DrugAcceptance::getAcceptTime);
        
        List<DrugAcceptance> records = acceptanceMapper.selectList(wrapper);
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);
            PdfFont font = getChineseFont();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            Paragraph title = new Paragraph("药品验收记录报表");
            title.setTextAlignment(TextAlignment.CENTER).setFontSize(18);
            if (font != null) title.setFont(font);
            doc.add(title);
            
            doc.add(new Paragraph("报表期间: " + startDate + " 至 " + endDate)
                    .setFont(font).setFontSize(10).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("\n"));
            
            Table table = new Table(UnitValue.createPercentArray(new float[]{16, 20, 14, 14, 18, 18}));
            table.setWidth(UnitValue.createPercentValue(100));
            
            String[] headers = {"验收时间", "商品名称", "批号", "数量", "外观检查", "综合结果"};
            for (String h : headers) {
                table.addHeaderCell(createHeaderCell(h, font));
            }
            
            for (DrugAcceptance record : records) {
                table.addCell(createDataCell(record.getAcceptTime() != null ? record.getAcceptTime().format(formatter) : "", font));
                table.addCell(createDataCell(record.getDrugName(), font));
                table.addCell(createDataCell(record.getBatchNo(), font));
                table.addCell(createDataCell(record.getQuantity() != null ? record.getQuantity().toString() : "", font));
                table.addCell(createDataCell(record.getAppearanceCheck(), font));
                table.addCell(createDataCell(record.getOverallResult(), font));
            }
            
            doc.add(table);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("导出验收PDF报表失败", e);
            throw new RuntimeException("导出失败");
        }
    }

    /**
     * 导出养护记录PDF报表
     */
    public byte[] exportMaintenanceReportPdf(Long storeId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<DrugMaintenance> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) wrapper.eq(DrugMaintenance::getStoreId, storeId);
        if (startDate != null) wrapper.ge(DrugMaintenance::getMaintenanceTime, startDate.atStartOfDay());
        if (endDate != null) wrapper.le(DrugMaintenance::getMaintenanceTime, endDate.plusDays(1).atStartOfDay());
        wrapper.orderByDesc(DrugMaintenance::getMaintenanceTime);
        
        List<DrugMaintenance> records = maintenanceMapper.selectList(wrapper);
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);
            PdfFont font = getChineseFont();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            Paragraph title = new Paragraph("药品养护记录报表");
            title.setTextAlignment(TextAlignment.CENTER).setFontSize(18);
            if (font != null) title.setFont(font);
            doc.add(title);
            
            doc.add(new Paragraph("报表期间: " + startDate + " 至 " + endDate)
                    .setFont(font).setFontSize(10).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("\n"));
            
            Table table = new Table(UnitValue.createPercentArray(new float[]{14, 16, 12, 12, 12, 12, 22}));
            table.setWidth(UnitValue.createPercentValue(100));
            
            String[] headers = {"养护日期", "商品名称", "养护类型", "外观检查", "包装检查", "综合结果", "异常描述"};
            for (String h : headers) {
                table.addHeaderCell(createHeaderCell(h, font));
            }
            
            for (DrugMaintenance record : records) {
                table.addCell(createDataCell(record.getMaintenanceTime() != null ? record.getMaintenanceTime().format(formatter) : "", font));
                table.addCell(createDataCell(record.getDrugName(), font));
                table.addCell(createDataCell(record.getMaintenanceType(), font));
                table.addCell(createDataCell(record.getAppearanceCheck(), font));
                table.addCell(createDataCell(record.getPackageCheck(), font));
                table.addCell(createDataCell(record.getOverallResult(), font));
                table.addCell(createDataCell(record.getAbnormalDesc(), font));
            }
            
            doc.add(table);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("导出养护PDF报表失败", e);
            throw new RuntimeException("导出失败");
        }
    }

    /**
     * 导出温湿度记录PDF报表
     */
    public byte[] exportTemperatureReportPdf(Long storeId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<TemperatureHumidityLog> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) wrapper.eq(TemperatureHumidityLog::getStoreId, storeId);
        if (startDate != null) wrapper.ge(TemperatureHumidityLog::getRecordTime, startDate.atStartOfDay());
        if (endDate != null) wrapper.le(TemperatureHumidityLog::getRecordTime, endDate.plusDays(1).atStartOfDay());
        wrapper.orderByDesc(TemperatureHumidityLog::getRecordTime);
        
        List<TemperatureHumidityLog> records = tempLogMapper.selectList(wrapper);
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);
            PdfFont font = getChineseFont();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            Paragraph title = new Paragraph("温湿度监控记录报表");
            title.setTextAlignment(TextAlignment.CENTER).setFontSize(18);
            if (font != null) title.setFont(font);
            doc.add(title);
            
            doc.add(new Paragraph("报表期间: " + startDate + " 至 " + endDate)
                    .setFont(font).setFontSize(10).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("\n"));
            
            Table table = new Table(UnitValue.createPercentArray(new float[]{18, 18, 16, 16, 16, 16}));
            table.setWidth(UnitValue.createPercentValue(100));
            
            String[] headers = {"记录时间", "存储位置", "温度(℃)", "湿度(%)", "是否异常", "处理状态"};
            for (String h : headers) {
                table.addHeaderCell(createHeaderCell(h, font));
            }
            
            for (TemperatureHumidityLog record : records) {
                table.addCell(createDataCell(record.getRecordTime() != null ? record.getRecordTime().format(formatter) : "", font));
                table.addCell(createDataCell(record.getLocation(), font));
                table.addCell(createDataCell(record.getTemperature() != null ? record.getTemperature().toString() : "", font));
                table.addCell(createDataCell(record.getHumidity() != null ? record.getHumidity().toString() : "", font));
                table.addCell(createDataCell(Boolean.TRUE.equals(record.getIsAbnormal()) ? "是" : "否", font));
                table.addCell(createDataCell(record.getHandleStatus(), font));
            }
            
            doc.add(table);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("导出温湿度PDF报表失败", e);
            throw new RuntimeException("导出失败");
        }
    }

    /**
     * 导出培训记录PDF报表
     */
    public byte[] exportTrainingReportPdf(Long storeId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<StaffTraining> wrapper = new LambdaQueryWrapper<>();
        if (storeId != null) wrapper.eq(StaffTraining::getStoreId, storeId);
        if (startDate != null) wrapper.ge(StaffTraining::getTrainingDate, startDate);
        if (endDate != null) wrapper.le(StaffTraining::getTrainingDate, endDate);
        wrapper.eq(StaffTraining::getStatus, "completed");
        wrapper.orderByDesc(StaffTraining::getTrainingDate);
        
        List<StaffTraining> records = trainingMapper.selectList(wrapper);
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);
            PdfFont font = getChineseFont();
            
            Paragraph title = new Paragraph("员工培训记录报表");
            title.setTextAlignment(TextAlignment.CENTER).setFontSize(18);
            if (font != null) title.setFont(font);
            doc.add(title);
            
            doc.add(new Paragraph("报表期间: " + startDate + " 至 " + endDate)
                    .setFont(font).setFontSize(10).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("\n"));
            
            Table table = new Table(UnitValue.createPercentArray(new float[]{12, 20, 12, 12, 10, 10, 24}));
            table.setWidth(UnitValue.createPercentValue(100));
            
            String[] headers = {"培训日期", "培训主题", "培训类型", "讲师", "时长(h)", "人数", "地点"};
            for (String h : headers) {
                table.addHeaderCell(createHeaderCell(h, font));
            }
            
            for (StaffTraining record : records) {
                table.addCell(createDataCell(record.getTrainingDate() != null ? record.getTrainingDate().toString() : "", font));
                table.addCell(createDataCell(record.getTitle(), font));
                table.addCell(createDataCell(getTrainingTypeName(record.getTrainingType()), font));
                table.addCell(createDataCell(record.getTrainer(), font));
                table.addCell(createDataCell(record.getDuration() != null ? record.getDuration().toString() : "", font));
                table.addCell(createDataCell(record.getAttendeeCount() != null ? record.getAttendeeCount().toString() : "", font));
                table.addCell(createDataCell(record.getLocation(), font));
            }
            
            doc.add(table);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("导出培训PDF报表失败", e);
            throw new RuntimeException("导出失败");
        }
    }
}
