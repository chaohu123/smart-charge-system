package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;
import com.smartcharge.entity.ChargePile;
import com.smartcharge.entity.Evaluation;
import com.smartcharge.entity.Station;
import com.smartcharge.entity.Reservation;
import com.smartcharge.mapper.ChargePileMapper;
import com.smartcharge.mapper.EvaluationMapper;
import com.smartcharge.mapper.StationMapper;
import com.smartcharge.mapper.UserMapper;
import com.smartcharge.mapper.VehicleMapper;
import com.smartcharge.mapper.OrderMapper;
import com.smartcharge.mapper.ReservationMapper;
import com.smartcharge.entity.User;
import com.smartcharge.entity.Vehicle;
import com.smartcharge.service.StationService;
import com.smartcharge.service.EvaluationService;
import com.smartcharge.vo.ChargePileVO;
import com.smartcharge.utils.DistanceUtil;
import com.smartcharge.config.JwtConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Comparator;

@Slf4j
@Service
public class StationServiceImpl implements StationService {
    @Autowired
    private StationMapper stationMapper;
    
    @Autowired
    private ChargePileMapper pileMapper;
    
    @Autowired
    private EvaluationMapper evaluationMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private VehicleMapper vehicleMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private ReservationMapper reservationMapper;
    
    @Autowired
    private EvaluationService evaluationService;
    
    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public Result<PageResult<?>> getNearbyStations(BigDecimal longitude, BigDecimal latitude, 
                                                   Integer radius, Integer type, Integer status, 
                                                   Integer current, Integer size) {
        QueryWrapper<Station> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        // 先查询所有符合条件的充电站
        List<Station> allStations = stationMapper.selectList(wrapper);
        
        // 使用Haversine公式计算距离并过滤
        double lat = latitude.doubleValue();
        double lon = longitude.doubleValue();
        List<Station> filteredStations = allStations.stream()
            .filter(station -> {
                double distance = DistanceUtil.calculateDistance(
                    lat, lon,
                    station.getLatitude().doubleValue(),
                    station.getLongitude().doubleValue()
                );
                station.setDistance((int) distance);
                return radius == null || distance <= radius;
            })
            .sorted((s1, s2) -> Integer.compare(s1.getDistance(), s2.getDistance()))
            .collect(Collectors.toList());
        
        // 动态计算可用桩数
        for (Station station : filteredStations) {
            calculateAvailablePiles(station);
        }
        
        // 分页处理
        int total = filteredStations.size();
        int start = (current - 1) * size;
        int end = Math.min(start + size, total);
        List<Station> pagedStations = start < total ? filteredStations.subList(start, end) : new ArrayList<>();
        
        PageResult<Station> pageResult = new PageResult<>(
            (long) total,
            pagedStations,
            current,
            size
        );
        return Result.success(pageResult);
    }
    
    /**
     * 计算充电站的总桩数和可用桩数
     * 总桩数：动态统计该站点的所有充电桩数量
     * 可用桩数：如果充电站状态为维护中，可用桩数为0；否则统计该站点状态为0（空闲）的充电桩数量
     */
    private void calculateAvailablePiles(Station station) {
        // 动态统计总桩数（该站点的所有充电桩数量）
        QueryWrapper<ChargePile> totalPileWrapper = new QueryWrapper<>();
        totalPileWrapper.eq("station_id", station.getId());
        long totalCount = pileMapper.selectCount(totalPileWrapper);
        station.setTotalPiles((int) totalCount);
        
        // 计算可用桩数
        if (station.getStatus() != null && station.getStatus() == 1) {
            // 维护中，可用桩数为0
            station.setAvailablePiles(0);
        } else {
            // 正常状态，统计空闲的充电桩数量
            QueryWrapper<ChargePile> pileWrapper = new QueryWrapper<>();
            pileWrapper.eq("station_id", station.getId());
            pileWrapper.eq("status", 0); // 0-空闲
            long availableCount = pileMapper.selectCount(pileWrapper);
            station.setAvailablePiles((int) availableCount);
        }
    }

    @Override
    public Result<?> getStationDetail(Long id) {
        Station station = stationMapper.selectById(id);
        if (station == null) {
            return Result.error("充电站不存在");
        }
        
        // 动态计算可用桩数
        calculateAvailablePiles(station);
        
        return Result.success(station);
    }

