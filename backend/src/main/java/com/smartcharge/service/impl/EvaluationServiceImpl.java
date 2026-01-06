package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;
import com.smartcharge.common.ResultCode;
import com.smartcharge.config.JwtConfig;
import com.smartcharge.entity.Evaluation;
import com.smartcharge.entity.User;
import com.smartcharge.mapper.EvaluationMapper;
import com.smartcharge.mapper.UserMapper;
import com.smartcharge.service.EvaluationService;
import com.smartcharge.vo.EvaluationVO;
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
public class EvaluationServiceImpl implements EvaluationService {
    @Autowired
    private EvaluationMapper evaluationMapper;
    
    @Autowired
    private com.smartcharge.mapper.EvaluationLikeMapper evaluationLikeMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtConfig jwtConfig;
    
    @Value("${file.upload-path}")
    private String uploadPath;
    
    @Value("${file.access-url}")
    private String accessUrl;

    @Override
    public Result<?> createEvaluation(String token, Evaluation evaluation) {
        try {
            Long userId = getUserIdFromToken(token);
            evaluation.setUserId(userId);
            evaluation.setCreateTime(LocalDateTime.now());
            evaluationMapper.insert(evaluation);
            return Result.success("评价成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> getStationEvaluations(Long stationId, Integer current, Integer size) {
        QueryWrapper<Evaluation> wrapper = new QueryWrapper<>();
        wrapper.eq("station_id", stationId);
        wrapper.orderByDesc("create_time");
        
        Page<Evaluation> page = new Page<>(current, size);
        Page<Evaluation> result = evaluationMapper.selectPage(page, wrapper);
        
        // 转换为VO，填充用户信息
        List<EvaluationVO> voList = result.getRecords().stream().map(evaluation -> {
            EvaluationVO vo = new EvaluationVO();
            vo.setId(evaluation.getId());
            vo.setUserId(evaluation.getUserId());
            vo.setStationId(evaluation.getStationId());
            vo.setOrderId(evaluation.getOrderId());
            vo.setScore(evaluation.getScore());
            vo.setContent(evaluation.getContent());
            vo.setImages(evaluation.getImages());
            vo.setEnvironmentScore(evaluation.getEnvironmentScore());
            vo.setServiceScore(evaluation.getServiceScore());
            vo.setEquipmentScore(evaluation.getEquipmentScore());
            vo.setCreateTime(evaluation.getCreateTime());
            
            // 查询用户信息
            if (evaluation.getUserId() != null) {
                User user = userMapper.selectById(evaluation.getUserId());
                if (user != null) {
                    // 优先使用nickname字段，如果为空或只有空格，则使用手机号
                    String nickname = user.getNickname();
                    log.info("评价ID: {}, 用户ID: {}, 查询到的用户nickname: {}", evaluation.getId(), evaluation.getUserId(), nickname);
                    if (nickname == null || nickname.trim().isEmpty()) {
                        nickname = user.getPhone();
                        log.info("评价ID: {}, 用户ID: {}, nickname为空，使用手机号: {}", evaluation.getId(), evaluation.getUserId(), nickname);
                    }
                    // 确保设置userNickname，不能为null
                    vo.setUserNickname(nickname != null ? nickname : user.getPhone());
                    vo.setUserAvatar(user.getAvatar());
                    vo.setUserPhone(user.getPhone());
                    // 调试日志
                    log.info("评价ID: {}, 用户ID: {}, 最终设置的userNickname: {}", evaluation.getId(), evaluation.getUserId(), vo.getUserNickname());
                } else {
                    // 如果用户不存在，设置默认值
                    vo.setUserNickname("匿名用户");
                    log.warn("评价ID: {}, 用户ID: {} 对应的用户不存在", evaluation.getId(), evaluation.getUserId());
                }
            } else {
                // 如果userId为null，设置默认值
                vo.setUserNickname("匿名用户");
                log.warn("评价ID: {} 的userId为null", evaluation.getId());
            }
            
            // 最终验证
            log.info("评价ID: {}, 最终VO的userNickname: {}", evaluation.getId(), vo.getUserNickname());
            
            return vo;
        }).collect(Collectors.toList());
        
        PageResult<EvaluationVO> pageResult = new PageResult<>(
            result.getTotal(),
            voList,
            current,
            size
        );
        return Result.success(pageResult);
    }

    @Override
    public Result<?> uploadEvaluationImage(String token, MultipartFile file) {
        try {
            Long userId = getUserIdFromToken(token);
            
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
            String filename = "eval_" + userId + "_" + UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path path = Paths.get(uploadPath + filename);
            Files.write(path, file.getBytes());
            
            // 返回访问URL
            String fileUrl = accessUrl + filename;
            return Result.success("图片上传成功", fileUrl);
        } catch (IOException e) {
            log.error("评价图片上传失败", e);
            return Result.error("图片上传失败");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> updateEvaluation(String token, Long id, Evaluation evaluation) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Evaluation exist = evaluationMapper.selectById(id);
            if (exist == null) {
                return Result.error("评价不存在");
            }
            
            if (!exist.getUserId().equals(userId)) {
                return Result.error("无权修改他人评价");
            }
            
            // 只允许修改内容和评分
            exist.setScore(evaluation.getScore());
            exist.setContent(evaluation.getContent());
            exist.setImages(evaluation.getImages());
            exist.setEnvironmentScore(evaluation.getEnvironmentScore());
            exist.setServiceScore(evaluation.getServiceScore());
            exist.setEquipmentScore(evaluation.getEquipmentScore());
            
            evaluationMapper.updateById(exist);
            return Result.success("修改成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> deleteEvaluation(String token, Long id) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Evaluation exist = evaluationMapper.selectById(id);
            if (exist == null) {
                return Result.error("评价不存在");
            }
            
            if (!exist.getUserId().equals(userId)) {
                return Result.error("无权删除他人评价");
            }
            
            evaluationMapper.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> likeEvaluation(String token, Long evaluationId) {
        try {
            Long userId = getUserIdFromToken(token);
            
            // 检查评价是否存在
            Evaluation evaluation = evaluationMapper.selectById(evaluationId);
            if (evaluation == null) {
                return Result.error("评价不存在");
            }
            
            // 检查是否已点赞
            QueryWrapper<com.smartcharge.entity.EvaluationLike> wrapper = new QueryWrapper<>();
            wrapper.eq("evaluation_id", evaluationId);
            wrapper.eq("user_id", userId);
            com.smartcharge.entity.EvaluationLike exist = evaluationLikeMapper.selectOne(wrapper);
            
            if (exist != null) {
                return Result.error("已经点过赞了");
            }
            
            // 添加点赞
            com.smartcharge.entity.EvaluationLike like = new com.smartcharge.entity.EvaluationLike();
            like.setEvaluationId(evaluationId);
            like.setUserId(userId);
            like.setCreateTime(LocalDateTime.now());
            evaluationLikeMapper.insert(like);
            
            return Result.success("点赞成功");
        } catch (Exception e) {
            log.error("点赞失败", e);
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> unlikeEvaluation(String token, Long evaluationId) {
        try {
            Long userId = getUserIdFromToken(token);
            
            QueryWrapper<com.smartcharge.entity.EvaluationLike> wrapper = new QueryWrapper<>();
            wrapper.eq("evaluation_id", evaluationId);
            wrapper.eq("user_id", userId);
            
            int deleted = evaluationLikeMapper.delete(wrapper);
            if (deleted > 0) {
                return Result.success("取消点赞成功");
            } else {
                return Result.error("未找到点赞记录");
            }
        } catch (Exception e) {
            log.error("取消点赞失败", e);
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> checkLikeStatus(String token, Long evaluationId) {
        try {
            Long userId = getUserIdFromToken(token);
            
            QueryWrapper<com.smartcharge.entity.EvaluationLike> wrapper = new QueryWrapper<>();
            wrapper.eq("evaluation_id", evaluationId);
            wrapper.eq("user_id", userId);
            
            com.smartcharge.entity.EvaluationLike like = evaluationLikeMapper.selectOne(wrapper);
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("isLiked", like != null);
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查点赞状态失败", e);
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> getEvaluationLikeCount(Long evaluationId) {
        QueryWrapper<com.smartcharge.entity.EvaluationLike> wrapper = new QueryWrapper<>();
        wrapper.eq("evaluation_id", evaluationId);
        
        Long count = evaluationLikeMapper.selectCount(wrapper);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }

    private Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtConfig.parseToken(token).get("userId", Long.class);
    }
}

