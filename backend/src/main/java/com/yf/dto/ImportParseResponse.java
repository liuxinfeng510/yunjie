package com.yf.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ImportParseResponse {
    private String fileToken;
    private List<String> headers;
    private List<FieldMapping> fieldMappings;
    private List<FieldMapping> availableFields;
    private List<Map<String, String>> previewData;
    private int totalRows;
}
