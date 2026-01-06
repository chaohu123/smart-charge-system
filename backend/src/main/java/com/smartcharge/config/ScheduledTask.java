package com.smartcharge.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartcharge.entity.Reservation;
import com.smartcharge.entity.User;
import com.smartcharge.entity.ChargePile;
import com.smartcharge.entity.FaultAlert;
import com.smartcharge.mapper.ReservationMapper;
import com.smartcharge.mapper.UserMapper;
import com.smartcharge.mapper.ChargePileMapper;
import com.smartcharge.mapper.FaultAlertMapper;
import com.smartcharge.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class ScheduledTask {
    @Autowired
    private ReservationMapper reservationMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private ChargePileMapper pileMapper;
    
    @Autowired
    private FaultAlertMapper alertMapper;

    /**
     * 检查预约超时，每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkReservationTimeout() {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Reservation> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0); // 待使用
        wrapper.lt("start_time", now.minusMinutes(30)); // 开始时间已过30分钟
        
        List<Reservation> expiredReservations = reservationMapper.selectList(wrapper);
        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(3); // 已过期
            reservation.setUpdateTime(LocalDateTime.now());
            reservationMapper.updateById(reservation);
            
            // 扣减信用分（预约超时未使用扣5分）
            User user = userMapper.selectById(reservation.getUserId());
            if (user != null && user.getCreditScore() > 0) {
                int newScore = Math.max(0, user.getCreditScore() - 5);
                user.setCreditScore(newScore);
                user.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(user);
            }
            
            // 发送通知
            notificationService.sendNotification(
                reservation.getUserId(),
                "预约已过期",
                "您的预约" + reservation.getReservationNo() + "已过期，信用分-5",
                "系统"
            );
            
            log.info("预约超时自动取消: {}, 用户ID: {}", reservation.getReservationNo(), reservation.getUserId());
        }
    }
    
    /**
     * 预约提醒，提前30分钟提醒
     */
    @Scheduled(cron = "0 * * * * ?")
    public void sendReservationReminder() {
        LocalDateTime reminderTime = LocalDateTime.now().plusMinutes(30);
        QueryWrapper<Reservation> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 0); // 待使用
        wrapper.between("start_time", LocalDateTime.now(), reminderTime);
        
        List<Reservation> reservations = reservationMapper.selectList(wrapper);
        for (Reservation reservation : reservations) {
            notificationService.sendNotification(
                reservation.getUserId(),
                "预约提醒",
                "您的预约将在30分钟后开始，请及时到达充电站",
                "系统"
            );
        }
    }

    /**
     * 检查充电订单，自动结算已完成订单
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void autoSettleOrders() {
        // 这里可以添加自动结算逻辑
        // 例如：检查长时间未支付的订单，自动取消
        log.debug("执行订单自动结算检查");
    }
    
    /**
     * 检测设备离线报警，每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkDeviceOffline() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusMinutes(5); // 5分钟未收到心跳视为离线
        
        QueryWrapper<ChargePile> wrapper = new QueryWrapper<>();
        wrapper.isNotNull("last_heartbeat");
        wrapper.lt("last_heartbeat", threshold);
        wrapper.ne("status", 3); // 排除已经是离线状态的
        
        List<ChargePile> offlinePiles = pileMapper.selectList(wrapper);
        
        for (ChargePile pile : offlinePiles) {
            // 检查是否已有未处理的离线报警
            QueryWrapper<FaultAlert> alertWrapper = new QueryWrapper<>();
            alertWrapper.eq("pile_id", pile.getId());
            alertWrapper.eq("alert_type", 0); // 设备离线
            alertWrapper.in("status", 0, 1); // 未处理或处理中
            alertWrapper.orderByDesc("create_time");
            alertWrapper.last("LIMIT 1");
            
            FaultAlert existingAlert = alertMapper.selectOne(alertWrapper);
            
            if (existingAlert == null) {
                // 创建新的离线报警
                FaultAlert alert = new FaultAlert();
                alert.setAlertNo("ALERT" + System.currentTimeMillis() + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase());
                alert.setStationId(pile.getStationId());
                alert.setPileId(pile.getId());
                alert.setAlertType(0); // 设备离线
                alert.setAlertLevel(2); // 重要
                alert.setDescription("设备超过5分钟未发送心跳，可能已离线");
                alert.setStatus(0); // 未处理
                alert.setCreateTime(now);
                alert.setUpdateTime(now);
                alertMapper.insert(alert);
                
                // 更新充电桩状态为离线
                pile.setStatus(3); // 离线
                pile.setUpdateTime(now);
                pileMapper.updateById(pile);
                
                log.warn("检测到设备离线: 充电桩ID={}, 最后心跳时间={}", pile.getId(), pile.getLastHeartbeat());
            }
        }
    }
    
    /**
     * 检测异常电流/电压报警，每分钟执行一次
     * 注意：这里使用模拟数据，实际应该从设备实时数据中获取
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkAbnormalCurrentVoltage() {
        // 这里应该从实时数据源获取电流和电压值
        // 为了演示，我们检查正在充电的充电桩
        QueryWrapper<ChargePile> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 占用中（正在充电）
        
        List<ChargePile> chargingPiles = pileMapper.selectList(wrapper);
        
        for (ChargePile pile : chargingPiles) {
            // 模拟检测：这里应该从实际设备获取电流和电压值
            // 假设正常电流范围：0-100A，正常电压范围：200-400V
            // 实际应该从设备实时数据接口获取
            
            // 示例：如果检测到异常，创建报警
            // 这里只是示例代码，实际需要根据真实数据来判断
            /*
            if (currentValue > 100 || currentValue < 0) {
                // 异常电流报警
                createAbnormalAlert(pile, 1, currentValue, 100, "电流异常");
            }
            if (voltageValue > 400 || voltageValue < 200) {
                // 异常电压报警
                createAbnormalAlert(pile, 2, voltageValue, 300, "电压异常");
            }
            */
        }
    }
    
    /**
     * 创建异常报警
     */
    private void createAbnormalAlert(ChargePile pile, Integer alertType, java.math.BigDecimal value, 
                                     java.math.BigDecimal threshold, String description) {
        LocalDateTime now = LocalDateTime.now();
        
        // 检查是否已有未处理的同类报警
        QueryWrapper<FaultAlert> alertWrapper = new QueryWrapper<>();
        alertWrapper.eq("pile_id", pile.getId());
        alertWrapper.eq("alert_type", alertType);
        alertWrapper.in("status", 0, 1); // 未处理或处理中
        alertWrapper.orderByDesc("create_time");
        alertWrapper.last("LIMIT 1");
        
        FaultAlert existingAlert = alertMapper.selectOne(alertWrapper);
        
        if (existingAlert == null) {
            FaultAlert alert = new FaultAlert();
            alert.setAlertNo("ALERT" + System.currentTimeMillis() + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase());
            alert.setStationId(pile.getStationId());
            alert.setPileId(pile.getId());
            alert.setAlertType(alertType);
            alert.setAlertLevel(3); // 紧急
            if (alertType == 1) {
                alert.setCurrentValue(value);
            } else if (alertType == 2) {
                alert.setVoltageValue(value);
            }
            alert.setThresholdValue(threshold);
            alert.setDescription(description + "，当前值：" + value);
            alert.setStatus(0); // 未处理
            alert.setCreateTime(now);
            alert.setUpdateTime(now);
            alertMapper.insert(alert);
            
            log.warn("检测到异常报警: 充电桩ID={}, 类型={}, 值={}", pile.getId(), alertType, value);
        }
    }
}


