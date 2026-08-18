package com.tarumt.resorts.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Adjustable in-memory clock used only by the Loyalty and Rewards module.
 * The clock keeps moving after an offset is applied and resets whenever the
 * application restarts.
 */
public final class LoyaltyClock {

    private static Duration demoOffset = Duration.ZERO;
    private static boolean demoMode;

    private LoyaltyClock() {
    }

    public static synchronized LocalDateTime now() {
        LocalDateTime realTime = LocalDateTime.now();
        return demoMode ? realTime.plus(demoOffset) : realTime;
    }

    public static synchronized LocalDate today() {
        return now().toLocalDate();
    }

    /** Changes only the Loyalty date and retains the current time of day. */
    public static synchronized void setDate(LocalDate targetDate) {

        if (targetDate == null) {
            throw new IllegalArgumentException(
                    "Target date cannot be null.");
        }

        LocalDateTime targetTime = targetDate.atTime(now().toLocalTime());
        demoOffset = Duration.between(LocalDateTime.now(), targetTime);
        demoMode = true;
    }

    public static synchronized void resetToSystemTime() {
        demoOffset = Duration.ZERO;
        demoMode = false;
    }

    public static synchronized boolean isDemoMode() {
        return demoMode;
    }
}
