# 数据库配置说明

## 问题说明

如果遇到 `Access denied for user 'root'@'localhost'` 错误，说明数据库连接配置不正确。

## 解决方案

### 方案一：修改配置文件中的数据库密码（推荐）

1. 打开 `backend/src/main/resources/application.yml`
2. 修改数据库连接配置，将 `password` 改为你的 MySQL root 用户的实际密码：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/smart_charge?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 你的MySQL密码  # 修改这里
```

### 方案二：创建专用数据库用户（更安全）

1. 登录 MySQL：
```bash
mysql -u root -p
```

2. 创建数据库：
```sql
CREATE DATABASE IF NOT EXISTS smart_charge DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. 创建专用用户并授权：
```sql
-- 创建用户（将 'your_password' 替换为你想要的密码）
CREATE USER 'smartcharge'@'localhost' IDENTIFIED BY 'your_password';

-- 授予权限
GRANT ALL PRIVILEGES ON smart_charge.* TO 'smartcharge'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;
```

4. 修改 `application.yml` 配置：
```yaml
spring:
  datasource:
    username: smartcharge
    password: your_password  # 使用上面设置的密码
```

### 方案三：如果 MySQL root 用户没有密码

如果你的 MySQL root 用户没有设置密码，可以：

1. 修改 `application.yml`：
```yaml
spring:
  datasource:
    password:  # 留空
```

或者：

2. 为 root 用户设置密码：
```sql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
```

然后修改配置文件中的密码。

## 数据库初始化

配置好数据库连接后，系统会自动创建表结构（因为 `ddl-auto: update`）。

如果需要手动初始化数据库，可以执行：

```bash
# 进入 MySQL
mysql -u root -p

# 选择数据库
USE smart_charge;

# 执行初始化脚本
SOURCE backend/src/main/resources/db/init.sql;

# 执行示例数据脚本（可选）
SOURCE backend/src/main/resources/db/sample_data.sql;
```

## 验证连接

配置完成后，重启后端服务。如果看到以下日志，说明连接成功：

```
HikariPool-1 - Starting...
HikariPool-1 - Start completed.
```

## 常见问题

### 1. 数据库不存在
确保数据库 `smart_charge` 已创建：
```sql
CREATE DATABASE IF NOT EXISTS smart_charge DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 端口不是 3306
如果 MySQL 运行在其他端口，修改 `application.yml` 中的 URL：
```yaml
url: jdbc:mysql://localhost:你的端口/smart_charge?...
```

### 3. MySQL 8.0 认证问题
如果使用 MySQL 8.0，确保连接 URL 中包含 `allowPublicKeyRetrieval=true`（已在配置中）。

### 4. 忘记 root 密码
重置 MySQL root 密码的方法请参考 MySQL 官方文档。

