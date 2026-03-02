# YF 药房管理系统 - 开发记录文档

> 本文档记录了本次会话中完成的所有开发工作，供后续开发对话参考。

---

## 一、项目概述

- **项目名称**: YF 药房管理系统 (SaaS多租户)
- **项目路径**: F:\yf
- **独立性**: 与 BPS 血液净化系统完全独立，无任何关联
- **核心定位**: GSP合规的药房管理系统，支持单体/连锁药店，含西药、中药饮片、POS销售、会员、库存等全流程管理

## 二、技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.12 |
| Java | JDK | 17 |
| ORM | MyBatis-Plus | 3.5.5 |
| 数据库迁移 | Flyway | - |
| 认证 | Spring Security + JJWT | 0.12.3 |
| Excel处理 | EasyExcel | - |
| 工具库 | Hutool | - |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis | - |
| 前端框架 | Vue | 3.4.0 |
| 构建工具 | Vite | 5.0.8 |
| UI组件库 | Element Plus | 2.4.4 |
| 状态管理 | Pinia | - |
| HTTP请求 | Axios | - |
| 图表 | ECharts | - |

## 三、项目结构

```
F:\yf\
├── backend/                          # Spring Boot 后端
│   └── src/main/java/com/yf/
│       ├── config/                   # 配置类
│       │   ├── MybatisPlusConfig.java        # MyBatis-Plus + 多租户 + 分页
│       │   ├── SecurityConfig.java           # Spring Security (JWT无状态)
│       │   └── tenant/
│       │       ├── TenantContext.java         # ThreadLocal租户上下文
│       │       └── YfTenantHandler.java       # 租户SQL自动注入
│       ├── controller/               # REST API (17个)
│       │   ├── AuthController.java           # 登录认证
│       │   ├── DrugController.java           # 药品管理
│       │   ├── DrugCategoryController.java   # 药品分类
│       │   ├── SupplierController.java       # 供应商
│       │   ├── HerbController.java           # 中药饮片
│       │   ├── HerbCabinetController.java    # 斗谱管理
│       │   ├── HerbGspController.java        # 中药GSP
│       │   ├── HerbPrescriptionController.java # 中药处方
│       │   ├── InventoryController.java      # 库存查询
│       │   ├── StockInController.java        # 入库管理
│       │   ├── SaleOrderController.java      # 销售/POS
│       │   ├── MemberController.java         # 会员管理
│       │   ├── GspController.java            # GSP合规
│       │   ├── SysConfigController.java      # 系统配置
│       │   ├── StoreController.java          # 门店管理
│       │   ├── QuickSetupController.java     # 快速配置向导
│       │   └── DataMigrationController.java  # 数据迁移
│       ├── dto/                      # 数据传输对象
│       │   ├── LoginRequest.java
│       │   ├── LoginResponse.java
│       │   ├── QuickSetupRequest.java        # 单体/连锁一键配置
│       │   └── DataMigrationRequest.java
│       ├── entity/                   # 实体类 (38个，均继承BaseEntity)
│       │   ├── BaseEntity.java               # 基类(id/tenantId/审计/逻辑删除)
│       │   ├── Tenant.java                   # 租户
│       │   ├── SysUser.java                  # 系统用户
│       │   ├── Store.java                    # 门店
│       │   ├── SysConfig.java                # 系统配置
│       │   ├── Drug.java                     # 药品
│       │   ├── DrugCategory.java             # 药品分类
│       │   ├── DrugBatch.java                # 药品批次
│       │   ├── DrugTraceCode.java            # 追溯码
│       │   ├── Supplier.java                 # 供应商
│       │   ├── SupplierQualification.java    # 供应商资质
│       │   ├── Herb.java                     # 中药
│       │   ├── HerbCabinet.java              # 药柜
│       │   ├── HerbCabinetCell.java          # 药柜格子
│       │   ├── HerbCabinetFillRecord.java    # 装斗记录
│       │   ├── HerbCabinetCleanRecord.java   # 清斗记录
│       │   ├── HerbAcceptanceRecord.java     # 中药验收
│       │   ├── HerbMaintenanceRecord.java    # 中药养护
│       │   ├── HerbPrescription.java         # 中药处方
│       │   ├── HerbPrescriptionItem.java     # 处方明细
│       │   ├── HerbDispensingRecord.java     # 调配记录
│       │   ├── Inventory.java                # 库存
│       │   ├── StockIn.java / StockInDetail.java      # 入库
│       │   ├── StockOut.java / StockOutDetail.java     # 出库
│       │   ├── StockCheck.java / StockCheckDetail.java # 盘点
│       │   ├── SaleOrder.java / SaleOrderDetail.java   # 销售订单
│       │   ├── RefundOrder.java              # 退货
│       │   ├── Member.java                   # 会员
│       │   ├── MemberLevel.java              # 会员等级
│       │   ├── MemberPointsLog.java          # 积分日志
│       │   ├── DrugAcceptance.java           # 药品验收
│       │   ├── DrugMaintenance.java          # 药品养护
│       │   ├── TemperatureHumidityLog.java   # 温湿度记录
│       │   └── DataMigrationTask.java        # 数据迁移任务
│       ├── exception/                # 异常处理
│       │   ├── BusinessException.java
│       │   └── GlobalExceptionHandler.java
│       ├── mapper/                   # MyBatis Mapper (35个)
│       ├── service/                  # 业务服务 (21个)
│       ├── util/
│       │   └── JwtUtil.java
│       └── vo/
│           └── ApiResponse.java              # 统一响应格式
│   └── src/main/resources/
│       ├── application.yml                   # 应用配置
│       └── db/migration/
│           ├── V1__init_pharmacy_schema.sql   # 50+核心表
│           ├── V2__herb_knowledge_seed_data.sql # 200+中药知识库
│           └── V3__sys_config_and_migration.sql # 系统配置+迁移任务表
│
├── frontend/                         # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── api/                      # API模块 (7个)
│       │   ├── drug.js               # 药品/分类/供应商
│       │   ├── herb.js               # 中药/斗谱/处方
│       │   ├── inventory.js          # 库存/入库
│       │   ├── sale.js               # 销售/POS
│       │   ├── member.js             # 会员
│       │   ├── gsp.js                # GSP合规
│       │   └── system.js             # 系统配置/门店/迁移
│       ├── router/index.js           # 路由 (55+路由)
│       ├── store/auth.js             # Pinia认证状态
│       ├── styles/index.scss         # 全局样式
│       ├── utils/request.js          # Axios封装(JWT注入/401拦截)
│       └── views/                    # 页面 (23个Vue组件)
│           ├── Layout.vue            # 主布局(侧边栏+面包屑)
│           ├── Login.vue             # 登录页
│           ├── dashboard/Index.vue   # 首页仪表盘
│           ├── drug/                 # DrugList/CategoryManage/SupplierList
│           ├── herb/                 # HerbList/CabinetManage/GspRecords/PrescriptionList
│           ├── inventory/            # InventoryList/StockInList/Warnings
│           ├── sale/                 # POS/OrderList
│           ├── member/               # MemberList
│           ├── gsp/                  # AcceptanceList/MaintenanceList/TemperatureLog
│           └── system/               # ConfigManage/StoreManage/DataMigration
│
└── mobile-app/                       # UniApp移动端 (预留)
```

