package com.permguard.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.permguard.entity.Permission;
import com.permguard.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// ================================================================
//  QrCodeService — generates QR codes for approved permissions
// ================================================================

@Service
public class QrCodeService {

    private final PermissionRepository permissionRepository;

    @Value("${app.upload.dir:uploads/qr-codes}")
    private String uploadDir;

    @Value("${app.qr.width:300}")
    private int qrWidth;

    @Value("${app.qr.height:300}")
    private int qrHeight;

    public QrCodeService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    // ----------------------------------------------------------------
    // Generate QR code for an approved permission
    // Returns Base64-encoded PNG image
    // ----------------------------------------------------------------
    @Transactional
    public String generateQrForPermission(Long permissionId) throws WriterException, IOException {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionId));

        if (permission.getStatus() != Permission.Status.APPROVED) {
            throw new RuntimeException("QR code can only be generated for APPROVED permissions");
        }

        // Check if QR already generated
        if (permission.getQrToken() != null && !permission.getQrToken().isEmpty()) {
            return permission.getQrToken();
        }

        // Check not expired
        if (permission.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Permission has expired");
        }

        // Generate unique token
        String token = UUID.randomUUID().toString().replace("-", "");

        // QR content — JSON-like payload for gate scanner
        String qrContent = String.format(
            "{\"permId\":%d,\"student\":\"%s\",\"roll\":\"%s\",\"type\":\"%s\"," +
            "\"expiry\":\"%s\",\"token\":\"%s\"}",
            permission.getId(),
            permission.getStudent().getFullName(),
            permission.getStudent().getRollNumber(),
            permission.getType(),
            permission.getExpiryTime().toString(),
            token
        );

        // Generate QR image
        byte[] qrBytes = generateQrBytes(qrContent);
        String base64Qr = Base64.getEncoder().encodeToString(qrBytes);

        // Save QR image to file
        saveQrToFile(token, qrBytes);

        // Save token to permission
        permission.setQrToken(token);
        permissionRepository.save(permission);

        return base64Qr;
    }

    // ----------------------------------------------------------------
    // Get existing QR code as Base64
    // ----------------------------------------------------------------
    public String getQrBase64(Long permissionId) throws IOException {
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionId));

        if (permission.getQrToken() == null) {
            throw new RuntimeException("QR code not yet generated for this permission");
        }

        // Read from file
        Path filePath = Paths.get(uploadDir, permission.getQrToken() + ".png");
        if (Files.exists(filePath)) {
            byte[] bytes = Files.readAllBytes(filePath);
            return Base64.getEncoder().encodeToString(bytes);
        }

        // Regenerate if file missing
        try {
            return generateQrForPermission(permissionId);
        } catch (WriterException e) {
            throw new RuntimeException("Failed to regenerate QR code", e);
        }
    }

    // ----------------------------------------------------------------
    // Generate QR bytes using ZXing
    // ----------------------------------------------------------------
    private byte[] generateQrBytes(String content) throws WriterException, IOException {
        QRCodeWriter writer = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE,
                qrWidth, qrHeight, hints);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);
        return out.toByteArray();
    }

    // ----------------------------------------------------------------
    // Save QR image to disk
    // ----------------------------------------------------------------
    private void saveQrToFile(String token, byte[] bytes) throws IOException {
        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        Path filePath = dir.resolve(token + ".png");
        Files.write(filePath, bytes);
    }
}