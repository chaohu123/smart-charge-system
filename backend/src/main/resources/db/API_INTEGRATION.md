# API接口集成说明

## 前后端集成配置

### 1. 前端代理配置

#### 用户端 (frontend-user)
- 端口：3000
- 代理：`/api` -> `http://localhost:8080`
- 配置文件：`frontend-user/vite.config.js`

#### 管理员端 (frontend-admin)
- 端口：3001
- 代理：`/api` -> `http://localhost:8080`
- 配置文件：`frontend-admin/vite.config.js`

### 2. 后端配置

- 端口：8080
- Context Path：`/api`
- 跨域配置：已启用，允许所有来源
- 配置文件：`backend/src/main/resources/application.yml`

### 3. 认证机制

#### JWT Token
- 所有需要认证的接口都需要在请求头中携带Token
- 格式：`Authorization: Bearer {token}`
- Token有效期：24小时

#### 公开接口（无需Token）
- `/api/user/login` - 用户登录
- `/api/user/register` - 用户注册
- `/api/admin/login` - 管理员登录
- `/api/station/nearby` - 附近充电站
- `/api/station/search` - 搜索充电站
- `/api/station/{id}` - 充电站详情
- `/api/station/{id}/piles` - 充电站桩列表
- `/api/station/{id}/evaluations` - 充电站评价
- `/api/files/**` - 文件访问

## API接口列表

### 用户端接口

