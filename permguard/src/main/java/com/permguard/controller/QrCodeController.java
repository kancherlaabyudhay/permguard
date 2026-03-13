package com.permguard.controller;

import com.permguard.service.QrCodeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// ================================================================
//  QrCodeController
//
//  GET  /qr/{permissionId}          → get QR as Base64 (student)
//  POST /qr/{permissionId}/generate → generate QR (auto on approve)
// ================================================================

@RestController
@RequestMapping("/qr")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    // ----------------------------------------------------------------
    // POST /qr/{id}/generate — Generate QR for approved permission
    // ----------------------------------------------------------------
    @PostMapping("/{id}/generate")
    public ResponseEntity<?> generateQr(@PathVariable Long id,
                                        Authentication auth) {
        try {
            String base64 = qrCodeService.generateQrForPermission(id);
            return ResponseEntity.ok(Map.of(
                "permissionId", id,
                "qrBase64", base64,
                "message", "QR code generated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    // GET /qr/{id} — Get existing QR as Base64
    // ----------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<?> getQr(@PathVariable Long id) {
        try {
            String base64 = qrCodeService.getQrBase64(id);
            return ResponseEntity.ok(Map.of(
                "permissionId", id,
                "qrBase64", base64,
                "imageUrl", "data:image/png;base64," + base64
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}