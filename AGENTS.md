# YF 药房管理系统 - 开发指南

## 项目结构

```
F:\yf\
├── backend/          # Spring Boot 3.2 后端 (Java 17)
│   └── src/main/java/com/yf/
│       ├── config/       # 配置类（MyBatis-Plus、Security、Tenant）
│       ├── controller/   # REST API 控制器
│       ├── dto/          # 数据传输对象
│       ├── entity/       # 实体类
│       ├── exception/    # 异常处理
│       ├── mapper/       # MyBatis-Plus Mapper
│       ├── service/      # 业务逻辑服务
│       ├── util/         # 工具类（JWT等）
│       └── vo/           # 视图对象（ApiResponse）
├── frontend/         # Vue 3.4 + Vite 5.0 前端
│   └── src/
│       ├── api/          # API 请求模块
│       ├── components/   # 公共组件
│       ├── router/       # 路由配置
│       ├── store/        # Pinia 状态管理
│       ├── styles/       # 全局样式
│       ├── utils/        # 工具函数
│       └── views/        # 页面组件
└── mobile-app/       # UniApp 移动端（预留）
```

## 技术栈

### 后端
- **Java 17** + **Spring Boot 3.2.12**
- **MyBatis-Plus 3.5.5** (ORM + 多租户)
- **Flyway** (数据库版本迁移)
- **Spring Security** + **JJWT 0.12.3** (认证授权)
- **EasyExcel** (Excel导入导出)
- **Hutool** (工具库)
- **MySQL 8.0** + **Redis**

### 前端
- **Vue 3.4** + **Vite 5.0.8**
- **Element Plus 2.4.4** (UI组件库)
- **Pinia** (状态管理)
- **Axios** (HTTP请求)
- **ECharts** (图表)
- **Vue Router** (路由)

## 构建命令

### 后端
```bash
cd F:\yf\backend && mvn compile -q              # 编译检查
cd F:\yf\backend && mvn clean package -DskipTests -q  # 打包JAR
```

### 前端
```bash
cd F:\yf\frontend && npm install                # 安装依赖
cd F:\yf\frontend && npm run build              # 生产构建
cd F:\yf\frontend && npm run dev                # 开发服务器
```

## 核心架构

### 多租户隔离
- 通过 `TenantContext` (ThreadLocal) 存储当前租户ID
- `YfTenantHandler` 实现 MyBatis-Plus `TenantLineHandler`，自动注入 `tenant_id` 条件
- 排除表：sys_tenant、drug_knowledge、herb_knowledge、herb_incompatibility 等公共表

### 认证流程
- JWT Token 通过 `Authorization: Bearer <token>` 传递
- `JwtAuthenticationFilter` 解析 Token 并设置 TenantContext
- SecurityConfig 放行 `/auth/**`，其余接口需认证

### API 规范
- 统一响应格式：`ApiResponse<T>` (code/message/data)
- 分页使用 MyBatis-Plus `Page<T>`
- 路径规范：`/模块名/操作`，如 `/drug/page`、`/store/list`

### 数据库迁移
- 使用 Flyway 管理迁移脚本
- 脚本位置：`backend/src/main/resources/db/migration/`
- 命名规范：`V{版本号}__{描述}.sql`

## 业务模块

| 模块 | 后端路径 | 前端路径 | 说明 |
|------|----------|----------|------|
| 药品管理 | DrugController | views/drug/ | 西药/成药/OTC管理 |
| 中药管理 | HerbController | views/herb/ | 中药饮片/斗谱/处方 |
| 库存管理 | InventoryController | views/inventory/ | 入库/出库/盘点/预警 |
| 销售管理 | SaleOrderController | views/sale/ | POS收银/订单管理 |
| 会员管理 | MemberController | views/member/ | 会员/积分/等级 |
| GSP合规 | GspController | views/gsp/ | 验收/养护/温湿度 |
| 系统管理 | SysConfigController | views/system/ | 配置/门店/数据迁移 |

## 部署信息

- **服务器**: 62.234.37.40 (root via SSH)
- **后端部署路径**: `/opt/yf-pharmacy/yf-pharmacy.jar` (systemd 服务: `yf-pharmacy`)
- **前端部署路径**: `/opt/yf-pharmacy/frontend/` (nginx root，构建产物直接放此目录)
- **数据库**: MySQL `yf_pharmacy` (本地 root 无密码)
- **代码仓库**:
  - GitHub: `https://github.com/liuxinfeng510/yunjie.git` (origin)
  - Gitee: `git@gitee.com:liuxinfeng-yunfeng/yunjie.git` (gitee)

### 部署流程
```bash
# 前端部署（注意：nginx root 指向 /opt/yf-pharmacy/frontend/，不是 dist/ 子目录）
cd F:\yf\frontend && npm run build
ssh root@62.234.37.40 "rm -rf /opt/yf-pharmacy/frontend/assets /opt/yf-pharmacy/frontend/index.html"
scp -r f:\yf\frontend\dist\* root@62.234.37.40:/opt/yf-pharmacy/frontend/

# 后端部署
cd F:\yf\backend && mvn clean package -DskipTests -q
scp f:\yf\backend\target\yf-pharmacy-1.0.0.jar root@62.234.37.40:/opt/yf-pharmacy/yf-pharmacy.jar
ssh root@62.234.37.40 "systemctl restart yf-pharmacy"
```

## 开发注意事项

