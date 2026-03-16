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

## 五、访问地址

- 前端页面：`http://服务器IP:8888`
- 后端 API：`http://服务器IP:8081/api`（内部，Nginx 代理）