#### 用户相关 (`/api/user`)
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user/info` - 获取用户信息
- `PUT /api/user/update` - 更新用户信息
- `POST /api/user/realname` - 实名认证
- `POST /api/user/vehicle/add` - 添加车辆
- `GET /api/user/vehicle/list` - 获取车辆列表
- `POST /api/user/avatar` - 上传头像
- `POST /api/user/password/change` - 修改密码

#### 充电站相关 (`/api/station`)
- `GET /api/station/nearby` - 附近充电站
- `GET /api/station/{id}` - 充电站详情
- `GET /api/station/{id}/piles` - 充电站桩列表
- `GET /api/station/search` - 搜索充电站
- `GET /api/station/{id}/evaluations` - 充电站评价

#### 订单相关 (`/api/order`)
- `POST /api/order/start` - 开始充电
- `POST /api/order/{id}/stop` - 停止充电
- `POST /api/order/{id}/refund` - 退款
- `GET /api/order/current` - 当前订单
- `GET /api/order/list` - 订单列表
- `GET /api/order/{id}` - 订单详情
- `GET /api/order/{id}/status` - 订单状态
- `POST /api/order/{id}/pay` - 支付订单

#### 预约相关 (`/api/reservation`)
- `POST /api/reservation/create` - 创建预约
- `GET /api/reservation/list` - 预约列表
- `POST /api/reservation/{id}/cancel` - 取消预约

#### 支付相关 (`/api/payment`)
- `POST /api/payment/pay` - 支付（备用接口）
- `POST /api/payment/recharge` - 充值
- `GET /api/payment/balance` - 获取余额
- `GET /api/payment/records` - 支付记录

#### 优惠券相关 (`/api/coupon`)
- `GET /api/coupon/list` - 可用优惠券列表
- `GET /api/coupon/my` - 我的优惠券
- `POST /api/coupon/{id}/receive` - 领取优惠券

#### 评价相关 (`/api/evaluation`)
- `POST /api/evaluation/create` - 创建评价
- `POST /api/evaluation/upload` - 上传评价图片
- `POST /api/evaluation/{id}/like` - 点赞评价
- `POST /api/evaluation/{id}/unlike` - 取消点赞
- `GET /api/evaluation/{id}/like/status` - 点赞状态
- `GET /api/evaluation/{id}/like/count` - 点赞数量

#### 收藏相关 (`/api/favorite`)
- `POST /api/favorite/add` - 添加收藏
- `DELETE /api/favorite/remove` - 取消收藏
- `GET /api/favorite/list` - 收藏列表
- `GET /api/favorite/check` - 检查是否收藏

#### 故障上报 (`/api/fault`)
- `POST /api/fault/report` - 上报故障
- `GET /api/fault/list` - 故障列表

#### 发票相关 (`/api/invoice`)
- `POST /api/invoice/apply` - 申请发票
- `GET /api/invoice/list` - 发票列表
- `GET /api/invoice/{id}` - 发票详情
- `GET /api/invoice/download/{id}` - 下载发票

#### 积分相关 (`/api/points`)
- `GET /api/points/balance` - 积分余额
- `GET /api/points/records` - 积分记录

#### 通知相关 (`/api/notification`)
- `GET /api/notification/list` - 通知列表（支持 isRead、type、current、size）
- `GET /api/notification/unread-count` - 未读数量
- `PUT /api/notification/{id}/read` - 标记已读
- `PUT /api/notification/read-all` - 全部标记已读
- `DELETE /api/notification/{id}` - 删除通知

#### 充电桩相关 (`/api/pile`)
- `POST /api/pile/scan` - 扫描二维码
- `POST /api/pile/{id}/qrcode` - 生成二维码

### 管理员端接口

#### 管理员相关 (`/api/admin`)
- `POST /api/admin/login` - 管理员登录
- `GET /api/admin/info` - 获取管理员信息

#### 用户管理 (`/api/admin/user`)
- `GET /api/admin/user/list` - 用户列表
- `GET /api/admin/user/{id}` - 用户详情
- `PUT /api/admin/user/{id}` - 更新用户
- `PUT /api/admin/user/{id}/status` - 更新用户状态
- `PUT /api/admin/user/{id}/credit` - 更新信用分

#### 充电站管理 (`/api/admin/station`)
- `GET /api/admin/station/list` - 充电站列表
- `GET /api/admin/station/{id}` - 充电站详情
- `POST /api/admin/station/add` - 添加充电站
- `PUT /api/admin/station/{id}` - 更新充电站
- `DELETE /api/admin/station/{id}` - 删除充电站
- `POST /api/admin/station/{id}/upload` - 上传图片

#### 充电桩管理 (`/api/admin/pile`)
- `GET /api/admin/pile/list` - 充电桩列表
- `GET /api/admin/pile/{id}` - 充电桩详情
- `POST /api/admin/pile/add` - 添加充电桩
- `PUT /api/admin/pile/{id}` - 更新充电桩
- `DELETE /api/admin/pile/{id}` - 删除充电桩
- `POST /api/admin/pile/{id}/restart` - 重启充电桩

#### 订单管理 (`/api/admin/order`)
- `GET /api/admin/order/list` - 订单列表
- `GET /api/admin/order/{id}` - 订单详情
- `POST /api/admin/order/{id}/refund` - 退款
- `GET /api/admin/order/export` - 导出订单
- `GET /api/admin/order/statistics` - 订单统计

#### 运维管理 (`/api/admin/maintenance`)
- `GET /api/admin/maintenance/list` - 工单列表
- `GET /api/admin/maintenance/{id}` - 工单详情
- `POST /api/admin/maintenance/create` - 创建工单
- `PUT /api/admin/maintenance/{id}/status` - 更新状态

#### 运营管理 (`/api/admin/operation`)
- 活动管理
  - `GET /api/admin/operation/activity/list` - 活动列表（支持 title、status、current、size）
  - `POST /api/admin/operation/activity` - 新增活动
  - `PUT /api/admin/operation/activity/{id}` - 更新活动
  - `DELETE /api/admin/operation/activity/{id}` - 删除活动
- 优惠券管理
  - `GET /api/admin/operation/coupon/list` - 优惠券列表（支持 name、status、current、size）
  - `POST /api/admin/operation/coupon` - 新增优惠券
  - `PUT /api/admin/operation/coupon/{id}` - 更新优惠券
  - `PUT /api/admin/operation/coupon/{id}/status` - 更新状态
  - `DELETE /api/admin/operation/coupon/{id}` - 删除优惠券
- `PUT /api/admin/maintenance/{id}/assign` - 分配工单

#### 财务管理 (`/api/admin/finance`)
- `GET /api/admin/finance/overview` - 财务概览
- `GET /api/admin/finance/revenue/list` - 收入列表
- `GET /api/admin/finance/statistics` - 财务统计

#### 监控管理 (`/api/admin/monitor`)
- `GET /api/admin/monitor/dashboard` - 监控面板
- `GET /api/admin/monitor/piles` - 充电桩状态列表
- `GET /api/admin/monitor/pile/{id}` - 充电桩状态
- `POST /api/admin/monitor/pile/{id}/restart` - 重启充电桩
- `POST /api/admin/monitor/pile/{id}/config` - 配置充电桩

#### 数据分析 (`/api/admin/analysis`)
- `GET /api/admin/analysis/user-growth` - 用户增长分析
- `GET /api/admin/analysis/charging-frequency` - 充电频率分析
- `GET /api/admin/analysis/peak-hours` - 高峰时段分析
- `GET /api/admin/analysis/device-utilization` - 设备利用率分析

## 响应格式

所有API接口统一返回格式：

```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

### 状态码说明
- `200` - 成功
- `400` - 请求参数错误
- `401` - 未授权/Token过期
- `403` - 无权限
- `404` - 资源不存在
- `500` - 服务器错误

## 分页响应格式

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [],
    "total": 100,
    "current": 1,
    "size": 10,
    "pages": 10
  }
}
```

## 注意事项

1. 所有需要认证的接口都需要在请求头中携带Token
2. 文件上传接口使用 `multipart/form-data` 格式
3. 时间格式统一使用 `yyyy-MM-dd HH:mm:ss`
4. 金额统一使用 `DECIMAL(10, 2)` 格式
5. 分页参数：`current`（当前页，从1开始），`size`（每页数量）

