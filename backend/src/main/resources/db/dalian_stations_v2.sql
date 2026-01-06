-- 大连市充电站数据插入脚本（自动获取ID版本）
-- 插入10个充电站及其充电桩

USE smart_charge;

-- 插入大连市充电站
INSERT INTO charge_station (name, address, longitude, latitude, business_hours, service_phone, total_piles, available_piles, service_fee, status, manager_id) VALUES
-- 1. 星海广场充电站
('星海广场充电站', '辽宁省大连市沙河口区星海广场', 121.586667, 38.886667, '00:00-24:00', '0411-12345678', 8, 6, 0.55, 0, 2),
-- 2. 中山广场充电站
('中山广场充电站', '辽宁省大连市中山区中山广场', 121.614167, 38.920833, '06:00-24:00', '0411-87654321', 12, 10, 0.60, 0, 2),
-- 3. 大连火车站充电站
('大连火车站充电站', '辽宁省大连市中山区长江路260号', 121.625833, 38.925000, '00:00-24:00', '0411-11111111', 15, 12, 0.50, 0, 3),
-- 4. 大连理工大学充电站
('大连理工大学充电站', '辽宁省大连市甘井子区凌工路2号', 121.520833, 38.883333, '00:00-24:00', '0411-22222222', 10, 8, 0.45, 0, 3),
-- 5. 大连国际机场充电站
('大连国际机场充电站', '辽宁省大连市甘井子区迎客路100号', 121.541667, 38.966667, '00:00-24:00', '0411-33333333', 20, 18, 0.65, 0, 2),
-- 6. 大连港充电站
('大连港充电站', '辽宁省大连市中山区港湾街1号', 121.658333, 38.920833, '06:00-22:00', '0411-44444444', 6, 5, 0.50, 0, 2),
-- 7. 大连北站充电站
('大连北站充电站', '辽宁省大连市甘井子区南关岭街道', 121.608333, 39.016667, '00:00-24:00', '0411-55555555', 12, 10, 0.55, 0, 3),
-- 8. 大连森林动物园充电站
('大连森林动物园充电站', '辽宁省大连市西岗区滨海西路79号', 121.575000, 38.891667, '08:00-18:00', '0411-66666666', 8, 7, 0.48, 0, 2),
-- 9. 大连万达广场充电站
('大连万达广场充电站', '辽宁省大连市沙河口区西安路107号', 121.595833, 38.908333, '09:00-22:00', '0411-77777777', 10, 8, 0.58, 0, 3),
-- 10. 大连软件园充电站
('大连软件园充电站', '辽宁省大连市甘井子区软件园路18号', 121.533333, 38.850000, '00:00-24:00', '0411-88888888', 14, 12, 0.52, 0, 2);

-- 获取刚插入的充电站ID范围（假设从最后一个ID开始）
SET @first_station_id = (SELECT MAX(id) - 9 FROM charge_station);
SET @last_station_id = (SELECT MAX(id) FROM charge_station);

