-- 智能充电桩管理系统示例数据脚本

USE smart_charge;

-- 插入默认管理员账号（密码：admin123，MD5加密）
INSERT INTO sys_admin (username, password, real_name, role, status) VALUES 
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 0, 0),
('station01', 'e10adc3949ba59abbe56e057f20f883e', '充电站管理员1', 1, 0),
('station02', 'e10adc3949ba59abbe56e057f20f883e', '充电站管理员2', 1, 0);

-- 插入示例用户（密码：123456，MD5加密）
INSERT INTO sys_user (phone, password, nickname, credit_score, balance, status, is_real_name, real_name, id_card) VALUES 
('13800138000', 'e10adc3949ba59abbe56e057f20f883e', '张三', 100, 500.00, 0, 1, '张三', '110101199001011234'),
('13800138001', 'e10adc3949ba59abbe56e057f20f883e', '李四', 95, 200.00, 0, 1, '李四', '110101199002021234'),
('13800138002', 'e10adc3949ba59abbe56e057f20f883e', '王五', 100, 1000.00, 0, 0, NULL, NULL),
('13800138003', 'e10adc3949ba59abbe56e057f20f883e', '赵六', 90, 300.00, 0, 1, '赵六', '110101199004041234'),
('13800138004', 'e10adc3949ba59abbe56e057f20f883e', '钱七', 100, 800.00, 0, 0, NULL, NULL);

-- 插入车辆信息
INSERT INTO user_vehicle (user_id, plate_number, brand, model, battery_capacity, is_default) VALUES 
(1, '京A12345', '特斯拉', 'Model 3', 60.0, 1),
(1, '京B67890', '比亚迪', '汉EV', 76.9, 0),
(2, '京C11111', '蔚来', 'ES6', 70.0, 1),
(3, '京D22222', '小鹏', 'P7', 80.9, 1),
(4, '京E33333', '理想', 'ONE', 40.5, 1),
(5, '京F44444', '极氪', '001', 100.0, 1);

-- 插入充电站（北京地区）
INSERT INTO charge_station (name, address, longitude, latitude, business_hours, service_phone, total_piles, available_piles, service_fee, status, manager_id) VALUES 
('中关村充电站', '北京市海淀区中关村大街1号', 116.316833, 39.983424, '00:00-24:00', '010-12345678', 20, 15, 0.50, 0, 2),
('国贸充电站', '北京市朝阳区建国门外大街1号', 116.458333, 39.909167, '06:00-24:00', '010-87654321', 30, 25, 0.60, 0, 2),
('西单充电站', '北京市西城区西单北大街176号', 116.373333, 39.908333, '00:00-24:00', '010-11111111', 15, 12, 0.45, 0, 3),
('望京充电站', '北京市朝阳区望京街10号', 116.483333, 39.991667, '00:00-24:00', '010-22222222', 25, 20, 0.55, 0, 3),
('天安门充电站', '北京市东城区天安门广场', 116.397222, 39.903333, '06:00-22:00', '010-33333333', 10, 8, 0.40, 0, 2);

-- 插入充电桩
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES 
-- 中关村充电站
(1, 'ZG001', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'ZG001_QR_CODE', 0.80),
(1, 'ZG002', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'ZG002_QR_CODE', 0.80),
(1, 'ZG003', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'ZG003_QR_CODE', 1.20),
(1, 'ZG004', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'ZG004_QR_CODE', 1.20),
(1, 'ZG005', 'BYD DC Charger', 60.0, 1, 'GB/T', 1, 'ZG005_QR_CODE', 1.00),
(1, 'ZG006', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'ZG006_QR_CODE', 1.00),
(1, 'ZG007', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'ZG007_QR_CODE', 0.80),
(1, 'ZG008', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 2, 'ZG008_QR_CODE', 0.80),
(1, 'ZG009', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'ZG009_QR_CODE', 1.20),
(1, 'ZG010', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'ZG010_QR_CODE', 1.20),
-- 国贸充电站
(2, 'GM001', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'GM001_QR_CODE', 0.90),
(2, 'GM002', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'GM002_QR_CODE', 0.90),
(2, 'GM003', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'GM003_QR_CODE', 1.30),
(2, 'GM004', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'GM004_QR_CODE', 1.30),
(2, 'GM005', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'GM005_QR_CODE', 1.10),
(2, 'GM006', 'BYD DC Charger', 60.0, 1, 'GB/T', 1, 'GM006_QR_CODE', 1.10),
(2, 'GM007', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'GM007_QR_CODE', 0.90),
(2, 'GM008', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'GM008_QR_CODE', 0.90),
(2, 'GM009', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'GM009_QR_CODE', 1.30),
(2, 'GM010', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'GM010_QR_CODE', 1.30),
-- 西单充电站
(3, 'XD001', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'XD001_QR_CODE', 0.75),
(3, 'XD002', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'XD002_QR_CODE', 0.75),
(3, 'XD003', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'XD003_QR_CODE', 0.95),
(3, 'XD004', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'XD004_QR_CODE', 0.95),
(3, 'XD005', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'XD005_QR_CODE', 1.15),
-- 望京充电站
(4, 'WJ001', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'WJ001_QR_CODE', 0.85),
(4, 'WJ002', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'WJ002_QR_CODE', 0.85),
(4, 'WJ003', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'WJ003_QR_CODE', 1.05),
(4, 'WJ004', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'WJ004_QR_CODE', 1.05),
(4, 'WJ005', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'WJ005_QR_CODE', 1.25),
-- 天安门充电站
(5, 'TAM001', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'TAM001_QR_CODE', 0.70),
(5, 'TAM002', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'TAM002_QR_CODE', 0.70),
(5, 'TAM003', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'TAM003_QR_CODE', 0.90),
(5, 'TAM004', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'TAM004_QR_CODE', 0.90),
(5, 'TAM005', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'TAM005_QR_CODE', 1.10);

