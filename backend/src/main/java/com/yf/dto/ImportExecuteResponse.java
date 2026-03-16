package com.yf.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImportExecuteResponse {
    private int total;
    private int success;
    private int fail;
    private int skip;
    private List<String> errors = new ArrayList<>();
}
