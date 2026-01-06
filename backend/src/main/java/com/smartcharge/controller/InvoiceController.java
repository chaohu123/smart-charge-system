package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.common.PageResult;
import com.smartcharge.service.InvoiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/invoice")
public class InvoiceController {
    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/apply")
    public Result<?> applyInvoice(@RequestHeader("Authorization") String token,
                                  @RequestParam Long orderId,
                                  @RequestParam String type,
                                  @RequestParam String title,
                                  @RequestParam(required = false) String taxNumber,
                                  @RequestParam(required = false) String content) {
        return invoiceService.applyInvoice(token, orderId, type, title, taxNumber, content);
    }

    @GetMapping("/list")
    public Result<?> getInvoiceList(@RequestHeader("Authorization") String token,
                                    @RequestParam(defaultValue = "1") Integer current,
                                    @RequestParam(defaultValue = "10") Integer size) {
        return invoiceService.getInvoiceList(token, current, size);
    }

    @GetMapping("/{id}")
    public Result<?> getInvoiceDetail(@RequestHeader("Authorization") String token,
                                      @PathVariable Long id) {
        return invoiceService.getInvoiceDetail(token, id);
    }

    @GetMapping("/download/{id}")
    public Result<?> downloadInvoice(@RequestHeader("Authorization") String token,
                                     @PathVariable Long id) {
        return invoiceService.downloadInvoice(token, id);
    }
}

