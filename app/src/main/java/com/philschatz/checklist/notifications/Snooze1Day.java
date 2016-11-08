package com.philschatz.checklist.notifications;


public class Snooze1Day extends AbstractSnoozeNotificationService {
    public Snooze1Day() {
        super(1 * 24 * 60 * 60 * 1000); // 1 day in millis
    }
}
