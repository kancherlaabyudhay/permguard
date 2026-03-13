package com.permguard.service;

import com.permguard.entity.Permission;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

// ================================================================
//  EmailService — async email notifications for permission events
//  @Async means these never block the HTTP response thread
// ================================================================

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ── APPROVED ─────────────────────────────────────────────
    @Async
    public void sendApprovalEmail(Permission permission, String qrBase64) {
        String to      = permission.getStudent().getEmail();
        String name    = permission.getStudent().getFullName();
        String expiry  = permission.getExpiryTime().format(FMT);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("[PermGuard] Your permission has been APPROVED");
        msg.setText(
            "Dear " + name + ",\n\n" +
            "Your permission request has been approved.\n\n" +
            "Type    : " + permission.getType() + "\n" +
            "Valid until: " + expiry + "\n\n" +
            "Your QR token: " + permission.getQrToken() + "\n\n" +
            "Show this token at the gate for scanning.\n\n" +
            "Regards,\nPermGuard System"
        );
        mailSender.send(msg);
    }

    // ── REJECTED ─────────────────────────────────────────────
    @Async
    public void sendRejectionEmail(Permission permission) {
        String to   = permission.getStudent().getEmail();
        String name = permission.getStudent().getFullName();

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("[PermGuard] Your permission request was NOT approved");
        msg.setText(
            "Dear " + name + ",\n\n" +
            "Your permission request for \"" + permission.getType() + "\" has been rejected.\n\n" +
            "If you believe this is an error, please contact your faculty.\n\n" +
            "Regards,\nPermGuard System"
        );
        mailSender.send(msg);
    }

    // ── HIGH RISK ALERT ───────────────────────────────────────
    @Async
    public void sendAdminAlertEmail(String adminEmail, String studentName, double riskScore, String reason) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(adminEmail);
        msg.setSubject("[PermGuard] 🚨 HIGH RISK ALERT — " + studentName);
        msg.setText(
            "FRAUD ALERT\n\n" +
            "Student : " + studentName + "\n" +
            "Risk Score: " + String.format("%.1f", riskScore) + " / 100\n" +
            "Reason  : " + reason + "\n\n" +
            "Please review this student on the admin dashboard.\n\n" +
            "PermGuard System"
        );
        mailSender.send(msg);
    }
}