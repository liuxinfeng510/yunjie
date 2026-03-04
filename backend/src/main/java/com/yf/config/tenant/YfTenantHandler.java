package com.yf.config.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

import java.util.Arrays;
import java.util.List;

/**
 * MyBatis-Plus 多租户处理器
 */
public class YfTenantHandler implements TenantLineHandler {

    /** 不需要租户隔离的表 */
    private static final List<String> IGNORE_TABLES = Arrays.asList(
            "sys_tenant",
            "sys_dict_item",
            "drug_knowledge",
            "herb_knowledge",
            "herb_incompatibility",
            "medical_insurance_catalog",
            "combined_medication",
            "data_dictionary",
            "audit_log"
    );

    @Override
    public Expression getTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return new LongValue(0);
        }
        return new LongValue(tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 超级管理员(tenant_id=0)跳过租户过滤，可查看所有租户数据
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null && tenantId == 0L) {
            return true;
        }
        return IGNORE_TABLES.contains(tableName);
    }
}
