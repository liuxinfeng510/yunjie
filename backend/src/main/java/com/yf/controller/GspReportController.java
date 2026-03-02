package com.yf.controller;

import com.yf.service.GspReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * GSP报表导出控制器
 * 支持Excel和PDF两种格式导出
 */
@RestController
@RequestMapping("/gsp-report")
@RequiredArgsConstructor
public class GspReportController {

    private final GspReportService reportService;

    // ==================== Excel 导出 ====================

    @GetMapping("/acceptance/export")
    public ResponseEntity<byte[]> exportAcceptance(
            @RequestParam(required = false) Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        byte[] data = reportService.exportAcceptanceReport(storeId, startDate, endDate);
        return buildExcelResponse(data, "验收记录_" + startDate + "_" + endDate + ".xlsx");
    }

    @GetMapping("/maintenance/export")
    public ResponseEntity<byte[]> exportMaintenance(
            @RequestParam(required = false) Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        byte[] data = reportService.exportMaintenanceReport(storeId, startDate, endDate);
        return buildExcelResponse(data, "养护记录_" + startDate + "_" + endDate + ".xlsx");
    }

    @GetMapping("/temperature/export")
    public ResponseEntity<byte[]> exportTemperature(
            @RequestParam(required = false) Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        byte[] data = reportService.exportTemperatureReport(storeId, startDate, endDate);
        return buildExcelResponse(data, "温湿度记录_" + startDate + "_" + endDate + ".xlsx");
    }

    @GetMapping("/training/export")
    public ResponseEntity<byte[]> exportTraining(
            @RequestParam(required = false) Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        byte[] data = reportService.exportTrainingReport(storeId, startDate, endDate);
        return buildExcelResponse(data, "培训记录_" + startDate + "_" + endDate + ".xlsx");
    }

    // ==================== PDF 导出 ====================

    @GetMapping("/acceptance/export-pdf")
    public ResponseEntity<byte[]> exportAcceptancePdf(
            @RequestParam(required = false) Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        byte[] data = reportService.exportAcceptanceReportPdf(storeId, startDate, endDate);
        return buildPdfResponse(data, "验收记录_" + startDate + "_" + endDate + ".pdf");
    }

    @GetMapping("/maintenance/export-pdf")
    public ResponseEntity<byte[]> exportMaintenancePdf(
            @RequestParam(required = false) Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        byte[] data = reportService.exportMaintenanceReportPdf(storeId, startDate, endDate);
        return buildPdfResponse(data, "养护记录_" + startDate + "_" + endDate + ".pdf");
    }

    @GetMapping("/temperature/export-pdf")
    public ResponseEntity<byte[]> exportTemperaturePdf(
            @RequestParam(required = false) Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        byte[] data = reportService.exportTemperatureReportPdf(storeId, startDate, endDate);
        return buildPdfResponse(data, "温湿度记录_" + startDate + "_" + endDate + ".pdf");
    }

    @GetMapping("/training/export-pdf")
    public ResponseEntity<byte[]> exportTrainingPdf(
            @RequestParam(required = false) Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        byte[] data = reportService.exportTrainingReportPdf(storeId, startDate, endDate);
        return buildPdfResponse(data, "培训记录_" + startDate + "_" + endDate + ".pdf");
    }

    private ResponseEntity<byte[]> buildExcelResponse(byte[] data, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        return ResponseEntity.ok().headers(headers).body(data);
    }

    private ResponseEntity<byte[]> buildPdfResponse(byte[] data, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
