package com.smartcharge.service;

import com.smartcharge.common.Result;

public interface ChargePileService {
    Result<?> generateQRCode(Long pileId);
    Result<?> scanQRCode(String qrCode);
    Result<?> updatePileStatus(Long pileId, Integer status);
}


