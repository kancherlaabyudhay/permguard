package com.permguard.service;

import com.permguard.entity.*;
import com.permguard.repository.AlertRepository;
import com.permguard.repository.RiskProfileRepository;
import com.permguard.repository.UsageLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

// ================================================================
//  RiskScoringService — weighted rule-based fraud detection
//
//  HOW IT WORKS:
//  1. After every gate event (exit or return), this service runs
//  2. Each rule check adds weighted points (0–100 max)
//  3. New score is blended with historical score (40% old, 60% new)
//  4. If score crosses a threshold → Alert is created
//
//  WEIGHTS:
//  +35  Timeout (no return scan)
//  +20  Overstay > 20% of permitted duration
//  +10  Overstay > 10% of permitted duration
//  +25  Exit between 10 PM – 6 AM (off-hours)
//  +15  3+ exits in same day
//  +10  5+ exits in same week
// ================================================================

@Service
public class RiskScoringService {

    private final RiskProfileRepository riskProfileRepo;
    private final UsageLogRepository    usageLogRepo;
    private final AlertRepository       alertRepo;
    private final EmailService          emailService;

    @Value("${app.fraud.risk-threshold-medium:40.0}")
    private double thresholdMedium;

    @Value("${app.fraud.risk-threshold-high:70.0}")
    private double thresholdHigh;

    @Value("${app.fraud.risk-threshold-critical:90.0}")
    private double thresholdCritical;

    // Admin email for critical alerts — add to application.properties
    @Value("${app.admin.email:admin@permguard.com}")
    private String adminEmail;

    public RiskScoringService(RiskProfileRepository riskProfileRepo,
                               UsageLogRepository usageLogRepo,
                               AlertRepository alertRepo,
                               EmailService emailService) {
        this.riskProfileRepo = riskProfileRepo;
        this.usageLogRepo    = usageLogRepo;
        this.alertRepo       = alertRepo;
        this.emailService    = emailService;
    }

    // ================================================================
    //  Main entry point — call this after every gate scan
    // ================================================================
    @Transactional
    public void evaluateAndUpdateRisk(UsageLog log) {
        User    student    = log.getPermission().getStudent();
        Long    studentId  = student.getUserId();
        double  rawScore   = 0.0;

        // ── RULE 1: Timeout (never returned) ─────────────────
        if (Boolean.TRUE.equals(log.getTimeoutFlag())) {
            rawScore += 35;
        }

        // ── RULE 2: Overstay ──────────────────────────────────
        if (log.getReturnTime() != null && log.getDurationMinutes() != null) {
            Permission perm    = log.getPermission();
            long allowedMins   = ChronoUnit.MINUTES.between(
                perm.getApprovedAt(), perm.getExpiryTime());

            if (allowedMins > 0) {
                double overstayPct = (log.getDurationMinutes() - allowedMins) / (double) allowedMins;
                if (overstayPct > 0.20)      rawScore += 20;
                else if (overstayPct > 0.10) rawScore += 10;
            }
        }

        // ── RULE 3: Off-hours exit (10 PM – 6 AM) ────────────
        if (log.getExitTime() != null) {
            int hour = log.getExitTime().getHour();
            if (hour >= 22 || hour < 6) {
                rawScore += 25;
            }
        }

        // ── RULE 4: High frequency exits today ───────────────
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = LocalDate.now().atTime(LocalTime.MAX);
        long todayExits          = usageLogRepo.countExitsByStudentInWindow(studentId, startOfDay, endOfDay);

        if      (todayExits >= 3) rawScore += 15;

        // ── RULE 5: High frequency exits this week ────────────
        LocalDateTime startOfWeek = LocalDate.now().minusDays(6).atStartOfDay();
        long weekExits            = usageLogRepo.countExitsByStudentInWindow(studentId, startOfWeek, endOfDay);

        if (weekExits >= 5) rawScore += 10;

        // ── CAP raw score ─────────────────────────────────────
        rawScore = Math.min(rawScore, 100.0);

        // ── BLEND with historical score (40% old, 60% new) ───
        RiskProfile profile = riskProfileRepo.findByStudent_UserId(studentId)
                .orElseGet(() -> {
                    RiskProfile p = new RiskProfile(student);
                    return riskProfileRepo.save(p);
                });

        double previousScore = profile.getCurrentScore();
        double blendedScore  = (previousScore * 0.4) + (rawScore * 0.6);
        blendedScore         = Math.min(blendedScore, 100.0);

        // ── UPDATE counters ───────────────────────────────────
        if (Boolean.TRUE.equals(log.getTimeoutFlag())) {
            profile.setTotalTimeouts(profile.getTotalTimeouts() + 1);
        }
        if (log.getDurationMinutes() != null) {
            Permission perm = log.getPermission();
            long allowed = perm.getApprovedAt() != null
                ? ChronoUnit.MINUTES.between(perm.getApprovedAt(), perm.getExpiryTime()) : 0;
            if (log.getDurationMinutes() > allowed) {
                profile.setTotalOverstays(profile.getTotalOverstays() + 1);
            }
        }
        if (log.getExitTime() != null) {
            int hour = log.getExitTime().getHour();
            if (hour >= 22 || hour < 6) {
                profile.setTotalOffHourExits(profile.getTotalOffHourExits() + 1);
            }
        }

        profile.setCurrentScore(blendedScore);
        riskProfileRepo.save(profile);

        // ── TRIGGER ALERTS if thresholds crossed ─────────────
        triggerAlertIfNeeded(student, blendedScore, previousScore);
    }

    // ================================================================
    //  Create alert only when score crosses a threshold boundary
    //  (avoids spamming alerts on every scan)
    // ================================================================
    private void triggerAlertIfNeeded(User student, double newScore, double oldScore) {
        Alert.RiskLevel level   = null;
        String          message = null;

        if (newScore >= thresholdCritical && oldScore < thresholdCritical) {
            level   = Alert.RiskLevel.HIGH;
            message = String.format("CRITICAL: %s risk score reached %.1f/100",
                student.getFullName(), newScore);

            // Also send email for critical alerts
            emailService.sendAdminAlertEmail(adminEmail, student.getFullName(), newScore, message);

        } else if (newScore >= thresholdHigh && oldScore < thresholdHigh) {
            level   = Alert.RiskLevel.HIGH;
            message = String.format("HIGH RISK: %s score reached %.1f/100",
                student.getFullName(), newScore);

        } else if (newScore >= thresholdMedium && oldScore < thresholdMedium) {
            level   = Alert.RiskLevel.MEDIUM;
            message = String.format("MEDIUM RISK: %s score reached %.1f/100",
                student.getFullName(), newScore);
        }

        if (level != null) {
            alertRepo.save(new Alert(student, level, message));
        }
    }
}