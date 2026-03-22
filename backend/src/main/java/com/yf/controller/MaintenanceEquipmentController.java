package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.EquipmentInspectionLog;
import com.yf.entity.MaintenanceEquipment;
import com.yf.service.MaintenanceEquipmentService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/maintenance-equipment")
@RequiredArgsConstructor
public class MaintenanceEquipmentController {

    private final MaintenanceEquipmentService service;

    @PostMapping
    public ApiResponse<MaintenanceEquipment> create(@RequestBody MaintenanceEquipment eq) {
        return ApiResponse.success(service.createEquipment(eq));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody MaintenanceEquipment eq) {
        service.updateEquipment(id, eq);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/retire")
    public ApiResponse<Void> retire(@PathVariable Long id) {
        service.retireEquipment(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/page")
    public ApiResponse<Page<MaintenanceEquipment>> page(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(service.pageEquipments(type, status, pageNum, pageSize));
    }

    @GetMapping("/list")
    public ApiResponse<List<MaintenanceEquipment>> list() {
        return ApiResponse.success(service.listEquipments());
    }

    @PostMapping("/inspection")
    public ApiResponse<EquipmentInspectionLog> createInspection(@RequestBody EquipmentInspectionLog log) {
        return ApiResponse.success(service.createInspection(log));
    }

    @GetMapping("/inspection/page")
    public ApiResponse<Page<EquipmentInspectionLog>> inspectionPage(
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(service.pageInspections(equipmentId, startDate, endDate, pageNum, pageSize));
    }

    @GetMapping("/inspection/list")
    public ApiResponse<List<EquipmentInspectionLog>> inspectionList(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return ApiResponse.success(service.listInspections(startDate, endDate));
    }
}
