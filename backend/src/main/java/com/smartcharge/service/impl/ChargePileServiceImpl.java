package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartcharge.common.Result;
import com.smartcharge.entity.ChargePile;
import com.smartcharge.mapper.ChargePileMapper;
import com.smartcharge.service.ChargePileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class ChargePileServiceImpl implements ChargePileService {
    @Autowired
    private ChargePileMapper pileMapper;

    @Override
    public Result<?> generateQRCode(Long pileId) {
        ChargePile pile = pileMapper.selectById(pileId);
        if (pile == null) {
            return Result.error("充电桩不存在");
        }
        
        // 生成二维码内容（包含充电桩ID和验证码）
        String qrCode = "CHARGE_" + pileId + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        pile.setQrCode(qrCode);
        pile.setUpdateTime(LocalDateTime.now());
        pileMapper.updateById(pile);
        
        return Result.success("二维码生成成功", qrCode);
    }

    @Override
    public Result<?> scanQRCode(String qrCode) {
        QueryWrapper<ChargePile> wrapper = new QueryWrapper<>();
        wrapper.eq("qr_code", qrCode);
        ChargePile pile = pileMapper.selectOne(wrapper);
        
        if (pile == null) {
            return Result.error("无效的二维码");
        }
        
        if (pile.getStatus() != 0) {
            return Result.error("充电桩不可用");
        }
        
        return Result.success(pile);
    }

    @Override
    public Result<?> updatePileStatus(Long pileId, Integer status) {
        ChargePile pile = pileMapper.selectById(pileId);
        if (pile == null) {
            return Result.error("充电桩不存在");
        }
        
        pile.setStatus(status);
        pile.setLastHeartbeat(LocalDateTime.now());
        pile.setUpdateTime(LocalDateTime.now());
        pileMapper.updateById(pile);
        
        return Result.success("状态更新成功");
    }
}


