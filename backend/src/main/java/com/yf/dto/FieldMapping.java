package com.yf.dto;

import lombok.Data;

@Data
public class FieldMapping {
    private int excelIndex;
    private String excelHeader;
    private String entityField;
    private String entityFieldLabel;
    private boolean required;
}
