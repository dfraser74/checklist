package com.philschatz.checklist.notifications;

public class Snooze20Minutes extends AbstractSnoozeNotificationService {
    public Snooze20Minutes() {
        super(20 * 60 * 1000); // 20 minutes
    }
}
