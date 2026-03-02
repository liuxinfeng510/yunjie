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

## 开发注意事项

1. 所有实体类继承 `BaseEntity`，自动包含 id、tenantId、审计字段、逻辑删除
2. 新增 Mapper 后需确保在 `com.yf.mapper` 包下，已配置 `@MapperScan`
3. 前端 API 请求自动注入 JWT Token（通过 Axios 拦截器）
4. 401 响应自动跳转登录页
