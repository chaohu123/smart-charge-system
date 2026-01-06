# 智能充电桩共享查询与预约管理系统

## 项目简介

这是一个为电动汽车用户提供便捷的充电桩查询、实时状态查看、在线预约和智能导航服务的完整系统，同时为运营商提供完善的桩站管理、订单监控和数据分析功能。

## 技术栈

### 后端
- Spring Boot 2.7.14
- MyBatis Plus 3.5.3.1
- MySQL 8.0
- Redis
- JWT 认证
- Maven

### 前端（用户端）
- Vue 3.3.4
- Vue Router 4.2.5
- Pinia 2.1.7
- Element Plus 2.4.4
- Vite 5.0.8
- 高德地图 API

### 前端（管理后台）
- Vue 3.3.4
- Vue Router 4.2.5
- Pinia 2.1.7
- Element Plus 2.4.4
- ECharts 5.4.3
- Vite 5.0.8

## 项目结构

```
smart-charge-system/
├── backend/                    # 后端SpringBoot项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/smartcharge/
│   │   │   │   ├── common/     # 通用类（Result、PageResult等）
│   │   │   │   ├── config/     # 配置类（Cors、JWT等）
│   │   │   │   ├── controller/ # 控制器
│   │   │   │   ├── entity/     # 实体类
│   │   │   │   ├── mapper/     # 数据访问层
│   │   │   │   ├── service/    # 服务层
│   │   │   │   └── SmartChargeApplication.java
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── pom.xml
├── frontend-user/              # 前端用户端
│   ├── src/
│   │   ├── api/                # API接口
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # Pinia状态管理
│   │   ├── views/              # 页面组件
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
├── frontend-admin/             # 前端管理后台
│   ├── src/
│   │   ├── api/                # API接口
│   │   ├── layouts/            # 布局组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # Pinia状态管理
│   │   ├── views/              # 页面组件
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
├── database/                  # 数据库脚本
│   ├── init.sql               # 初始化SQL
│   └── README.md
└── README.md
```

## 核心功能

### 用户端功能
1. **用户系统**
   - 手机号注册登录
   - 实名认证
   - 车辆信息绑定
   - 电子钱包管理

2. **充电桩查询与导航**
   - 地图可视化查询
   - 筛选条件（快充/慢充、空闲/占用、价格、距离）
   - 实时状态显示
   - 智能推荐

3. **预约与使用**
   - 实时预约
   - 扫码充电
   - 充电过程监控
   - 远程停止充电

4. **订单与支付**
   - 订单管理
   - 多方式支付（余额/微信/支付宝）
   - 电子发票

5. **社交与评价**
   - 充电站评价
   - 实时路况分享
   - 收藏功能

### 管理后台功能
1. **充电站管理**
   - 站点信息管理
   - 充电桩设备管理
   - 价格策略管理

2. **订单与财务管理**
   - 订单实时监控
   - 财务统计报表
   - 退款管理

3. **用户管理**
   - 用户信息管理
   - 用户行为分析
   - 信用分调整

4. **设备监控与运维**
   - 实时状态大屏
   - 故障报警系统
   - 运维工单

5. **数据分析与报表**
   - 运营数据分析
   - 设备利用率分析
   - 财务报表

## 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+

### 后端启动

1. 创建数据库并执行初始化脚本
```bash
mysql -u root -p < database/init.sql
```

2. 修改配置文件 `backend/src/main/resources/application.yml`
   - 配置数据库连接
   - 配置Redis连接
   - 配置JWT密钥
   - 配置地图API密钥

3. 启动后端服务
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务默认运行在：http://localhost:8080

### 前端用户端启动

```bash
cd frontend-user
npm install
npm run dev
```

用户端默认运行在：http://localhost:3000

### 前端管理后台启动

```bash
cd frontend-admin
npm install
npm run dev
```

管理后台默认运行在：http://localhost:3001

## 默认账号

### 管理员账号
- 用户名：admin
- 密码：admin123

## API接口文档

### 用户端接口
- `/api/user/*` - 用户相关接口
- `/api/station/*` - 充电站相关接口
- `/api/reservation/*` - 预约相关接口
- `/api/order/*` - 订单相关接口
- `/api/payment/*` - 支付相关接口
- `/api/evaluation/*` - 评价相关接口

### 管理后台接口
- `/api/admin/*` - 管理员相关接口
- `/api/admin/station/*` - 充电站管理接口
- `/api/admin/pile/*` - 充电桩管理接口
- `/api/admin/order/*` - 订单管理接口
- `/api/admin/user/*` - 用户管理接口

## 开发说明

### 后端开发
- 使用MyBatis Plus进行数据库操作
- 统一使用Result类返回结果
- JWT Token认证，请求头格式：`Authorization: Bearer {token}`

### 前端开发
- 使用Pinia进行状态管理
- 使用Element Plus组件库
- API请求统一使用axios，已配置请求拦截器

## 注意事项

1. 需要申请高德地图API密钥并配置
2. 需要配置微信支付和支付宝支付参数
3. 生产环境需要修改JWT密钥
4. 文件上传路径需要根据实际情况配置

## 许可证

MIT License

