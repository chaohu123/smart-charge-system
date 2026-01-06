package com.smartcharge.service;

import com.smartcharge.common.PageResult;
import com.smartcharge.common.Result;

public interface InvoiceService {
    Result<?> applyInvoice(String token, Long orderId, String type, String title, String taxNumber, String content);
    Result<?> getInvoiceList(String token, Integer current, Integer size);
    Result<?> getInvoiceDetail(String token, Long id);
    Result<?> downloadInvoice(String token, Long id);
}