## 四、数据库迁移文件说明

### V1__init_pharmacy_schema.sql
- 50+ 核心业务表，含完整索引和外键
- 初始数据: 1个租户、1个管理员、1个门店、4个会员等级、6个药品分类
- 15条配伍禁忌规则、7条联合用药推荐

### V2__herb_knowledge_seed_data.sql
- 200+ 味中药饮片，按20个治疗分类组织
- 完整元数据: 性味(寒/热/温/凉/平)、归经、功效、适应症、用量范围、毒性
- 18条十八反配伍、19条十九畏配伍
- 孕妇禁忌(禁用 vs 慎用)
- 分类: 解表药/清热药/泻下药/祛风湿药/化湿药/利水渗湿药/温里药/理气药/消食药/止血药/活血祛瘀药/化痰止咳平喘药/安神药/平肝息风药/补气药/补血药/补阴药/补阳药/收涩药/其他常用药

### V3__sys_config_and_migration.sql
- sys_config 系统配置表 (分组键值对 + 唯一约束)
- data_migration_task 数据迁移任务表
- 默认配置数据: 基础/GSP/库存/销售/功能开关共22项

## 五、核心架构设计

### 多租户隔离
- `TenantContext` (ThreadLocal) 存储当前请求的租户ID
- `YfTenantHandler` 实现 MyBatis-Plus `TenantLineHandler`，所有SQL自动注入 `tenant_id` 条件
- 排除租户过滤的公共表: sys_tenant, drug_knowledge, herb_knowledge, herb_incompatibility, medical_insurance_catalog, combined_medication, data_dictionary

### JWT认证流程
- 登录 -> AuthService 验证 -> JwtUtil 生成 Token (含 userId/tenantId)
- 请求 -> JwtAuthenticationFilter 解析 Bearer Token -> 设置 TenantContext + SecurityContext
- 前端 Axios 拦截器自动注入 Authorization 头，401 自动跳转登录

### 单体/连锁模式
- `QuickSetupService` 一键初始化: 更新租户 -> 创建门店 -> 写入系统配置 -> 设置功能开关
- 单体模式: 1个主店，无跨店功能
- 连锁模式: 1个总部 + N个分店，启用统一采购、跨店调拨、合并报表

## 六、尚未完成的模块

以下模块有实体和Mapper但缺少Service/Controller，需后续补充:

| 模块 | 实体 | 缺少 |
|------|------|------|
| 出库管理 | StockOut/StockOutDetail | Service + Controller + 前端页面 |
| 盘点管理 | StockCheck/StockCheckDetail | Service + Controller + 前端页面 |
| 退货管理 | RefundOrder | Service + Controller + 前端页面 |
| 首页统计 | - | DashboardController (后端统计API) |
| 药品批次 | DrugBatch | 独立管理Service (当前可通过DrugService管理) |
| 追溯码 | DrugTraceCode | 独立管理Service |
| 数据迁移 | DataMigrationTask | EasyExcel 实际读取解析逻辑 (框架已搭建) |

## 七、构建验证结果

- **后端 Maven 编译**: 通过 (`mvn compile -q` exit code 0)
- **前端 Vite 构建**: 通过 (`npx vite build` 8.51s, 全部23个Vue组件成功编译)
- 前端构建产出 dist/ 目录，含完整的 JS/CSS 资源

## 八、构建命令

```bash
# 后端编译
cd F:\yf\backend && mvn compile -q

# 后端打包
cd F:\yf\backend && mvn clean package -DskipTests -q

# 前端安装依赖
cd F:\yf\frontend && npm install

# 前端构建
cd F:\yf\frontend && npx vite build

# 前端开发服务器
cd F:\yf\frontend && npm run dev
```
