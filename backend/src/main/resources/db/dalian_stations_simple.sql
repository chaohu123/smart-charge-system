-- 大连市充电站数据插入脚本（简化版本）
-- 说明：假设数据库中已有5个北京充电站（ID 1-5），本脚本插入10个大连充电站（ID 6-15）
-- 如果您的数据库ID不同，请先查询最大ID，然后修改下面的station_id

USE smart_charge;

-- 插入大连市充电站（10个）
INSERT INTO charge_station (name, address, longitude, latitude, business_hours, service_phone, total_piles, available_piles, service_fee, status, manager_id) VALUES 
('星海广场充电站', '辽宁省大连市沙河口区星海广场', 121.586667, 38.886667, '00:00-24:00', '0411-12345678', 8, 6, 0.55, 0, 2),
('中山广场充电站', '辽宁省大连市中山区中山广场', 121.614167, 38.920833, '06:00-24:00', '0411-87654321', 12, 10, 0.60, 0, 2),
('大连火车站充电站', '辽宁省大连市中山区长江路260号', 121.625833, 38.925000, '00:00-24:00', '0411-11111111', 15, 12, 0.50, 0, 3),
('大连理工大学充电站', '辽宁省大连市甘井子区凌工路2号', 121.520833, 38.883333, '00:00-24:00', '0411-22222222', 10, 8, 0.45, 0, 3),
('大连国际机场充电站', '辽宁省大连市甘井子区迎客路100号', 121.541667, 38.966667, '00:00-24:00', '0411-33333333', 20, 18, 0.65, 0, 2),
('大连港充电站', '辽宁省大连市中山区港湾街1号', 121.658333, 38.920833, '06:00-22:00', '0411-44444444', 6, 5, 0.50, 0, 2),
('大连北站充电站', '辽宁省大连市甘井子区南关岭街道', 121.608333, 39.016667, '00:00-24:00', '0411-55555555', 12, 10, 0.55, 0, 3),
('大连森林动物园充电站', '辽宁省大连市西岗区滨海西路79号', 121.575000, 38.891667, '08:00-18:00', '0411-66666666', 8, 7, 0.48, 0, 2),
('大连万达广场充电站', '辽宁省大连市沙河口区西安路107号', 121.595833, 38.908333, '09:00-22:00', '0411-77777777', 10, 8, 0.58, 0, 3),
('大连软件园充电站', '辽宁省大连市甘井子区软件园路18号', 121.533333, 38.850000, '00:00-24:00', '0411-88888888', 14, 12, 0.52, 0, 2);

-- 注意：下面的station_id需要根据实际插入后的ID调整
-- 方法1：如果已有5个北京充电站，则大连充电站ID从6开始
-- 方法2：执行完上面的INSERT后，查询：SELECT MAX(id) FROM charge_station; 然后从MAX(id)-9开始

