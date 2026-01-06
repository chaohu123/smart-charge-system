-- 智能充电桩管理系统数据库初始化脚本

CREATE DATABASE IF NOT EXISTS smart_charge DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE smart_charge;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone VARCHAR(11) NOT NULL UNIQUE COMMENT '手机号',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像',
    credit_score INT DEFAULT 100 COMMENT '信用分',
    balance DECIMAL(10, 2) DEFAULT 0.00 COMMENT '电子钱包余额',
    status TINYINT DEFAULT 0 COMMENT '状态：0-正常 1-禁用',
    is_real_name TINYINT DEFAULT 0 COMMENT '是否实名：0-否 1-是',
    real_name VARCHAR(50) COMMENT '真实姓名',
    id_card VARCHAR(18) COMMENT '身份证号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 车辆信息表
CREATE TABLE IF NOT EXISTS user_vehicle (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    plate_number VARCHAR(20) NOT NULL COMMENT '车牌号',
    brand VARCHAR(50) COMMENT '品牌',
    model VARCHAR(50) COMMENT '型号',
    battery_capacity DOUBLE COMMENT '电池容量(kWh)',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认：0-否 1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆信息表';

-- 充电站表
CREATE TABLE IF NOT EXISTS charge_station (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '充电站名称',
    address VARCHAR(255) NOT NULL COMMENT '地址',
    longitude DECIMAL(10, 6) NOT NULL COMMENT '经度',
    latitude DECIMAL(10, 6) NOT NULL COMMENT '纬度',
    images TEXT COMMENT '图片，逗号分隔',
    business_hours VARCHAR(50) COMMENT '营业时间',
    service_phone VARCHAR(20) COMMENT '服务电话',
    total_piles INT DEFAULT 0 COMMENT '总桩数',
    available_piles INT DEFAULT 0 COMMENT '可用桩数',
    service_fee DECIMAL(10, 2) DEFAULT 0.00 COMMENT '服务费(元/kWh)',
    status TINYINT DEFAULT 0 COMMENT '状态：0-正常 1-维护中',
    manager_id BIGINT COMMENT '管理员ID',
    free_parking TINYINT DEFAULT 0 COMMENT '免费停车：0-否 1-是',
    has_lounge TINYINT DEFAULT 0 COMMENT '带休息室：0-否 1-是',
    is_24_hours TINYINT DEFAULT 0 COMMENT '24小时营业：0-否 1-是',
    reservable TINYINT DEFAULT 1 COMMENT '可预约：0-否 1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_location (longitude, latitude),
    INDEX idx_status (status),
    INDEX idx_free_parking (free_parking),
    INDEX idx_has_lounge (has_lounge),
    INDEX idx_is_24_hours (is_24_hours)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电站表';

-- 充电桩表
CREATE TABLE IF NOT EXISTS charge_pile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    station_id BIGINT NOT NULL COMMENT '充电站ID',
    pile_number VARCHAR(50) NOT NULL COMMENT '桩号',
    model VARCHAR(50) COMMENT '型号',
    power DECIMAL(10, 2) NOT NULL COMMENT '功率(kW)',
    type TINYINT DEFAULT 0 COMMENT '类型：0-慢充 1-快充',
    protocol VARCHAR(50) COMMENT '协议类型',
    status TINYINT DEFAULT 0 COMMENT '状态：0-空闲 1-占用 2-故障 3-离线',
    qr_code VARCHAR(255) COMMENT '二维码',
    price DECIMAL(10, 2) DEFAULT 0.00 COMMENT '电价(元/kWh)',
    last_heartbeat DATETIME COMMENT '最后心跳时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_station_id (station_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电桩表';

-- 充电订单表
CREATE TABLE IF NOT EXISTS charge_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    station_id BIGINT NOT NULL COMMENT '充电站ID',
    pile_id BIGINT NOT NULL COMMENT '充电桩ID',
    type TINYINT DEFAULT 0 COMMENT '类型：0-即时充电 1-预约充电',
    start_time DATETIME COMMENT '预约开始时间',
    actual_start_time DATETIME COMMENT '实际开始时间',
    end_time DATETIME COMMENT '结束时间',
    power DECIMAL(10, 2) DEFAULT 0.00 COMMENT '充电量(kWh)',
    price DECIMAL(10, 2) DEFAULT 0.00 COMMENT '单价',
    service_fee DECIMAL(10, 2) DEFAULT 0.00 COMMENT '服务费',
    total_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '总金额',
    discount_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '优惠金额',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待支付 1-进行中 2-已完成 3-已取消 4-已退款',
    payment_method TINYINT COMMENT '支付方式：0-余额 1-微信 2-支付宝',
    pay_time DATETIME COMMENT '支付时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_station_id (station_id),
    INDEX idx_pile_id (pile_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电订单表';

-- 预约表
CREATE TABLE IF NOT EXISTS charge_reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_no VARCHAR(50) NOT NULL UNIQUE COMMENT '预约号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    station_id BIGINT NOT NULL COMMENT '充电站ID',
    pile_id BIGINT NOT NULL COMMENT '充电桩ID',
    start_time DATETIME NOT NULL COMMENT '预约开始时间',
    end_time DATETIME NOT NULL COMMENT '预约结束时间',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待使用 1-已使用 2-已取消 3-已过期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_pile_id (pile_id),
    INDEX idx_start_time (start_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- 评价表
CREATE TABLE IF NOT EXISTS station_evaluation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    station_id BIGINT NOT NULL COMMENT '充电站ID',
    order_id BIGINT COMMENT '订单ID',
    score TINYINT NOT NULL COMMENT '评分：1-5星',
    content TEXT COMMENT '评价内容',
    images TEXT COMMENT '评价图片，逗号分隔',
    environment_score TINYINT COMMENT '环境评分',
    service_score TINYINT COMMENT '服务评分',
    equipment_score TINYINT COMMENT '设备评分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_station_id (station_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 管理员表
CREATE TABLE IF NOT EXISTS sys_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    role TINYINT DEFAULT 0 COMMENT '角色：0-系统管理员 1-充电站管理员 2-运维人员',
    status TINYINT DEFAULT 0 COMMENT '状态：0-正常 1-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 支付记录表
CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT COMMENT '订单ID',
    type TINYINT NOT NULL COMMENT '类型：1-充值 2-消费 3-退款 4-提现',
    amount DECIMAL(10, 2) NOT NULL COMMENT '金额',
    payment_method TINYINT NOT NULL COMMENT '支付方式：0-余额 1-微信 2-支付宝',
    transaction_no VARCHAR(100) COMMENT '交易号',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待支付 1-已支付 2-已退款',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

-- 提现记录表
CREATE TABLE IF NOT EXISTS withdraw_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    amount DECIMAL(10, 2) NOT NULL COMMENT '提现金额',
    withdraw_no VARCHAR(50) NOT NULL UNIQUE COMMENT '提现单号',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-审核通过 2-审核拒绝',
    bank_name VARCHAR(100) COMMENT '银行名称',
    bank_account VARCHAR(100) COMMENT '银行账号',
    remark VARCHAR(500) COMMENT '备注',
    admin_id BIGINT COMMENT '审核管理员ID',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_withdraw_no (withdraw_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提现记录表';

-- 运维工单表
CREATE TABLE IF NOT EXISTS maintenance_ticket (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_no VARCHAR(50) NOT NULL UNIQUE COMMENT '工单号',
    station_id BIGINT NOT NULL COMMENT '充电站ID',
    pile_id BIGINT COMMENT '充电桩ID',
    fault_type VARCHAR(50) COMMENT '故障类型',
    description TEXT COMMENT '故障描述',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待处理 1-处理中 2-已完成',
    assignee_id BIGINT COMMENT '处理人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_station_id (station_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运维工单表';

-- 故障报警表
CREATE TABLE IF NOT EXISTS fault_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    alert_no VARCHAR(50) NOT NULL UNIQUE COMMENT '报警编号',
    station_id BIGINT NOT NULL COMMENT '充电站ID',
    pile_id BIGINT NOT NULL COMMENT '充电桩ID',
    alert_type TINYINT NOT NULL COMMENT '报警类型：0-设备离线 1-异常电流 2-异常电压 3-其他异常',
    alert_level TINYINT DEFAULT 1 COMMENT '报警级别：1-一般 2-重要 3-紧急',
    current_value DECIMAL(10, 2) COMMENT '当前电流值(A)',
    voltage_value DECIMAL(10, 2) COMMENT '当前电压值(V)',
    threshold_value DECIMAL(10, 2) COMMENT '阈值',
    description TEXT COMMENT '报警描述',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未处理 1-处理中 2-已处理 3-已忽略',
    handler_id BIGINT COMMENT '处理人ID',
    handle_time DATETIME COMMENT '处理时间',
    handle_remark TEXT COMMENT '处理备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_station_id (station_id),
    INDEX idx_pile_id (pile_id),
    INDEX idx_alert_type (alert_type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='故障报警表';

-- 维护记录表
CREATE TABLE IF NOT EXISTS maintenance_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_no VARCHAR(50) NOT NULL UNIQUE COMMENT '记录编号',
    ticket_id BIGINT COMMENT '关联工单ID',
    station_id BIGINT NOT NULL COMMENT '充电站ID',
    pile_id BIGINT COMMENT '充电桩ID',
    maintenance_type VARCHAR(50) COMMENT '维护类型：日常维护/故障维修/定期检查',
    maintenance_content TEXT COMMENT '维护内容',
    maintenance_result TEXT COMMENT '维护结果',
    maintainer_id BIGINT NOT NULL COMMENT '维护人ID',
    maintenance_time DATETIME NOT NULL COMMENT '维护时间',
    next_maintenance_time DATETIME COMMENT '下次维护时间',
    cost DECIMAL(10, 2) COMMENT '维护费用',
    images TEXT COMMENT '维护图片，逗号分隔',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_ticket_id (ticket_id),
    INDEX idx_station_id (station_id),
    INDEX idx_pile_id (pile_id),
    INDEX idx_maintenance_time (maintenance_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维护记录表';

-- 用户收藏表
CREATE TABLE IF NOT EXISTS user_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    station_id BIGINT NOT NULL COMMENT '充电站ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_station_id (station_id),
    UNIQUE KEY uk_user_station (user_id, station_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- 发票表
CREATE TABLE IF NOT EXISTS invoice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    invoice_no VARCHAR(50) NOT NULL UNIQUE COMMENT '发票号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    type VARCHAR(20) NOT NULL COMMENT '发票类型：个人/企业',
    title VARCHAR(100) NOT NULL COMMENT '发票抬头',
    tax_number VARCHAR(50) COMMENT '税号',
    amount DECIMAL(10, 2) NOT NULL COMMENT '发票金额',
    content VARCHAR(200) COMMENT '发票内容',
    status VARCHAR(20) DEFAULT '待开票' COMMENT '状态：待开票/已开票/已作废',
    file_url VARCHAR(255) COMMENT '发票文件URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票表';

-- 故障上报表
CREATE TABLE IF NOT EXISTS fault_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_no VARCHAR(50) NOT NULL UNIQUE COMMENT '报修号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    station_id BIGINT NOT NULL COMMENT '充电站ID',
    pile_id BIGINT COMMENT '充电桩ID',
    fault_type VARCHAR(50) NOT NULL COMMENT '故障类型',
    description TEXT COMMENT '故障描述',
    images TEXT COMMENT '故障图片，逗号分隔',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待处理 1-处理中 2-已处理',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_station_id (station_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='故障上报表';

-- 优惠券表
CREATE TABLE IF NOT EXISTS coupon (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    type VARCHAR(20) NOT NULL COMMENT '类型：满减/折扣',
    amount DECIMAL(10, 2) NOT NULL COMMENT '优惠金额或折扣率',
    min_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '最低使用金额',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    total_count INT DEFAULT 0 COMMENT '总数量',
    used_count INT DEFAULT 0 COMMENT '已使用数量',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未发布 1-已发布 2-已结束',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS user_coupon (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coupon_id BIGINT NOT NULL COMMENT '优惠券ID',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未使用 1-已使用 2-已过期',
    get_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    use_time DATETIME COMMENT '使用时间',
    order_id BIGINT COMMENT '使用的订单ID',
    INDEX idx_user_id (user_id),
    INDEX idx_coupon_id (coupon_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 积分记录表
CREATE TABLE IF NOT EXISTS points_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    points INT NOT NULL COMMENT '积分变化（正数为增加，负数为减少）',
    type VARCHAR(50) NOT NULL COMMENT '类型：消费获得/签到/活动/使用抵扣',
    description VARCHAR(200) COMMENT '描述',
    order_id BIGINT COMMENT '关联订单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';

-- 消息通知表
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL DEFAULT 0 COMMENT '用户ID，0表示系统通知',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    type VARCHAR(50) COMMENT '类型：订单/系统/活动/优惠券',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 活动表
CREATE TABLE IF NOT EXISTS activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL COMMENT '活动标题',
    description TEXT COMMENT '活动描述',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未发布 1-进行中 2-已结束',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运营活动表';

-- 管理员通知表
CREATE TABLE IF NOT EXISTS admin_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NOT NULL COMMENT '管理员ID',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    type VARCHAR(50) COMMENT '类型：故障报警/系统/工单',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin_id (admin_id),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员通知表';

-- 信用分记录表
CREATE TABLE IF NOT EXISTS credit_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    points INT NOT NULL COMMENT '信用分变化（正数为增加，负数为减少）',
    type VARCHAR(50) NOT NULL COMMENT '类型：预约超时/评价/充值/违规等',
    reason VARCHAR(200) COMMENT '原因描述',
    order_id BIGINT COMMENT '关联订单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信用分记录表';

