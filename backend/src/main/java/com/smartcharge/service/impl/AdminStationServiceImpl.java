package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;
import com.smartcharge.common.ResultCode;
import com.smartcharge.config.JwtConfig;
import com.smartcharge.entity.ChargePile;
import com.smartcharge.entity.Station;
import com.smartcharge.mapper.ChargePileMapper;
import com.smartcharge.mapper.StationMapper;
import com.smartcharge.service.AdminStationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminStationServiceImpl implements AdminStationService {
    @Autowired
    private StationMapper stationMapper;
    
    @Autowired
    private ChargePileMapper chargePileMapper;
    
    @Autowired
    private JwtConfig jwtConfig;
    
    @Value("${file.upload-path:uploads/}")
    private String uploadPath;
    
    @Value("${file.access-url:/uploads/}")
    private String accessUrl;

    @Override
    public Result<PageResult<?>> getStationList(String name, Integer current, Integer size) {
        QueryWrapper<Station> wrapper = new QueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like("name", name.trim());
        }
        
        Page<Station> page = new Page<>(current, size);
        Page<Station> result = stationMapper.selectPage(page, wrapper);
        
        // 动态计算总桩数和可用桩数
        List<Station> stations = result.getRecords();
        for (Station station : stations) {
            // 动态统计总桩数（该站点的所有充电桩数量）
            QueryWrapper<ChargePile> totalPileWrapper = new QueryWrapper<>();
            totalPileWrapper.eq("station_id", station.getId());
            long totalCount = chargePileMapper.selectCount(totalPileWrapper);
            station.setTotalPiles((int) totalCount);
            
            // 动态计算可用桩数：如果充电站状态为维护中，可用桩数为0
            // 否则，统计该站点状态为0（空闲）的充电桩数量
            Integer stationStatus = station.getStatus();
            // 确保status值正确：1表示维护中，0或其他值表示正常
            if (stationStatus != null && stationStatus.intValue() == 1) {
                // 维护中，可用桩数为0
                station.setAvailablePiles(0);
                log.info("充电站[{}] (ID:{}) 状态为维护中(status={})，设置可用桩数为0，总桩数: {}", 
                    station.getName(), station.getId(), stationStatus, station.getTotalPiles());
            } else {
                // 正常状态，统计空闲的充电桩数量
                QueryWrapper<ChargePile> pileWrapper = new QueryWrapper<>();
                pileWrapper.eq("station_id", station.getId());
                pileWrapper.eq("status", 0); // 0-空闲
                long availableCount = chargePileMapper.selectCount(pileWrapper);
                station.setAvailablePiles((int) availableCount);
                log.debug("充电站[{}] (ID:{}) 状态为正常(status={})，统计空闲桩数: {}，总桩数: {}", 
                    station.getName(), station.getId(), stationStatus, availableCount, station.getTotalPiles());
            }
        }
        
        PageResult<Station> pageResult = new PageResult<>(
            result.getTotal(),
            stations,
            current,
            size
        );
        return Result.success(pageResult);
    }

    @Override
    public Result<?> getStationDetail(Long id) {
        Station station = stationMapper.selectById(id);
        if (station == null) {
            return Result.error("充电站不存在");
        }
        
        // 动态统计总桩数（该站点的所有充电桩数量）
        QueryWrapper<ChargePile> totalPileWrapper = new QueryWrapper<>();
        totalPileWrapper.eq("station_id", station.getId());
        long totalCount = chargePileMapper.selectCount(totalPileWrapper);
        station.setTotalPiles((int) totalCount);
        
        // 动态计算可用桩数：如果充电站状态为维护中，可用桩数为0
        if (station.getStatus() != null && station.getStatus() == 1) {
            // 维护中，可用桩数为0
            station.setAvailablePiles(0);
        } else {
            // 正常状态，统计空闲的充电桩数量
            QueryWrapper<ChargePile> pileWrapper = new QueryWrapper<>();
            pileWrapper.eq("station_id", station.getId());
            pileWrapper.eq("status", 0); // 0-空闲
            long availableCount = chargePileMapper.selectCount(pileWrapper);
            station.setAvailablePiles((int) availableCount);
        }
        
        return Result.success(station);
    }

    @Override
    public Result<?> addStation(String token, Station station) {
        try {
            Long adminId = getAdminIdFromToken(token);
            station.setCreateTime(LocalDateTime.now());
            station.setUpdateTime(LocalDateTime.now());
            stationMapper.insert(station);
            return Result.success("新增成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> updateStation(String token, Long id, Station station) {
        try {
            Long adminId = getAdminIdFromToken(token);
            Station existStation = stationMapper.selectById(id);
            if (existStation == null) {
                return Result.error("充电站不存在");
            }
            station.setId(id);
            station.setUpdateTime(LocalDateTime.now());
            stationMapper.updateById(station);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> deleteStation(String token, Long id) {
        try {
            Long adminId = getAdminIdFromToken(token);
            Station station = stationMapper.selectById(id);
            if (station == null) {
                return Result.error("充电站不存在");
            }
            stationMapper.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> uploadStationImage(String token, Long stationId, MultipartFile file) {
        try {
            Long adminId = getAdminIdFromToken(token);
            
            Station station = stationMapper.selectById(stationId);
            if (station == null) {
                return Result.error("充电站不存在");
            }
            
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只能上传图片文件");
            }
            
            // 验证文件大小（5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("图片大小不能超过5MB");
            }
            
            // 创建上传目录
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = "station_" + stationId + "_" + UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path path = Paths.get(uploadPath + filename);
            Files.write(path, file.getBytes());
            
            // 更新充电站图片
            String fileUrl = accessUrl + filename;
            String images = station.getImages();
            if (images == null || images.isEmpty()) {
                station.setImages(fileUrl);
            } else {
                station.setImages(images + "," + fileUrl);
            }
            station.setUpdateTime(LocalDateTime.now());
            stationMapper.updateById(station);
            
            return Result.success("图片上传成功", fileUrl);
        } catch (IOException e) {
            log.error("充电站图片上传失败", e);
            return Result.error("图片上传失败");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }
    
    private Long getAdminIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtConfig.parseToken(token).get("adminId", Long.class);
    }
}