-- 插入充电桩
-- 星海广场充电站 (ID = @first_station_id)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES
(@first_station_id, 'DL001', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL001_QR_CODE', 0.85),
(@first_station_id, 'DL002', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL002_QR_CODE', 1.25),
(@first_station_id, 'DL003', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL003_QR_CODE', 1.05),
(@first_station_id, 'DL004', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL004_QR_CODE', 0.85),
(@first_station_id, 'DL005', 'Tesla Supercharger', 120.0, 1, 'GB/T', 1, 'DL005_QR_CODE', 1.25),
(@first_station_id, 'DL006', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL006_QR_CODE', 1.05),
(@first_station_id, 'DL007', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL007_QR_CODE', 0.85),
(@first_station_id, 'DL008', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL008_QR_CODE', 1.25);

-- 中山广场充电站 (ID = @first_station_id + 1)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES
(@first_station_id + 1, 'DL009', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL009_QR_CODE', 0.90),
(@first_station_id + 1, 'DL010', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL010_QR_CODE', 1.30),
(@first_station_id + 1, 'DL011', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL011_QR_CODE', 1.10),
(@first_station_id + 1, 'DL012', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL012_QR_CODE', 0.90),
(@first_station_id + 1, 'DL013', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL013_QR_CODE', 1.30),
(@first_station_id + 1, 'DL014', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL014_QR_CODE', 1.10),
(@first_station_id + 1, 'DL015', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL015_QR_CODE', 0.90),
(@first_station_id + 1, 'DL016', 'Tesla Supercharger', 120.0, 1, 'GB/T', 1, 'DL016_QR_CODE', 1.30),
(@first_station_id + 1, 'DL017', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL017_QR_CODE', 1.10),
(@first_station_id + 1, 'DL018', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL018_QR_CODE', 0.90),
(@first_station_id + 1, 'DL019', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL019_QR_CODE', 1.30),
(@first_station_id + 1, 'DL020', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL020_QR_CODE', 1.10);

-- 大连火车站充电站 (ID = @first_station_id + 2)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES
(@first_station_id + 2, 'DL021', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL021_QR_CODE', 0.80),
(@first_station_id + 2, 'DL022', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL022_QR_CODE', 1.20),
(@first_station_id + 2, 'DL023', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL023_QR_CODE', 1.00),
(@first_station_id + 2, 'DL024', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL024_QR_CODE', 0.80),
(@first_station_id + 2, 'DL025', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL025_QR_CODE', 1.20),
(@first_station_id + 2, 'DL026', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL026_QR_CODE', 1.00),
(@first_station_id + 2, 'DL027', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL027_QR_CODE', 0.80),
(@first_station_id + 2, 'DL028', 'Tesla Supercharger', 120.0, 1, 'GB/T', 1, 'DL028_QR_CODE', 1.20),
(@first_station_id + 2, 'DL029', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL029_QR_CODE', 1.00),
(@first_station_id + 2, 'DL030', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL030_QR_CODE', 0.80),
(@first_station_id + 2, 'DL031', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL031_QR_CODE', 1.20),
(@first_station_id + 2, 'DL032', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL032_QR_CODE', 1.00),
(@first_station_id + 2, 'DL033', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL033_QR_CODE', 0.80),
(@first_station_id + 2, 'DL034', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL034_QR_CODE', 1.20),
(@first_station_id + 2, 'DL035', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL035_QR_CODE', 1.00);

-- 大连理工大学充电站 (ID = @first_station_id + 3)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES
(@first_station_id + 3, 'DL036', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL036_QR_CODE', 0.75),
(@first_station_id + 3, 'DL037', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL037_QR_CODE', 1.15),
(@first_station_id + 3, 'DL038', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL038_QR_CODE', 0.95),
(@first_station_id + 3, 'DL039', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL039_QR_CODE', 0.75),
(@first_station_id + 3, 'DL040', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL040_QR_CODE', 1.15),
(@first_station_id + 3, 'DL041', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL041_QR_CODE', 0.95),
(@first_station_id + 3, 'DL042', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL042_QR_CODE', 0.75),
(@first_station_id + 3, 'DL043', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL043_QR_CODE', 1.15),
(@first_station_id + 3, 'DL044', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL044_QR_CODE', 0.95),
(@first_station_id + 3, 'DL045', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL045_QR_CODE', 0.75);

-- 大连国际机场充电站 (ID = @first_station_id + 4)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES
(@first_station_id + 4, 'DL046', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL046_QR_CODE', 0.95),
(@first_station_id + 4, 'DL047', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL047_QR_CODE', 1.35),
(@first_station_id + 4, 'DL048', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL048_QR_CODE', 1.15),
(@first_station_id + 4, 'DL049', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL049_QR_CODE', 0.95),
(@first_station_id + 4, 'DL050', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL050_QR_CODE', 1.35),
(@first_station_id + 4, 'DL051', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL051_QR_CODE', 1.15),
(@first_station_id + 4, 'DL052', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL052_QR_CODE', 0.95),
(@first_station_id + 4, 'DL053', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL053_QR_CODE', 1.35),
(@first_station_id + 4, 'DL054', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL054_QR_CODE', 1.15),
(@first_station_id + 4, 'DL055', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL055_QR_CODE', 0.95),
(@first_station_id + 4, 'DL056', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL056_QR_CODE', 1.35),
(@first_station_id + 4, 'DL057', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL057_QR_CODE', 1.15),
(@first_station_id + 4, 'DL058', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL058_QR_CODE', 0.95),
(@first_station_id + 4, 'DL059', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL059_QR_CODE', 1.35),
(@first_station_id + 4, 'DL060', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL060_QR_CODE', 1.15),
(@first_station_id + 4, 'DL061', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL061_QR_CODE', 0.95),
(@first_station_id + 4, 'DL062', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL062_QR_CODE', 1.35),
(@first_station_id + 4, 'DL063', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL063_QR_CODE', 1.15),
(@first_station_id + 4, 'DL064', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL064_QR_CODE', 0.95),
(@first_station_id + 4, 'DL065', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL065_QR_CODE', 1.35);

-- 大连港充电站 (ID = @first_station_id + 5)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES
(@first_station_id + 5, 'DL066', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL066_QR_CODE', 0.80),
(@first_station_id + 5, 'DL067', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL067_QR_CODE', 1.20),
(@first_station_id + 5, 'DL068', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL068_QR_CODE', 1.00),
(@first_station_id + 5, 'DL069', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL069_QR_CODE', 0.80),
(@first_station_id + 5, 'DL070', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL070_QR_CODE', 1.20),
(@first_station_id + 5, 'DL071', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL071_QR_CODE', 1.00);

-- 大连北站充电站 (ID = @first_station_id + 6)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES
(@first_station_id + 6, 'DL072', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL072_QR_CODE', 0.85),
(@first_station_id + 6, 'DL073', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL073_QR_CODE', 1.25),
(@first_station_id + 6, 'DL074', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL074_QR_CODE', 1.05),
(@first_station_id + 6, 'DL075', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL075_QR_CODE', 0.85),
(@first_station_id + 6, 'DL076', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL076_QR_CODE', 1.25),
(@first_station_id + 6, 'DL077', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL077_QR_CODE', 1.05),
(@first_station_id + 6, 'DL078', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL078_QR_CODE', 0.85),
(@first_station_id + 6, 'DL079', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL079_QR_CODE', 1.25),
(@first_station_id + 6, 'DL080', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL080_QR_CODE', 1.05),
(@first_station_id + 6, 'DL081', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL081_QR_CODE', 0.85),
(@first_station_id + 6, 'DL082', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL082_QR_CODE', 1.25),
(@first_station_id + 6, 'DL083', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL083_QR_CODE', 1.05);

-- 大连森林动物园充电站 (ID = @first_station_id + 7)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES
(@first_station_id + 7, 'DL084', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL084_QR_CODE', 0.78),
(@first_station_id + 7, 'DL085', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL085_QR_CODE', 1.18),
(@first_station_id + 7, 'DL086', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL086_QR_CODE', 0.98),
(@first_station_id + 7, 'DL087', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL087_QR_CODE', 0.78),
(@first_station_id + 7, 'DL088', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL088_QR_CODE', 1.18),
(@first_station_id + 7, 'DL089', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL089_QR_CODE', 0.98),
(@first_station_id + 7, 'DL090', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL090_QR_CODE', 0.78),
(@first_station_id + 7, 'DL091', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL091_QR_CODE', 1.18);

-- 大连万达广场充电站 (ID = @first_station_id + 8)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES
(@first_station_id + 8, 'DL092', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL092_QR_CODE', 0.88),
(@first_station_id + 8, 'DL093', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL093_QR_CODE', 1.28),
(@first_station_id + 8, 'DL094', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL094_QR_CODE', 1.08),
(@first_station_id + 8, 'DL095', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL095_QR_CODE', 0.88),
(@first_station_id + 8, 'DL096', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL096_QR_CODE', 1.28),
(@first_station_id + 8, 'DL097', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL097_QR_CODE', 1.08),
(@first_station_id + 8, 'DL098', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL098_QR_CODE', 0.88),
(@first_station_id + 8, 'DL099', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL099_QR_CODE', 1.28),
(@first_station_id + 8, 'DL100', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL100_QR_CODE', 1.08),
(@first_station_id + 8, 'DL101', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL101_QR_CODE', 0.88);

-- 大连软件园充电站 (ID = @first_station_id + 9)
INSERT INTO charge_pile (station_id, pile_number, model, power, type, protocol, status, qr_code, price) VALUES
(@first_station_id + 9, 'DL102', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL102_QR_CODE', 0.82),
(@first_station_id + 9, 'DL103', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL103_QR_CODE', 1.22),
(@first_station_id + 9, 'DL104', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL104_QR_CODE', 1.02),
(@first_station_id + 9, 'DL105', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL105_QR_CODE', 0.82),
(@first_station_id + 9, 'DL106', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL106_QR_CODE', 1.22),
(@first_station_id + 9, 'DL107', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL107_QR_CODE', 1.02),
(@first_station_id + 9, 'DL108', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL108_QR_CODE', 0.82),
(@first_station_id + 9, 'DL109', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL109_QR_CODE', 1.22),
(@first_station_id + 9, 'DL110', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL110_QR_CODE', 1.02),
(@first_station_id + 9, 'DL111', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL111_QR_CODE', 0.82),
(@first_station_id + 9, 'DL112', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL112_QR_CODE', 1.22),
(@first_station_id + 9, 'DL113', 'BYD DC Charger', 60.0, 1, 'GB/T', 0, 'DL113_QR_CODE', 1.02),
(@first_station_id + 9, 'DL114', 'ABB AC Wallbox', 7.0, 0, 'GB/T', 0, 'DL114_QR_CODE', 0.82),
(@first_station_id + 9, 'DL115', 'Tesla Supercharger', 120.0, 1, 'GB/T', 0, 'DL115_QR_CODE', 1.22);

SELECT id, phone, nickname FROM sys_user WHERE id = 1;
USE smart_charge;

-- 添加免费停车字段
ALTER TABLE charge_station
    ADD COLUMN free_parking TINYINT DEFAULT 0 COMMENT '免费停车：0-否 1-是' AFTER manager_id;

-- 添加带休息室字段
ALTER TABLE charge_station
    ADD COLUMN has_lounge TINYINT DEFAULT 0 COMMENT '带休息室：0-否 1-是' AFTER free_parking;

-- 添加24小时营业字段
ALTER TABLE charge_station
    ADD COLUMN is_24_hours TINYINT DEFAULT 0 COMMENT '24小时营业：0-否 1-是' AFTER has_lounge;

-- 添加可预约字段
ALTER TABLE charge_station
    ADD COLUMN reservable TINYINT DEFAULT 1 COMMENT '可预约：0-否 1-是' AFTER is_24_hours;

-- 添加索引
CREATE INDEX idx_free_parking ON charge_station(free_parking);
CREATE INDEX idx_has_lounge ON charge_station(has_lounge);
CREATE INDEX idx_is_24_hours ON charge_station(is_24_hours);

-- 数据库迁移脚本：添加故障报警和维护记录相关表
-- 执行时间：2024-01-06
-- 说明：为支持运维人员角色和故障报警功能，添加以下表

USE smart_charge;

-- 更新管理员表role字段注释（如果已存在则忽略）
ALTER TABLE sys_admin
    MODIFY COLUMN role TINYINT DEFAULT 0 COMMENT '角色：0-系统管理员 1-充电站管理员 2-运维人员';

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

-- 插入默认管理员账号（如果不存在）
INSERT INTO sys_admin (username, password, real_name, role)
SELECT 'admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 0
WHERE NOT EXISTS (SELECT 1 FROM sys_admin WHERE username = 'admin');

INSERT INTO sys_admin (username, password, real_name, role)
SELECT 'station_admin', 'e10adc3949ba59abbe56e057f20f883e', '充电站管理员', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_admin WHERE username = 'station_admin');

INSERT INTO sys_admin (username, password, real_name, role)
SELECT 'maintenance', 'e10adc3949ba59abbe56e057f20f883e', '运维人员', 2
WHERE NOT EXISTS (SELECT 1 FROM sys_admin WHERE username = 'maintenance');


USE smart_charge;

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

