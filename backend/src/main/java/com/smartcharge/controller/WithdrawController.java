package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.service.WithdrawService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/withdraw")
public class WithdrawController {
    @Autowired
    private WithdrawService withdrawService;

    @PostMapping("/apply")
    public Result<?> applyWithdraw(@RequestHeader("Authorization") String token,
                                   @RequestParam java.math.BigDecimal amount) {
        return withdrawService.applyWithdraw(token, amount);
    }

    @GetMapping("/records")
    public Result<?> getWithdrawRecords(@RequestHeader("Authorization") String token,
                                       @RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size) {
        return withdrawService.getWithdrawRecords(token, current, size);
    }

    @GetMapping("/list")
    public Result<?> getWithdrawList(@RequestParam(required = false) Integer status,
                                    @RequestParam(required = false) String withdrawNo,
                                    @RequestParam(required = false) String userPhone,
                                    @RequestParam(defaultValue = "1") Integer current,
                                    @RequestParam(defaultValue = "10") Integer size) {
        return withdrawService.getWithdrawList(status, withdrawNo, userPhone, current, size);
    }

    @PostMapping("/audit")
    public Result<?> auditWithdraw(@RequestParam Long id,
                                  @RequestParam Integer status,
                                  @RequestParam(required = false) String reason) {
        return withdrawService.auditWithdraw(id, status, reason);
    }
}

