# 大连充电站数据插入说明

本目录包含用于在大连市插入10个充电站及其充电桩数据的SQL脚本。

## 文件说明

1. **dalian_stations.sql** - 基础版本，使用固定ID（假设已有5个北京充电站，ID为6-15）
2. **dalian_stations_v2.sql** - 使用MySQL变量的动态版本（可能在某些MySQL版本中不稳定）
3. **dalian_stations_simple.sql** - 简化版本，推荐使用（使用固定ID，最稳定）

## 推荐使用方法

### 方法1：使用简化版本（推荐）

```sql
-- 1. 先查询当前最大充电站ID
SELECT MAX(id) FROM charge_station;

-- 2. 如果最大ID是5（已有5个北京充电站），直接执行：
source backend/src/main/resources/db/dalian_stations_simple.sql;

-- 3. 如果最大ID不是5，需要修改dalian_stations_simple.sql中的station_id
--    例如：如果最大ID是10，则修改所有station_id从6改为11，从7改为12，以此类推
```

### 方法2：手动执行（最安全）

```sql
-- 1. 先插入充电站
INSERT INTO charge_station (name, address, longitude, latitude, business_hours, service_phone, total_piles, available_piles, service_fee, status, manager_id) VALUES 
('星海广场充电站', '辽宁省大连市沙河口区星海广场', 121.586667, 38.886667, '00:00-24:00', '0411-12345678', 8, 6, 0.55, 0, 2),
-- ... 其他9个充电站

-- 2. 查询刚插入的充电站ID
SELECT id, name FROM charge_station WHERE address LIKE '%大连%' ORDER BY id DESC LIMIT 10;

-- 3. 根据查询到的ID，修改SQL中的station_id，然后插入充电桩
```

## 充电站列表

1. **星海广场充电站** - 8个充电桩
2. **中山广场充电站** - 12个充电桩
3. **大连火车站充电站** - 15个充电桩
4. **大连理工大学充电站** - 10个充电桩
5. **大连国际机场充电站** - 20个充电桩
6. **大连港充电站** - 6个充电桩
7. **大连北站充电站** - 12个充电桩
8. **大连森林动物园充电站** - 8个充电桩
9. **大连万达广场充电站** - 10个充电桩
10. **大连软件园充电站** - 14个充电桩

**总计：115个充电桩**

## 充电桩类型

- **慢充桩（AC）**：7kW功率，ABB AC Wallbox型号
- **快充桩（DC）**：
  - Tesla Supercharger：120kW功率
  - BYD DC Charger：60kW功率

## 注意事项

1. 确保数据库中已有管理员账号（manager_id为2和3）
2. 如果manager_id不存在，请先创建或修改SQL中的manager_id
3. 执行前建议备份数据库
4. 充电站坐标使用大连市真实地理位置
5. 充电桩编号格式：DL001-DL115（DL代表大连）

## 验证数据

插入完成后，可以使用以下SQL验证：

```sql
-- 查看所有大连充电站
SELECT id, name, address, total_piles, available_piles FROM charge_station WHERE address LIKE '%大连%';

-- 查看所有大连充电桩
SELECT cp.id, cp.station_id, cs.name, cp.pile_number, cp.type, cp.power, cp.status 
FROM charge_pile cp 
JOIN charge_station cs ON cp.station_id = cs.id 
WHERE cs.address LIKE '%大连%' 
ORDER BY cp.station_id, cp.pile_number;
```