-- 插入优惠券
INSERT INTO coupon (name, type, amount, min_amount, start_time, end_time, total_count, used_count, status) VALUES 
('新用户专享', '满减', 10.00, 50.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 1000, 50, 1),
('满100减20', '满减', 20.00, 100.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 500, 30, 1),
('9折优惠', '折扣', 0.90, 30.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 800, 100, 1),
('周末特惠', '满减', 15.00, 80.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 300, 20, 1),
('会员专享', '折扣', 0.85, 50.00, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 200, 10, 1);

-- 插入用户优惠券
INSERT INTO user_coupon (user_id, coupon_id, status, get_time) VALUES 
(1, 1, 0, '2024-01-15 10:00:00'),
(1, 2, 1, '2024-01-15 10:00:00'),
(1, 3, 0, '2024-01-20 14:00:00'),
(2, 1, 0, '2024-01-16 11:00:00'),
(2, 3, 0, '2024-01-16 11:00:00'),
(3, 2, 0, '2024-01-17 12:00:00'),
(3, 4, 0, '2024-01-17 12:00:00'),
(4, 1, 0, '2024-01-18 13:00:00'),
(4, 5, 0, '2024-01-18 13:00:00'),
(5, 3, 0, '2024-01-19 15:00:00');

-- 插入示例订单
INSERT INTO charge_order (order_no, user_id, station_id, pile_id, type, actual_start_time, end_time, power, price, service_fee, total_amount, discount_amount, status, payment_method, pay_time) VALUES 
('ORD20240115001', 1, 1, 3, 0, '2024-01-15 10:00:00', '2024-01-15 10:45:00', 45.0, 1.20, 0.50, 76.50, 10.00, 2, 0, '2024-01-15 10:45:00'),
('ORD20240116001', 2, 2, 13, 0, '2024-01-16 14:00:00', '2024-01-16 14:30:00', 30.0, 1.30, 0.60, 57.00, 0.00, 2, 1, '2024-01-16 14:30:00'),
('ORD20240117001', 3, 3, 23, 0, '2024-01-17 09:00:00', '2024-01-17 09:20:00', 20.0, 0.95, 0.45, 28.50, 0.00, 2, 2, '2024-01-17 09:20:00'),
('ORD20240118001', 1, 4, 28, 0, '2024-01-18 16:00:00', '2024-01-18 16:50:00', 50.0, 1.05, 0.55, 80.00, 15.00, 2, 0, '2024-01-18 16:50:00'),
('ORD20240119001', 4, 5, 33, 0, '2024-01-19 11:00:00', '2024-01-19 11:25:00', 25.0, 0.90, 0.40, 32.50, 0.00, 2, 0, '2024-01-19 11:25:00');

-- 插入评价
INSERT INTO station_evaluation (user_id, station_id, order_id, score, content, environment_score, service_score, equipment_score) VALUES 
(1, 1, 1, 5, '充电速度很快，环境也很好，服务态度不错！', 5, 5, 5),
(2, 2, 2, 4, '位置方便，充电桩数量充足，就是价格稍微贵一点。', 4, 4, 5),
(3, 3, 3, 5, '非常满意，充电速度快，停车方便。', 5, 5, 5),
(1, 4, 4, 4, '整体不错，就是高峰期人比较多。', 4, 4, 4),
(4, 5, 5, 5, '天安门附近，位置绝佳，充电体验很好！', 5, 5, 5);

