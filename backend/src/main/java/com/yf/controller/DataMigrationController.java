package com.yf.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yf.entity.DataMigrationTask;
import com.yf.service.DataMigrationService;
import com.yf.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据迁移控制器
 */
@RestController
@RequestMapping("/migration")
@RequiredArgsConstructor
public class DataMigrationController {

    private final DataMigrationService dataMigrationService;

    /**
     * 分页查询迁移任务
     */
    @GetMapping("/page")
    public ApiResponse<Page<DataMigrationTask>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String targetModule,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(dataMigrationService.page(
                new Page<>(current, size), targetModule, status));
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public ApiResponse<DataMigrationTask> getById(@PathVariable Long id) {
        return ApiResponse.success(dataMigrationService.getById(id));
    }

    /**
     * 上传文件创建迁移任务
     */
    @PostMapping("/upload")
    public ApiResponse<DataMigrationTask> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam String targetModule,
            @RequestParam(required = false) String sourceType,
            @RequestParam(defaultValue = "true") boolean skipDuplicate) {
        return ApiResponse.success(
                dataMigrationService.uploadAndCreate(file, targetModule, sourceType, skipDuplicate));
    }

    /**
     * 执行迁移任务
     */
    @PostMapping("/{id}/execute")
    public ApiResponse<DataMigrationTask> execute(@PathVariable Long id) {
        return ApiResponse.success(dataMigrationService.execute(id));
    }

    /**
     * 删除迁移任务
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dataMigrationService.delete(id);
        return ApiResponse.success();
    }
}
