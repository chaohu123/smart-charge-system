package com.smartcharge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;
import com.smartcharge.common.ResultCode;
import com.smartcharge.config.JwtConfig;
import com.smartcharge.entity.PaymentRecord;
import com.smartcharge.entity.User;
import com.smartcharge.entity.WithdrawRecord;
import com.smartcharge.mapper.PaymentRecordMapper;
import com.smartcharge.mapper.UserMapper;
import com.smartcharge.mapper.WithdrawRecordMapper;
import com.smartcharge.service.WithdrawService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WithdrawServiceImpl implements WithdrawService {
    @Autowired
    private WithdrawRecordMapper withdrawRecordMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PaymentRecordMapper paymentRecordMapper;
    
    @Autowired
    private JwtConfig jwtConfig;

    @Override
    @Transactional
    public Result<?> applyWithdraw(String token, BigDecimal amount) {
        try {
            Long userId = getUserIdFromToken(token);
            User user = userMapper.selectById(userId);
            
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 检查余额是否充足
            if (user.getBalance().compareTo(amount) < 0) {
                return Result.error("账户余额不足");
            }
            
            // 检查最小提现金额
            if (amount.compareTo(new BigDecimal("10")) < 0) {
                return Result.error("提现金额不能少于10元");
            }
            
            // 创建提现记录
            WithdrawRecord withdrawRecord = new WithdrawRecord();
            withdrawRecord.setUserId(userId);
            withdrawRecord.setAmount(amount);
            withdrawRecord.setWithdrawNo("WD" + System.currentTimeMillis());
            withdrawRecord.setStatus(0); // 0-待审核
            withdrawRecord.setCreateTime(LocalDateTime.now());
            withdrawRecord.setUpdateTime(LocalDateTime.now());
            withdrawRecordMapper.insert(withdrawRecord);
            
            // 冻结余额：从账户余额中减去提现金额
            user.setBalance(user.getBalance().subtract(amount));
            userMapper.updateById(user);
            
            // 创建支付记录（提现申请）
            PaymentRecord paymentRecord = new PaymentRecord();
            paymentRecord.setUserId(userId);
            paymentRecord.setOrderId(null);
            paymentRecord.setType(4); // 4-提现
            paymentRecord.setAmount(amount);
            paymentRecord.setPaymentMethod(0); // 提现默认使用余额
            paymentRecord.setTransactionNo(withdrawRecord.getWithdrawNo());
            paymentRecord.setStatus(0); // 0-待支付（待审核）
            paymentRecord.setCreateTime(LocalDateTime.now());
            paymentRecordMapper.insert(paymentRecord);
            
            log.info("用户申请提现，用户ID: {}, 金额: {}, 提现单号: {}", userId, amount, withdrawRecord.getWithdrawNo());
            
            return Result.success("提现申请已提交，等待审核", withdrawRecord);
        } catch (Exception e) {
            log.error("申请提现失败", e);
            return Result.error("申请提现失败");
        }
    }

    @Override
    public Result<?> getWithdrawRecords(String token, Integer current, Integer size) {
        try {
            Long userId = getUserIdFromToken(token);
            
            QueryWrapper<WithdrawRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.orderByDesc("create_time");
            
            Page<WithdrawRecord> page = new Page<>(current, size);
            Page<WithdrawRecord> result = withdrawRecordMapper.selectPage(page, wrapper);
            
            PageResult<WithdrawRecord> pageResult = new PageResult<>(
                result.getTotal(),
                result.getRecords(),
                current,
                size
            );
            
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("查询提现记录失败", e);
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "Token无效");
        }
    }

    @Override
    public Result<?> getWithdrawList(Integer status, String withdrawNo, String userPhone, Integer current, Integer size) {
        try {
            QueryWrapper<WithdrawRecord> wrapper = new QueryWrapper<>();
            if (status != null) {
                wrapper.eq("status", status);
            }
            if (withdrawNo != null && !withdrawNo.trim().isEmpty()) {
                wrapper.like("withdraw_no", withdrawNo);
            }
            // 如果提供了用户手机号，需要先查询用户ID
            if (userPhone != null && !userPhone.trim().isEmpty()) {
                QueryWrapper<User> userWrapper = new QueryWrapper<>();
                userWrapper.eq("phone", userPhone);
                User user = userMapper.selectOne(userWrapper);
                if (user != null) {
                    wrapper.eq("user_id", user.getId());
                } else {
                    // 如果用户不存在，返回空结果
                    wrapper.eq("user_id", -1);
                }
            }
            wrapper.orderByDesc("create_time");
            
            Page<WithdrawRecord> page = new Page<>(current, size);
            Page<WithdrawRecord> result = withdrawRecordMapper.selectPage(page, wrapper);
            
            // 关联查询用户信息
            result.getRecords().forEach(record -> {
                if (record.getUserId() != null) {
                    User user = userMapper.selectById(record.getUserId());
                    if (user != null) {
                        // 使用反射或创建一个扩展类来添加用户信息
                        // 这里我们使用一个简单的方法：创建一个Map来存储扩展信息
                        // 但由于返回的是WithdrawRecord对象，我们需要创建一个VO类
                        // 为了简化，我们直接在WithdrawRecord中添加临时字段（不推荐，但可以工作）
                        // 更好的方式是创建WithdrawRecordVO，但为了快速实现，我们使用Map
                    }
                }
            });
            
            // 转换为包含用户信息的Map列表
            List<Map<String, Object>> recordsWithUser = result.getRecords().stream().map(record -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", record.getId());
                map.put("userId", record.getUserId());
                map.put("amount", record.getAmount());
                map.put("withdrawNo", record.getWithdrawNo());
                map.put("status", record.getStatus());
                map.put("bankName", record.getBankName());
                map.put("bankAccount", record.getBankAccount());
                map.put("remark", record.getRemark());
                map.put("adminId", record.getAdminId());
                map.put("rejectReason", record.getRejectReason());
                map.put("createTime", record.getCreateTime());
                map.put("updateTime", record.getUpdateTime());
                
                // 查询用户信息
                if (record.getUserId() != null) {
                    User user = userMapper.selectById(record.getUserId());
                    if (user != null) {
                        map.put("userPhone", user.getPhone());
                        map.put("userNickname", user.getNickname() != null && !user.getNickname().trim().isEmpty() 
                            ? user.getNickname() : user.getPhone());
                    } else {
                        map.put("userPhone", "--");
                        map.put("userNickname", "用户已删除");
                    }
                } else {
                    map.put("userPhone", "--");
                    map.put("userNickname", "--");
                }
                
                return map;
            }).collect(Collectors.toList());
            
            PageResult<Map<String, Object>> pageResult = new PageResult<>(
                result.getTotal(),
                recordsWithUser,
                current,
                size
            );
            
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("查询提现列表失败", e);
            return Result.error("查询失败");
        }
    }

    @Override
    @Transactional
    public Result<?> auditWithdraw(Long id, Integer status, String reason) {
        try {
            WithdrawRecord withdrawRecord = withdrawRecordMapper.selectById(id);
            if (withdrawRecord == null) {
                return Result.error("提现记录不存在");
            }
            
            if (withdrawRecord.getStatus() != 0) {
                return Result.error("该提现记录已审核");
            }
            
            User user = userMapper.selectById(withdrawRecord.getUserId());
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            if (status == 1) {
                // 审核通过：余额已经冻结，不需要再操作
                withdrawRecord.setStatus(1); // 1-审核通过
                withdrawRecord.setUpdateTime(LocalDateTime.now());
                
                // 更新支付记录状态为已支付
                QueryWrapper<PaymentRecord> wrapper = new QueryWrapper<>();
                wrapper.eq("transaction_no", withdrawRecord.getWithdrawNo());
                PaymentRecord paymentRecord = paymentRecordMapper.selectOne(wrapper);
                if (paymentRecord != null) {
                    paymentRecord.setStatus(1); // 1-已支付
                    paymentRecordMapper.updateById(paymentRecord);
                }
                
                log.info("提现审核通过，提现ID: {}, 用户ID: {}, 金额: {}", id, withdrawRecord.getUserId(), withdrawRecord.getAmount());
            } else if (status == 2) {
                // 审核拒绝：退回冻结金额
                withdrawRecord.setStatus(2); // 2-审核拒绝
                withdrawRecord.setRejectReason(reason);
                withdrawRecord.setUpdateTime(LocalDateTime.now());
                
                // 退回余额
                user.setBalance(user.getBalance().add(withdrawRecord.getAmount()));
                userMapper.updateById(user);
                
                // 更新支付记录状态为已退款
                QueryWrapper<PaymentRecord> wrapper = new QueryWrapper<>();
                wrapper.eq("transaction_no", withdrawRecord.getWithdrawNo());
                PaymentRecord paymentRecord = paymentRecordMapper.selectOne(wrapper);
                if (paymentRecord != null) {
                    paymentRecord.setStatus(2); // 2-已退款
                    paymentRecordMapper.updateById(paymentRecord);
                }
                
                // 创建退款记录
                PaymentRecord refundRecord = new PaymentRecord();
                refundRecord.setUserId(withdrawRecord.getUserId());
                refundRecord.setOrderId(null);
                refundRecord.setType(3); // 3-退款
                refundRecord.setAmount(withdrawRecord.getAmount());
                refundRecord.setPaymentMethod(0);
                refundRecord.setTransactionNo("REFUND_" + withdrawRecord.getWithdrawNo());
                refundRecord.setStatus(1); // 1-已支付（已退回）
                refundRecord.setCreateTime(LocalDateTime.now());
                paymentRecordMapper.insert(refundRecord);
                
                log.info("提现审核拒绝，提现ID: {}, 用户ID: {}, 金额: {}, 原因: {}", id, withdrawRecord.getUserId(), withdrawRecord.getAmount(), reason);
            } else {
                return Result.error("审核状态无效");
            }
            
            withdrawRecordMapper.updateById(withdrawRecord);
            
            return Result.success("审核成功");
        } catch (Exception e) {
            log.error("审核提现失败", e);
            return Result.error("审核失败");
        }
    }

    private Long getUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtConfig.parseToken(token).get("userId", Long.class);
    }
}