1. 所有实体类继承 `BaseEntity`，自动包含 id、tenantId、审计字段、逻辑删除
2. 新增 Mapper 后需确保在 `com.yf.mapper` 包下，已配置 `@MapperScan`
3. 前端 API 请求自动注入 JWT Token（通过 Axios 拦截器）
4. 401 响应自动跳转登录页

## 版本记录

### v1.7.1 (2026-03-22)
- **POS搜索优化**: 零库存过滤从内存层面移至SQL层面（EXISTS子查询），修复拼音搜索结果为空的问题
- **POS中药调价**: 中药饮片处方区域单价支持可编辑调价（根据allowPriceAdjust字段控制）
- **POS右键菜单**: 中药处方购物车增加右键功能（查看进价、跳转药品列表）；修复进价字段名(purchasePrice)；进价显示原始值不四舍五入；零售价/单价输入精度从2位改为4位
- **POS按钮右键**: 结算/挂单按钮右键事件改为外层div绑定，解决disabled状态下右键不响应问题；日终对账右键限定在结算明细区域
- **拼音码批量补全**: 新增供应商rebuildPinyin接口，批量补全供应商(215条)、药品(17210条)、会员(21381条)拼音码
- **会员批量导入优化**: 预加载手机号去重(O(1)内存查找替代逐条DB查询)；批量插入(每500条flush)；前端超时从120s增至600s；nginx proxy_read_timeout从120s增至600s
- **药品列表增强**: 新增"销售可调价"列显示；中药饮片编辑表单增加"销售可调价"开关
- **界面优化**: 右上角显示租户名称；浏览器标题改为"云界智慧药房系统"
- **入库审批修复**: StockInController.approve()从SecurityContext获取userId，修复500错误
- **近效期催销**: 默认saleMeasure="accelerate"、status="processing"；A4竖版打印+日期筛选
- **验收记录**: 入库完成自动生成验收记录；增加供应商列和日期筛选+打印功能
- **中药记录自动生成**: 通过categoryId=23识别中药饮片，入库时自动生成装斗/清斗记录

#### v1.7.1 关键文件变更
| 文件 | 变更说明 |
|------|----------|
| `views/sale/POS.vue` | 中药调价、右键菜单修复、零售价精度、按钮右键外层绑定 |
| `views/drug/DrugList.vue` | 新增销售可调价列和中药编辑表单开关 |
| `views/Layout.vue` | 右上角显示租户名称 |
| `router/index.js` | 浏览器标题改为云界智慧药房系统 |
| `api/member.js` | 批量导入超时增至600s |
| `service/DrugService.java` | SQL层面零库存过滤(EXISTS)、page方法增加showZeroStock参数 |
| `service/BatchImportService.java` | 会员批量导入优化(预加载去重+批量插入)、flushMemberBatch/flushDrugBatch |
| `service/SupplierService.java` | 新增rebuildAllPinyin批量补全拼音 |
| `controller/SupplierController.java` | 新增rebuild-pinyin接口 |
| `service/StockInService.java` | 自动生成验收记录和中药记录 |
| `controller/StockInController.java` | 审批接口从SecurityContext获取userId |

### v1.6.0 (2026-03-20)
- **POS收银 - 中药处方模式**: 中药饮片按克计价，支持副数设置，购物车分区显示（普通药品+中药处方），独立处方笺自动打印
- **对账管理**: 日结/交接班对账功能 (ReconciliationController)
- **会员购药历史**: 购药记录查询页面 (PurchaseHistory.vue)
- **库存预警增强**: 预警功能优化
- **药品管理**: 批次管理优化、字段扩展（stockQuantity、拼音码等）
- **数据库迁移**: V35-V43（含中药销售字段、对账表、小票配置等）

#### v1.6.0 关键文件变更
| 文件 | 变更说明 |
|------|----------|
| `views/sale/POS.vue` | 中药处方模式核心实现：购物车分区、按克计价、副数、处方笺打印、挂单/取单兼容 |
| `entity/SaleOrder.java` | 新增 herbDoseCount 字段 |
| `entity/SaleOrderDetail.java` | 新增 isHerb、dosePerGram、doseCount 字段 |
| `V43__add_herb_sale_fields.sql` | sale_order/sale_order_detail 新增中药相关列 |
| `views/sale/Reconciliation.vue` | 对账管理页面 |
| `ReconciliationController.java` | 对账 REST API |
| `views/member/PurchaseHistory.vue` | 会员购药历史 |

#### v1.6.0 中药处方模式说明
- **分类识别**: 通过 `drug_category` 表匹配含"中药饮片/中草药/中药"的分类名（含子分类递归收集）
- **系统级中药分类**: `drug_category.id=23`（"中药饮片"），含子分类 id=25-35
- **购物车分区**: `herbCartItems` / `normalCartItems` 计算属性过滤
- **价格计算**: 中药 = dosePerGram × herbDoseCount × retailPrice；普通药 = quantity × retailPrice
- **打印**: normalCartItems > 0 打印普通小票，herbCartItems > 0 自动打印处方笺（300ms延迟）

### v1.5.0
- 批量导入 + 药品原编号 + 知识库管理 + 数据迁移增强

### v1.4.0
- 首营管理 + AI识别建档 + 全局生产企业资料库

### v1.3.0
- 完善药房管理系统核心功能

### v1.2.0
- Phase 6-8 补充开发 - GSP合规/移动端/营销功能

### v1.0.0
- YF药房管理系统初始项目结构