-- 插入积分记录
INSERT INTO points_record (user_id, points, type, description, order_id) VALUES 
(1, 76, '消费获得', '订单ORD20240115001消费获得积分', 1),
(1, 80, '消费获得', '订单ORD20240118001消费获得积分', 4),
(2, 57, '消费获得', '订单ORD20240116001消费获得积分', 2),
(3, 28, '消费获得', '订单ORD20240117001消费获得积分', 3),
(4, 32, '消费获得', '订单ORD20240119001消费获得积分', 5),
(1, 10, '签到', '每日签到获得积分', NULL),
(2, 10, '签到', '每日签到获得积分', NULL);

-- 插入支付记录
INSERT INTO payment_record (user_id, order_id, type, amount, payment_method, transaction_no, status) VALUES 
(1, 1, 2, 66.50, 0, 'PAY20240115001', 1),
(2, 2, 2, 57.00, 1, 'WX20240116001', 1),
(3, 3, 2, 28.50, 2, 'ALI20240117001', 1),
(1, 4, 2, 65.00, 0, 'PAY20240118001', 1),
(4, 5, 2, 32.50, 0, 'PAY20240119001', 1),
(1, NULL, 1, 500.00, 1, 'WX_RECHARGE_001', 1),
(2, NULL, 1, 200.00, 2, 'ALI_RECHARGE_001', 1),
(3, NULL, 1, 1000.00, 0, 'BALANCE_RECHARGE_001', 1);

-- 插入消息通知
INSERT INTO notification (user_id, title, content, type, is_read) VALUES 
(0, '系统维护通知', '系统将于今晚22:00-24:00进行维护，期间可能无法使用部分功能，敬请谅解。', '系统', 0),
(1, '订单完成', '您的订单ORD20240115001已完成，感谢使用！', '订单', 1),
(1, '优惠券到账', '您已获得新用户专享优惠券，满50减10元！', '优惠券', 0),
(2, '订单完成', '您的订单ORD20240116001已完成，感谢使用！', '订单', 1),
(3, '订单完成', '您的订单ORD20240117001已完成，感谢使用！', '订单', 1),
(1, '订单完成', '您的订单ORD20240118001已完成，感谢使用！', '订单', 1),
(4, '订单完成', '您的订单ORD20240119001已完成，感谢使用！', '订单', 1);

-- 插入信用分记录
INSERT INTO credit_record (user_id, points, type, reason, order_id) VALUES 
(1, 5, '评价', '完成订单评价获得信用分', 1),
(2, 5, '评价', '完成订单评价获得信用分', 2),
(3, 5, '评价', '完成订单评价获得信用分', 3),
(1, 5, '评价', '完成订单评价获得信用分', 4),
(4, 5, '评价', '完成订单评价获得信用分', 5),
(2, -5, '预约超时', '预约充电超时未使用', NULL),
(4, -5, '预约超时', '预约充电超时未使用', NULL);

-- 插入用户收藏
INSERT INTO user_favorite (user_id, station_id) VALUES 
(1, 1),
(1, 4),
(2, 2),
(3, 3),
(4, 5),
(5, 1);

-- 插入预约记录
INSERT INTO charge_reservation (reservation_no, user_id, station_id, pile_id, start_time, end_time, status) VALUES 
('RES20240120001', 1, 1, 3, '2024-01-20 10:00:00', '2024-01-20 11:00:00', 0),
('RES20240120002', 2, 2, 13, '2024-01-20 14:00:00', '2024-01-20 15:00:00', 0),
('RES20240120003', 3, 3, 23, '2024-01-21 09:00:00', '2024-01-21 10:00:00', 0);

-- 插入故障上报
INSERT INTO fault_report (report_no, user_id, station_id, pile_id, fault_type, description, status) VALUES 
('FAULT20240115001', 1, 1, 8, '设备故障', '充电桩无法启动，屏幕无显示', 2),
('FAULT20240116001', 2, 2, 16, '连接异常', '充电枪连接后无法充电', 1),
('FAULT20240117001', 3, 3, 25, '支付故障', '支付成功后订单状态未更新', 0);

-- 插入运维工单
INSERT INTO maintenance_ticket (ticket_no, station_id, pile_id, fault_type, description, status, assignee_id) VALUES 
('TICKET20240115001', 1, 8, '设备故障', '充电桩无法启动，需要更换主板', 2, 2),
('TICKET20240116001', 2, 16, '连接异常', '充电枪连接后无法充电，需要检查连接器', 1, 2),
('TICKET20240117001', 3, 25, '支付故障', '支付系统异常，需要检查接口', 0, 3);

-- 插入发票
INSERT INTO invoice (invoice_no, user_id, order_id, type, title, tax_number, amount, content, status) VALUES 
('INV20240115001', 1, 1, '个人', '张三', NULL, 66.50, '充电服务费', '已开票'),
('INV20240116001', 2, 2, '企业', '北京科技有限公司', '91110000123456789X', 57.00, '充电服务费', '已开票'),
('INV20240118001', 1, 4, '个人', '张三', NULL, 65.00, '充电服务费', '待开票');

