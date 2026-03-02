package com.yf.annotation;

import java.lang.annotation.*;

/**
 * 审计日志注解
 * 标注在Controller方法上，自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /** 业务模块名称 */
    String module();

    /** 操作描述 */
    String operation();
}