-- 插入充电桩
-- 星海广场充电站 (假设ID=6)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES 
(6, 'DL001', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL001_QR_CODE', 0.85),
(6, 'DL002', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL002_QR_CODE', 1.25),
(6, 'DL003', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL003_QR_CODE', 1.05),
(6, 'DL004', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL004_QR_CODE', 0.85),
(6, 'DL005', 'Tesla Supercharger', 120.0, 1, 'GB/T', 1, 'DL005_QR_CODE', 1.25),
(6, 'DL006', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL006_QR_CODE', 1.05),
(6, 'DL007', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL007_QR_CODE', 0.85),
(6, 'DL008', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL008_QR_CODE', 1.25);

-- 中山广场充电站 (假设ID=7)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES 
(7, 'DL009', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL009_QR_CODE', 0.90),
(7, 'DL010', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL010_QR_CODE', 1.30),
(7, 'DL011', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL011_QR_CODE', 1.10),
(7, 'DL012', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL012_QR_CODE', 0.90),
(7, 'DL013', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL013_QR_CODE', 1.30),
(7, 'DL014', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL014_QR_CODE', 1.10),
(7, 'DL015', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL015_QR_CODE', 0.90),
(7, 'DL016', 'Tesla Supercharger', 120.0, 1, 'GB/T', 1, 'DL016_QR_CODE', 1.30),
(7, 'DL017', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL017_QR_CODE', 1.10),
(7, 'DL018', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL018_QR_CODE', 0.90),
(7, 'DL019', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL019_QR_CODE', 1.30),
(7, 'DL020', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL020_QR_CODE', 1.10);

-- 大连火车站充电站 (假设ID=8)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES 
(8, 'DL021', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL021_QR_CODE', 0.80),
(8, 'DL022', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL022_QR_CODE', 1.20),
(8, 'DL023', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL023_QR_CODE', 1.00),
(8, 'DL024', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL024_QR_CODE', 0.80),
(8, 'DL025', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL025_QR_CODE', 1.20),
(8, 'DL026', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL026_QR_CODE', 1.00),
(8, 'DL027', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL027_QR_CODE', 0.80),
(8, 'DL028', 'Tesla Supercharger', 120.0, 1, 'GB/T', 1, 'DL028_QR_CODE', 1.20),
(8, 'DL029', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL029_QR_CODE', 1.00),
(8, 'DL030', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL030_QR_CODE', 0.80),
(8, 'DL031', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL031_QR_CODE', 1.20),
(8, 'DL032', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL032_QR_CODE', 1.00),
(8, 'DL033', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL033_QR_CODE', 0.80),
(8, 'DL034', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL034_QR_CODE', 1.20),
(8, 'DL035', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL035_QR_CODE', 1.00);

-- 大连理工大学充电站 (假设ID=9)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES 
(9, 'DL036', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL036_QR_CODE', 0.75),
(9, 'DL037', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL037_QR_CODE', 1.15),
(9, 'DL038', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL038_QR_CODE', 0.95),
(9, 'DL039', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL039_QR_CODE', 0.75),
(9, 'DL040', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL040_QR_CODE', 1.15),
(9, 'DL041', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL041_QR_CODE', 0.95),
(9, 'DL042', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL042_QR_CODE', 0.75),
(9, 'DL043', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL043_QR_CODE', 1.15),
(9, 'DL044', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL044_QR_CODE', 0.95),
(9, 'DL045', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL045_QR_CODE', 0.75);

-- 大连国际机场充电站 (假设ID=10)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES 
(10, 'DL046', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL046_QR_CODE', 0.95),
(10, 'DL047', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL047_QR_CODE', 1.35),
(10, 'DL048', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL048_QR_CODE', 1.15),
(10, 'DL049', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL049_QR_CODE', 0.95),
(10, 'DL050', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL050_QR_CODE', 1.35),
(10, 'DL051', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL051_QR_CODE', 1.15),
(10, 'DL052', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL052_QR_CODE', 0.95),
(10, 'DL053', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL053_QR_CODE', 1.35),
(10, 'DL054', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL054_QR_CODE', 1.15),
(10, 'DL055', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL055_QR_CODE', 0.95),
(10, 'DL056', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL056_QR_CODE', 1.35),
(10, 'DL057', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL057_QR_CODE', 1.15),
(10, 'DL058', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL058_QR_CODE', 0.95),
(10, 'DL059', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL059_QR_CODE', 1.35),
(10, 'DL060', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL060_QR_CODE', 1.15),
(10, 'DL061', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL061_QR_CODE', 0.95),
(10, 'DL062', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL062_QR_CODE', 1.35),
(10, 'DL063', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL063_QR_CODE', 1.15),
(10, 'DL064', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL064_QR_CODE', 0.95),
(10, 'DL065', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL065_QR_CODE', 1.35);

-- 大连港充电站 (假设ID=11)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES 
(11, 'DL066', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL066_QR_CODE', 0.80),
(11, 'DL067', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL067_QR_CODE', 1.20),
(11, 'DL068', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL068_QR_CODE', 1.00),
(11, 'DL069', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL069_QR_CODE', 0.80),
(11, 'DL070', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL070_QR_CODE', 1.20),
(11, 'DL071', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL071_QR_CODE', 1.00);

-- 大连北站充电站 (假设ID=12)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES 
(12, 'DL072', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL072_QR_CODE', 0.85),
(12, 'DL073', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL073_QR_CODE', 1.25),
(12, 'DL074', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL074_QR_CODE', 1.05),
(12, 'DL075', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL075_QR_CODE', 0.85),
(12, 'DL076', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL076_QR_CODE', 1.25),
(12, 'DL077', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL077_QR_CODE', 1.05),
(12, 'DL078', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL078_QR_CODE', 0.85),
(12, 'DL079', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL079_QR_CODE', 1.25),
(12, 'DL080', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL080_QR_CODE', 1.05),
(12, 'DL081', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL081_QR_CODE', 0.85),
(12, 'DL082', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL082_QR_CODE', 1.25),
(12, 'DL083', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL083_QR_CODE', 1.05);

-- 大连森林动物园充电站 (假设ID=13)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES 
(13, 'DL084', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL084_QR_CODE', 0.78),
(13, 'DL085', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL085_QR_CODE', 1.18),
(13, 'DL086', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL086_QR_CODE', 0.98),
(13, 'DL087', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL087_QR_CODE', 0.78),
(13, 'DL088', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL088_QR_CODE', 1.18),
(13, 'DL089', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL089_QR_CODE', 0.98),
(13, 'DL090', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL090_QR_CODE', 0.78),
(13, 'DL091', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL091_QR_CODE', 1.18);

-- 大连万达广场充电站 (假设ID=14)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES 
(14, 'DL092', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL092_QR_CODE', 0.88),
(14, 'DL093', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL093_QR_CODE', 1.28),
(14, 'DL094', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL094_QR_CODE', 1.08),
(14, 'DL095', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL095_QR_CODE', 0.88),
(14, 'DL096', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL096_QR_CODE', 1.28),
(14, 'DL097', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL097_QR_CODE', 1.08),
(14, 'DL098', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL098_QR_CODE', 0.88),
(14, 'DL099', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL099_QR_CODE', 1.28),
(14, 'DL100', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL100_QR_CODE', 1.08),
(14, 'DL101', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL101_QR_CODE', 0.88);

-- 大连软件园充电站 (假设ID=15)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES 
(15, 'DL102', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL102_QR_CODE', 0.82),
(15, 'DL103', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL103_QR_CODE', 1.22),
(15, 'DL104', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL104_QR_CODE', 1.02),
(15, 'DL105', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL105_QR_CODE', 0.82),
(15, 'DL106', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL106_QR_CODE', 1.22),
(15, 'DL107', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL107_QR_CODE', 1.02),
(15, 'DL108', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL108_QR_CODE', 0.82),
(15, 'DL109', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL109_QR_CODE', 1.22),
(15, 'DL110', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL110_QR_CODE', 1.02),
(15, 'DL111', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL111_QR_CODE', 0.82),
(15, 'DL112', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL112_QR_CODE', 1.22),
(15, 'DL113', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL113_QR_CODE', 1.02),
(15, 'DL114', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL114_QR_CODE', 0.82),
(15, 'DL115', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL115_QR_CODE', 1.22);

