package com.philschatz.checklist.notifications;

public class Snooze2Minutes extends AbstractSnoozeNotificationService {
    public Snooze2Minutes() {
        super(2 * 60 * 1000); // 2 minutes
    }
}
