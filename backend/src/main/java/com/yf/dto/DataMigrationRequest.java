package com.yf.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 数据迁移上传请求
 */
@Data
public class DataMigrationRequest {

    /** 目标模块: drug / herb / member / supplier / inventory */
    @NotBlank(message = "目标模块不能为空")
    private String targetModule;

    /** 数据来源类型: excel / csv */
    @NotBlank(message = "数据来源类型不能为空")
    private String sourceType;

    /** 是否跳过重复记录 */
    private Boolean skipDuplicate = true;

    /** 是否覆盖已有数据 */
    private Boolean overwriteExisting = false;
}
