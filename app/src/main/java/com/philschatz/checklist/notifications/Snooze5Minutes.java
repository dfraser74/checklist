package com.philschatz.checklist.notifications;

public class Snooze5Minutes extends AbstractSnoozeNotificationService {
    public Snooze5Minutes() {
        super(5 * 60 * 1000); // 5 minutes
    }
}
