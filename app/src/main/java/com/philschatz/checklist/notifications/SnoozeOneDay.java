package com.philschatz.checklist.notifications;


public class SnoozeOneDay extends AbstractSnoozeNotificationService {
    public SnoozeOneDay() {
        super(1 * 24 * 60 * 60 * 1000); // 1 day in millis
    }
}