    @Override
    public Result<?> getStationPiles(Long id) {
        QueryWrapper<ChargePile> wrapper = new QueryWrapper<>();
        wrapper.eq("station_id", id);
        List<ChargePile> piles = pileMapper.selectList(wrapper);
        
        // 查询所有待使用的预约（包括未来和当前时间段的预约）
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Reservation> reservationWrapper = new QueryWrapper<>();
        reservationWrapper.eq("status", 0) // 待使用
                .ge("end_time", now); // 结束时间 >= 当前时间（包括未来和当前时间段的预约）
        
        List<Reservation> activeReservations = reservationMapper.selectList(reservationWrapper);
        
        // 创建充电桩ID到预约信息的映射
        Map<Long, Reservation> reservationMap = activeReservations.stream()
                .collect(java.util.stream.Collectors.toMap(
                        Reservation::getPileId,
                        r -> r,
                        (r1, r2) -> r1 // 如果有多个预约，保留第一个
                ));
        
        // 转换为VO并设置预约信息
        List<ChargePileVO> pileVOs = piles.stream().map(pile -> {
            ChargePileVO vo = new ChargePileVO();
            vo.setId(pile.getId());
            vo.setStationId(pile.getStationId());
            vo.setPileNumber(pile.getPileNumber());
            vo.setModel(pile.getModel());
            vo.setPower(pile.getPower());
            vo.setType(pile.getType());
            vo.setProtocol(pile.getProtocol());
            vo.setStatus(pile.getStatus());
            vo.setQrCode(pile.getQrCode());
            vo.setPrice(pile.getPrice());
            vo.setLastHeartbeat(pile.getLastHeartbeat());
            vo.setCreateTime(pile.getCreateTime());
            vo.setUpdateTime(pile.getUpdateTime());
            
            // 检查是否有有效的预约
            Reservation reservation = reservationMap.get(pile.getId());
            if (reservation != null) {
                vo.setIsReserved(true);
                vo.setReservationStartTime(reservation.getStartTime());
                vo.setReservationEndTime(reservation.getEndTime());
            } else {
                vo.setIsReserved(false);
            }
            
            return vo;
        }).collect(java.util.stream.Collectors.toList());
        
        return Result.success(pileVOs);
    }

