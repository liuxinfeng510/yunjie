package com.yf.dto;

import lombok.Data;

import java.util.List;

@Data
public class ImportExecuteRequest {
    private String fileToken;
    private List<FieldMapping> fieldMappings;
    private boolean skipDuplicate;
}
