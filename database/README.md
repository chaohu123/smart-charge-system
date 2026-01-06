# 数据库设计文档

## 数据库说明

数据库名称：`smart_charge`

字符集：utf8mb4

排序规则：utf8mb4_unicode_ci

## 表结构说明

### 1. sys_user - 用户表
存储用户基本信息、信用分、余额等

### 2. user_vehicle - 车辆信息表
存储用户绑定的车辆信息

### 3. charge_station - 充电站表
存储充电站基本信息、位置、营业时间等

### 4. charge_pile - 充电桩表
存储充电桩设备信息、状态、价格等

### 5. charge_order - 充电订单表
存储充电订单的完整信息

### 6. charge_reservation - 预约表
存储用户预约充电的信息

### 7. station_evaluation - 评价表
存储用户对充电站的评价

### 8. sys_admin - 管理员表
存储管理员账号信息

### 9. payment_record - 支付记录表
存储所有支付交易记录

### 10. maintenance_ticket - 运维工单表
存储设备故障和维护工单

## 初始化说明

1. 执行 `init.sql` 脚本创建数据库和表结构
2. 默认管理员账号：admin / admin123（密码已MD5加密）

