package com.permguard.controller;

import com.permguard.service.GateScanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// ================================================================
//  GateScanController
//
//  POST /gate/scan          → scan QR token at gate
//  GET  /gate/history/{id}  → get scan history for a permission
// ================================================================

@RestController
@RequestMapping("/gate")
public class GateScanController {

    private final GateScanService gateScanService;

    public GateScanController(GateScanService gateScanService) {
        this.gateScanService = gateScanService;
    }

    // ----------------------------------------------------------------
    // POST /gate/scan — Security staff scans QR code
    // Body: { "qrToken": "abc123...", "scanType": "EXIT" }
    // ----------------------------------------------------------------
    @PostMapping("/scan")
    public ResponseEntity<?> scan(@RequestBody Map<String, String> body,
                                  Authentication auth) {
        String qrToken  = body.get("qrToken");
        String scanType = body.getOrDefault("scanType", "EXIT");

        if (qrToken == null || qrToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "qrToken is required"));
        }

        Map<String, Object> result = gateScanService.scanQrToken(
                qrToken, auth.getName(), scanType);

        // Return 200 with valid/invalid in body (let frontend decide UI)
        return ResponseEntity.ok(result);
    }

    // ----------------------------------------------------------------
    // GET /gate/history/{id} — View scan history for a permission
    // ----------------------------------------------------------------
    @GetMapping("/history/{id}")
    public ResponseEntity<?> getScanHistory(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(gateScanService.getScanHistory(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}