    @Override
    public Result<PageResult<?>> searchStations(String keyword, Integer type, 
                                               BigDecimal minPrice, BigDecimal maxPrice,
                                               Integer freeParking, Integer hasLounge,
                                               Integer is24Hours, Integer reservable,
                                               Integer current, Integer size) {
        QueryWrapper<Station> wrapper = new QueryWrapper<>();
        
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("name", keyword).or().like("address", keyword));
        }
        
        // 充电类型筛选
        if (type != null) {
            // 注意：这里需要根据充电桩类型筛选，暂时先不过滤
            // 实际应该关联查询充电桩表
        }
        
        // 价格范围筛选
        if (minPrice != null) {
            wrapper.ge("service_fee", minPrice);
        }
        if (maxPrice != null) {
            wrapper.le("service_fee", maxPrice);
        }
        
        // 免费停车筛选
        if (freeParking != null && freeParking == 1) {
            wrapper.eq("free_parking", 1);
        }
        
        // 带休息室筛选
        if (hasLounge != null && hasLounge == 1) {
            wrapper.eq("has_lounge", 1);
        }
        
        // 24小时营业筛选
        if (is24Hours != null && is24Hours == 1) {
            wrapper.eq("is_24_hours", 1);
        }
        
        // 可预约筛选
        if (reservable != null && reservable == 1) {
            wrapper.eq("reservable", 1);
        }
        
        // 只查询正常状态的充电站
        wrapper.eq("status", 0);
        
        Page<Station> page = new Page<>(current, size);
        Page<Station> result = stationMapper.selectPage(page, wrapper);
        
        // 动态计算可用桩数
        for (Station station : result.getRecords()) {
            calculateAvailablePiles(station);
        }
        
        PageResult<Station> pageResult = new PageResult<>(
            result.getTotal(),
            result.getRecords(),
            current,
            size
        );
        return Result.success(pageResult);
    }

    @Override
    public Result<?> getStationEvaluations(Long id, Integer current, Integer size) {
        // 委托给EvaluationService处理，因为它会转换为EvaluationVO并填充用户信息
        return evaluationService.getStationEvaluations(id, current, size);
    }
    
    @Override
    public Result<?> getRecommendedStations(String token, BigDecimal longitude, BigDecimal latitude, Integer radius) {
        try {
            Long userId = null;
            Vehicle userVehicle = null;
            
            // 尝试获取用户信息
            try {
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }
                userId = getUserIdFromToken(token);
                
                // 获取用户默认车辆信息
                if (userId != null) {
                    QueryWrapper<Vehicle> vehicleWrapper = new QueryWrapper<>();
                    vehicleWrapper.eq("user_id", userId);
                    vehicleWrapper.eq("is_default", 1);
                    userVehicle = vehicleMapper.selectOne(vehicleWrapper);
                }
            } catch (Exception e) {
                // 未登录或token无效，继续使用匿名推荐
                log.debug("用户未登录，使用匿名推荐算法");
            }
            
            // 获取附近充电站
            Result<PageResult<?>> nearbyResult = getNearbyStations(
                longitude, latitude, radius != null ? radius : 10000, 
                null, 0, 1, 50
            );
            
            if (nearbyResult.getCode() != 200) {
                return nearbyResult;
            }
            
            List<Station> stations = (List<Station>) ((PageResult<?>) nearbyResult.getData()).getRecords();
            final Long currentUserId = userId;
            final Vehicle currentUserVehicle = userVehicle;
            
            // 确保所有站点的可用桩数已正确计算
            for (Station station : stations) {
                calculateAvailablePiles(station);
            }
            
            // 智能推荐算法
            List<Map<String, Object>> recommendedStations = stations.stream().map(station -> {
                Map<String, Object> stationMap = new HashMap<>();
                stationMap.put("id", station.getId());
                stationMap.put("name", station.getName());
                stationMap.put("address", station.getAddress());
                stationMap.put("businessHours", station.getBusinessHours());
                stationMap.put("longitude", station.getLongitude());
                stationMap.put("latitude", station.getLatitude());
                stationMap.put("availablePiles", station.getAvailablePiles());
                stationMap.put("totalPiles", station.getTotalPiles());
                stationMap.put("serviceFee", station.getServiceFee());
                stationMap.put("distance", station.getDistance());
                
                // 计算推荐分数
                double score = 0.0;
                
                // 1. 距离分数（越近分数越高，最高50分）
                if (station.getDistance() != null) {
                    double distanceScore = Math.max(0, 50 - (station.getDistance() / 200.0));
                    score += distanceScore;
                }
                
                // 2. 可用性分数（可用桩越多分数越高，最高30分）
                if (station.getTotalPiles() > 0) {
                    double availabilityScore = (station.getAvailablePiles() * 1.0 / station.getTotalPiles()) * 30;
                    score += availabilityScore;
                }
                
                // 3. 价格分数（价格越低分数越高，最高10分）
                if (station.getServiceFee() != null) {
                    double priceScore = Math.max(0, 10 - (station.getServiceFee().doubleValue() * 2));
                    score += priceScore;
                }
                
                // 4. 用户偏好分数（如果用户有历史订单，优先推荐，最高10分）
                if (currentUserId != null) {
                    QueryWrapper<com.smartcharge.entity.Order> orderWrapper = new QueryWrapper<>();
                    orderWrapper.eq("user_id", currentUserId);
                    orderWrapper.eq("station_id", station.getId());
                    orderWrapper.in("status", 1, 2); // 进行中或已完成
                    long orderCount = orderMapper.selectCount(orderWrapper);
                    if (orderCount > 0) {
                        score += 10; // 有历史订单，加分
                    }
                }
                
                // 5. 车辆匹配分数（如果用户有车辆信息，匹配快充/慢充，最高10分）
                if (currentUserVehicle != null && currentUserVehicle.getBatteryCapacity() != null) {
                    // 大电池容量车辆优先推荐快充站
                    QueryWrapper<ChargePile> pileWrapper = new QueryWrapper<>();
                    pileWrapper.eq("station_id", station.getId());
                    pileWrapper.eq("status", 0); // 空闲
                    List<ChargePile> availablePiles = pileMapper.selectList(pileWrapper);
                    
                    boolean hasFastCharge = availablePiles.stream().anyMatch(p -> p.getType() == 1);
                    boolean hasSlowCharge = availablePiles.stream().anyMatch(p -> p.getType() == 0);
                    
                    if (currentUserVehicle.getBatteryCapacity() >= 60 && hasFastCharge) {
                        score += 10; // 大电池容量，推荐快充
                    } else if (currentUserVehicle.getBatteryCapacity() < 60 && hasSlowCharge) {
                        score += 8; // 小电池容量，推荐慢充
                    }
                }
                
                stationMap.put("recommendScore", score);
                return stationMap;
            })
            .sorted((s1, s2) -> {
                Double score1 = (Double) s1.get("recommendScore");
                Double score2 = (Double) s2.get("recommendScore");
                return score2.compareTo(score1); // 降序排序
            })
            .limit(10) // 返回前10个推荐
            .collect(Collectors.toList());
            
            return Result.success(recommendedStations);
        } catch (Exception e) {
            log.error("获取推荐充电站失败", e);
            return Result.error("获取推荐充电站失败：" + e.getMessage());
        }
    }
    
    private Long getUserIdFromToken(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            io.jsonwebtoken.Claims claims = jwtConfig.parseToken(token);
            Object userIdObj = claims.get("userId");
            if (userIdObj == null) {
                return null;
            }
            if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            }
            return Long.parseLong(userIdObj.toString());
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public Result<?> searchRouteStations(BigDecimal startLng, BigDecimal startLat, 
                                         BigDecimal endLng, BigDecimal endLat, 
                                         Integer vehicleRange, Integer chargeType) {
        try {
            // 计算起点到终点的直线距离（公里）
            double totalDistance = DistanceUtil.calculateDistance(
                startLat.doubleValue(), startLng.doubleValue(),
                endLat.doubleValue(), endLng.doubleValue()
            ) / 1000.0; // 转换为公里
            
            // 如果车辆续航足够，不需要中途充电
            if (vehicleRange != null && vehicleRange >= totalDistance) {
                // 仍然返回终点附近的充电站，以防万一
                Result<PageResult<?>> nearbyResult = getNearbyStations(
                    endLng, endLat, 5000, chargeType, 0, 1, 5
                );
                if (nearbyResult.getCode() == 200) {
                    List<Station> endStations = (List<Station>) ((PageResult<?>) nearbyResult.getData()).getRecords();
                    return Result.success(endStations);
                }
                return Result.success(new ArrayList<>());
            }
            
            // 需要中途充电，计算沿途充电站
            // 简化算法：在起点和终点之间均匀分布几个搜索点
            int searchPoints = vehicleRange != null ? (int) Math.ceil(totalDistance / (vehicleRange * 0.7)) : 3;
            searchPoints = Math.min(searchPoints, 5); // 最多5个搜索点
            
            List<Station> routeStations = new ArrayList<>();
            Set<Long> addedStationIds = new HashSet<>();
            
            for (int i = 1; i <= searchPoints; i++) {
                // 计算搜索点坐标（在起点和终点之间插值）
                double ratio = (double) i / (searchPoints + 1);
                double searchLat = startLat.doubleValue() + (endLat.doubleValue() - startLat.doubleValue()) * ratio;
                double searchLng = startLng.doubleValue() + (endLng.doubleValue() - startLng.doubleValue()) * ratio;
                
                // 在搜索点附近查找充电站
                BigDecimal searchLatBd = BigDecimal.valueOf(searchLat);
                BigDecimal searchLngBd = BigDecimal.valueOf(searchLng);
                
                // 搜索半径：车辆续航的30%（确保能找到充电站）
                int searchRadius = vehicleRange != null ? (int) (vehicleRange * 0.3 * 1000) : 10000;
                
                Result<PageResult<?>> nearbyResult = getNearbyStations(
                    searchLngBd, searchLatBd, searchRadius, chargeType, 0, 1, 3
                );
                
                if (nearbyResult.getCode() == 200) {
                    List<Station> nearbyStations = (List<Station>) ((PageResult<?>) nearbyResult.getData()).getRecords();
                    
                    // 选择最近的可用充电站
                    for (Station station : nearbyStations) {
                        if (!addedStationIds.contains(station.getId()) && station.getAvailablePiles() > 0) {
                            // 计算到起点的距离
                            double distanceFromStart = DistanceUtil.calculateDistance(
                                startLat.doubleValue(), startLng.doubleValue(),
                                station.getLatitude().doubleValue(), station.getLongitude().doubleValue()
                            );
                            station.setDistance((int) distanceFromStart);
                            routeStations.add(station);
                            addedStationIds.add(station.getId());
                            break; // 每个搜索点只选择一个充电站
                        }
                    }
                }
            }
            
            // 添加终点附近的充电站
            Result<PageResult<?>> endResult = getNearbyStations(
                endLng, endLat, 5000, chargeType, 0, 1, 3
            );
            if (endResult.getCode() == 200) {
                List<Station> endStations = (List<Station>) ((PageResult<?>) endResult.getData()).getRecords();
                for (Station station : endStations) {
                    if (!addedStationIds.contains(station.getId()) && station.getAvailablePiles() > 0) {
                        double distanceFromStart = DistanceUtil.calculateDistance(
                            startLat.doubleValue(), startLng.doubleValue(),
                            station.getLatitude().doubleValue(), station.getLongitude().doubleValue()
                        );
                        station.setDistance((int) distanceFromStart);
                        routeStations.add(station);
                        addedStationIds.add(station.getId());
                    }
                }
            }
            
            // 按距离排序
            routeStations.sort(Comparator.comparingInt(Station::getDistance));
            
            return Result.success(routeStations);
        } catch (Exception e) {
            log.error("搜索沿途充电站失败", e);
            return Result.error("搜索沿途充电站失败：" + e.getMessage());
        }
    }
}

