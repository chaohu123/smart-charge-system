package com.smartcharge.service;

import com.smartcharge.common.Result;
import org.springframework.web.multipart.MultipartFile;

public interface FaultReportService {
    Result<?> reportFault(String token, Long stationId, Long pileId, String faultType, String description, MultipartFile[] images);
    Result<?> getFaultReportList(String token);
}

