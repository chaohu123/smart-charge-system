package com.smartcharge.controller;

import com.smartcharge.common.Result;
import com.smartcharge.service.ChargePileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/pile")
public class ChargePileController {
    @Autowired
    private ChargePileService pileService;

    @PostMapping("/{id}/qrcode")
    public Result<?> generateQRCode(@PathVariable Long id) {
        return pileService.generateQRCode(id);
    }

    @PostMapping("/scan")
    public Result<?> scanQRCode(@RequestParam String qrCode) {
        return pileService.scanQRCode(qrCode);
    }

    @PostMapping("/{id}/status")
    public Result<?> updatePileStatus(@PathVariable Long id, @RequestParam Integer status) {
        return pileService.updatePileStatus(id, status);
    }
}


