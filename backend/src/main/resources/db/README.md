# 数据库初始化说明

## 文件说明

1. **init.sql** - 数据库表结构创建脚本
   - 包含所有表的创建语句
   - 包含索引和外键约束
   - 包含评价点赞表的创建

2. **sample_data.sql** - 示例数据脚本
   - 包含默认管理员账号
   - 包含示例用户数据
   - 包含充电站和充电桩数据
   - 包含订单、评价、优惠券等业务数据

## 使用方法

### 1. 初始化数据库

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source /path/to/init.sql

# 执行示例数据脚本（可选）
source /path/to/sample_data.sql
```

### 2. 默认账号

#### 管理员账号
- 用户名：`admin`
- 密码：`123456`（MD5加密：e10adc3949ba59abbe56e057f20f883e）
- 角色：系统管理员

#### 测试用户账号
- 手机号：`13800138000`
- 密码：`123456`（MD5加密：e10adc3949ba59abbe56e057f20f883e）
- 昵称：张三

### 3. 数据库配置

在 `application.yml` 中配置数据库连接：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/smart_charge?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root
```

### 4. 表结构说明

主要数据表：
- `sys_user` - 用户表
- `sys_admin` - 管理员表
- `charge_station` - 充电站表
- `charge_pile` - 充电桩表
- `charge_order` - 充电订单表
- `charge_reservation` - 预约表
- `station_evaluation` - 评价表
- `coupon` - 优惠券表
- `user_coupon` - 用户优惠券表
- `payment_record` - 支付记录表
- `points_record` - 积分记录表
- `credit_record` - 信用分记录表
- `notification` - 消息通知表
- `invoice` - 发票表
- `fault_report` - 故障上报表
- `maintenance_ticket` - 运维工单表
- `user_favorite` - 用户收藏表
- `evaluation_like` - 评价点赞表

### 5. 注意事项

1. 确保MySQL版本 >= 5.7
2. 字符集使用 utf8mb4
3. 时区设置为 Asia/Shanghai
4. 所有密码使用MD5加密存储
5. 示例数据中的时间需要根据实际情况调整

### 6. 数据清理

如果需要清空所有数据重新初始化：

```sql
-- 清空所有表数据（保留表结构）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE sys_user;
TRUNCATE TABLE sys_admin;
TRUNCATE TABLE charge_station;
TRUNCATE TABLE charge_pile;
TRUNCATE TABLE charge_order;
-- ... 其他表
SET FOREIGN_KEY_CHECKS = 1;

-- 重新执行示例数据脚本
source /path/to/sample_data.sql
```

