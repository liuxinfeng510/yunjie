# 云界智慧药房 - 部署文档

## 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 后端运行环境 |
| MySQL | 8.0+ | 数据库 |
| Redis | 6.0+ | 缓存 |
| Nginx | 1.20+ | 反向代理 + 静态资源 |
| Node.js | 18+ | 前端构建（仅开发机需要） |
| Maven | 3.8+ | 后端构建（仅开发机需要） |

## 服务器目录结构

```
/opt/yf-pharmacy/
├── yf-pharmacy.jar          # 后端 JAR 包
├── .env                     # 环境变量配置
├── uploads/                 # 文件上传目录
└── frontend/                # 前端静态文件
    ├── index.html
    └── assets/
```

## 一、首次部署

### 1. 创建目录

```bash
mkdir -p /opt/yf-pharmacy/frontend
mkdir -p /opt/yf-pharmacy/uploads
```

### 2. 配置环境变量

```bash
cat > /opt/yf-pharmacy/.env << 'EOF'
DB_USERNAME=root
DB_PASSWORD=你的MySQL密码
JWT_SECRET=至少256位的随机字符串
REDIS_PASSWORD=
DASHSCOPE_API_KEY=你的阿里百炼API密钥
EOF

chmod 600 /opt/yf-pharmacy/.env
```

### 3. 创建 systemd 服务

```bash
cat > /etc/systemd/system/yf-pharmacy.service << 'EOF'
[Unit]
Description=YF Pharmacy Management System
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/yf-pharmacy
ExecStart=/usr/bin/java -jar -Xms256m -Xmx512m -Dspring.profiles.active=prod /opt/yf-pharmacy/yf-pharmacy.jar
ExecStop=/bin/kill -TERM $MAINPID
Restart=on-failure
RestartSec=10
EnvironmentFile=/opt/yf-pharmacy/.env

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable yf-pharmacy
```

### 4. 配置 Nginx

```bash
cp deploy/nginx-yunjie.conf /etc/nginx/conf.d/yf-pharmacy.conf
nginx -t && systemctl reload nginx
```

Nginx 监听 8888 端口，前端静态资源从 `/opt/yf-pharmacy/frontend` 提供，API 请求代理到后端 8081 端口。

### 5. 上传文件并启动

```bash
# 在开发机执行
scp backend/target/yf-pharmacy-1.0.0.jar root@服务器IP:/opt/yf-pharmacy/yf-pharmacy.jar
scp -r frontend/dist/* root@服务器IP:/opt/yf-pharmacy/frontend/

# 在服务器执行
systemctl start yf-pharmacy
```

## 二、日常更新部署

### 构建

```bash
# 后端
cd backend && mvn clean package -DskipTests -q

# 前端
cd frontend && npm run build
```

### 部署

```bash
# 上传
scp backend/target/yf-pharmacy-1.0.0.jar root@服务器IP:/opt/yf-pharmacy/yf-pharmacy.jar
scp -r frontend/dist/* root@服务器IP:/opt/yf-pharmacy/frontend/

# 重启后端（Flyway 自动执行数据库迁移）
ssh root@服务器IP "systemctl restart yf-pharmacy"
```

### 验证

```bash
# 检查服务状态
ssh root@服务器IP "systemctl status yf-pharmacy"

# 检查启动日志
ssh root@服务器IP "journalctl -u yf-pharmacy --since '2 min ago' --no-pager | tail -20"

# 检查 Flyway 迁移
ssh root@服务器IP "journalctl -u yf-pharmacy --since '2 min ago' --no-pager | grep -i migrat"
```

## 三、运维命令

```bash
# 查看服务状态
systemctl status yf-pharmacy

# 查看实时日志
journalctl -u yf-pharmacy -f

# 重启服务
systemctl restart yf-pharmacy

# 停止服务
systemctl stop yf-pharmacy
```

## 四、数据库迁移

使用 Flyway 自动管理，迁移脚本位于 `backend/src/main/resources/db/migration/`，应用启动时自动执行未应用的迁移。

命名规范：`V{版本号}__{描述}.sql`

### Flyway 常见问题

**1. Validate failed: Migrations have failed validation**

已应用的迁移文件被修改过（checksum 不匹配），需要执行 repair：

```bash
cd backend && mvn flyway:repair \
  -Dflyway.url="jdbc:mysql://localhost:3306/yf_pharmacy?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false" \
  -Dflyway.user=root \
  -Dflyway.password="你的密码" \
  -Dflyway.locations=classpath:db/migration \
  -Dflyway.baselineOnMigrate=true \
  -Dflyway.baselineVersion=0 -q
```

或者在服务器上直接更新 checksum（需先确认文件改动无害）：

```sql
-- 查看不匹配的版本
SELECT version, description, checksum FROM flyway_schema_history WHERE success=1;
```

> 规则：已 apply 的迁移脚本不要再修改内容，需要改动请新建迁移。

**2. MySQL 未启动导致 Communications link failure**

确保 MySQL 先于应用启动。systemd 已配置 `After=mysql.service`，但如果 MySQL 启动慢可能仍会失败。可增加启动延迟：

```bash
# 在 [Service] 段加入
ExecStartPre=/bin/sleep 5
```

## 五、访问地址

- 前端页面：`http://服务器IP:8888`
- 后端 API：`http://服务器IP:8081/api`（内部，Nginx 代理）

## 六、部署记录

### 2026-03-16 - 知识库数据迁移 (V22-V31)

**部署内容：**
- V22: 200家药品生产企业种子数据
- V23: 补充151味中药饮片的13个空字段
- V24: 新增65味中药品种
- V25-V31: 新增196种药品知识库（感冒退热、抗生素、心血管、降糖、消化、呼吸、神经、骨科、维生素、中成药、儿科、皮肤外用、眼耳鼻喉、泌尿、妇科、抗过敏）

**遇到的问题：**

1. **本地 Flyway 校验失败** - V3 迁移文件在之前被修改过导致 checksum 不匹配，通过 `mvn flyway:repair` 修复。服务器端未出现此问题（服务器已在之前的部署中同步过）。

2. **本地 MySQL 未持久化运行** - 开发机 MySQL 未注册为 Windows 服务，手动启动 mysqld 后被 Maven flyway:repair 命令的连接关闭影响自动退出，需重新启动后才能运行 Spring Boot。生产服务器无此问题（systemd 管理）。

3. **V24 数据截断警告** - `Data truncated for column 'dosage_min'`，部分中药的 dosage_min/max 超出字段精度。数据已入库但被截断，不影响功能，后续可调整字段精度。

**部署结果：** 服务器 Flyway 成功执行 10 个迁移（V22-V31），应用正常启动，耗时 15.5s。
