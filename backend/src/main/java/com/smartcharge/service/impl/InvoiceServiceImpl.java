package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;
import com.smartcharge.common.ResultCode;
import com.smartcharge.config.JwtConfig;
import com.smartcharge.entity.Invoice;
import com.smartcharge.entity.Order;
import com.smartcharge.mapper.InvoiceMapper;
import com.smartcharge.mapper.OrderMapper;
import com.smartcharge.service.InvoiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class InvoiceServiceImpl implements InvoiceService {
    @Autowired
    private InvoiceMapper invoiceMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public Result<?> applyInvoice(String token, Long orderId, String type, String title, String taxNumber, String content) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Order order = orderMapper.selectById(orderId);
            if (order == null || !order.getUserId().equals(userId)) {
                return Result.error(ResultCode.ORDER_NOT_FOUND.getCode(), ResultCode.ORDER_NOT_FOUND.getMessage());
            }
            
            if (order.getStatus() != 2) {
                return Result.error("只能为已完成的订单申请发票");
            }
            
            // 检查是否已申请发票
            QueryWrapper<Invoice> wrapper = new QueryWrapper<>();
            wrapper.eq("order_id", orderId);
            Invoice existInvoice = invoiceMapper.selectOne(wrapper);
            if (existInvoice != null) {
                return Result.error("该订单已申请发票");
            }
            
            Invoice invoice = new Invoice();
            invoice.setInvoiceNo("INV" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
            invoice.setUserId(userId);
            invoice.setOrderId(orderId);
            invoice.setType(type);
            invoice.setTitle(title);
            invoice.setTaxNumber(taxNumber);
            invoice.setAmount(order.getTotalAmount());
            invoice.setContent(content != null ? content : "充电服务费");
            invoice.setStatus("待开票");
            invoice.setCreateTime(LocalDateTime.now());
            invoice.setUpdateTime(LocalDateTime.now());
            
            invoiceMapper.insert(invoice);
            
            // 这里应该调用发票生成服务，生成PDF发票
            // 简化处理，直接设为已开票
            invoice.setStatus("已开票");
            invoice.setFileUrl("/files/invoice/" + invoice.getInvoiceNo() + ".pdf");
            invoice.setUpdateTime(LocalDateTime.now());
            invoiceMapper.updateById(invoice);
            
            return Result.success("发票申请成功", invoice);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> getInvoiceList(String token, Integer current, Integer size) {
        try {
            Long userId = getUserIdFromToken(token);
            
            QueryWrapper<Invoice> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.orderByDesc("create_time");
            
            Page<Invoice> page = new Page<>(current, size);
            Page<Invoice> result = invoiceMapper.selectPage(page, wrapper);
            
            PageResult<Invoice> pageResult = new PageResult<>(
                result.getTotal(),
                result.getRecords(),
                current,
                size
            );
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> getInvoiceDetail(String token, Long id) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Invoice invoice = invoiceMapper.selectById(id);
            if (invoice == null || !invoice.getUserId().equals(userId)) {
                return Result.error("发票不存在");
            }
            return Result.success(invoice);
        } catch (Exception e) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> downloadInvoice(String token, Long id) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Invoice invoice = invoiceMapper.selectById(id);
            if (invoice == null || !invoice.getUserId().equals(userId)) {
                return Result.error("发票不存在");
            }
            
            if (!"已开票".equals(invoice.getStatus())) {
                return Result.error("发票尚未开具");
            }
            
            return Result.success("下载成功", invoice.getFileUrl());
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

