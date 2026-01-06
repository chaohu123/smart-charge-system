package com.smartcharge.service.impl;

import com.smartcharge.common.Result;
import com.smartcharge.common.ResultCode;
import com.smartcharge.config.JwtConfig;
import com.smartcharge.entity.FaultReport;
import com.smartcharge.mapper.FaultReportMapper;
import com.smartcharge.service.FaultReportService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FaultReportServiceImpl implements FaultReportService {
    @Autowired
    private FaultReportMapper faultReportMapper;
    
    @Autowired
    private JwtConfig jwtConfig;
    
    @Value("${file.upload-path}")
    private String uploadPath;
    
    @Value("${file.access-url}")
    private String accessUrl;

    @Override
    public Result<?> reportFault(String token, Long stationId, Long pileId, String faultType, String description, MultipartFile[] images) {
        try {
            Long userId = getUserIdFromToken(token);
            
            FaultReport report = new FaultReport();
            report.setReportNo("FAULT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
            report.setUserId(userId);
            report.setStationId(stationId);
            report.setPileId(pileId);
            report.setFaultType(faultType);
            report.setDescription(description);
            report.setStatus(0); // 待处理
            report.setCreateTime(LocalDateTime.now());
            report.setUpdateTime(LocalDateTime.now());
            
            // 上传图片
            if (images != null && images.length > 0) {
                List<String> imageUrls = new ArrayList<>();
                File dir = new File(uploadPath + "fault/");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                
                for (MultipartFile file : images) {
                    if (file != null && !file.isEmpty()) {
                        String originalFilename = file.getOriginalFilename();
                        if (originalFilename != null && originalFilename.contains(".")) {
                            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                            String filename = "fault_" + UUID.randomUUID().toString() + extension;
                        
                            Path path = Paths.get(uploadPath + "fault/" + filename);
                            Files.write(path, file.getBytes());
                            
                            imageUrls.add(accessUrl + "fault/" + filename);
                        }
                    }
                }
                if (!imageUrls.isEmpty()) {
                    report.setImages(String.join(",", imageUrls));
                }
            }
            
            faultReportMapper.insert(report);
            return Result.success("故障上报成功", report);
        } catch (IOException e) {
            log.error("故障图片上传失败", e);
            return Result.error("图片上传失败");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> getFaultReportList(String token) {
        try {
            Long userId = getUserIdFromToken(token);
            
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FaultReport> wrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.orderByDesc("create_time");
            
            List<FaultReport> reports = faultReportMapper.selectList(wrapper);
            return Result.success(reports);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    private Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtConfig.parseToken(token).get("userId", Long.class);
    }
}

