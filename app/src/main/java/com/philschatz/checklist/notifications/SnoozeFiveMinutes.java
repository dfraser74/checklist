package com.philschatz.checklist.notifications;

public class SnoozeFiveMinutes extends AbstractSnoozeNotificationService {
    public SnoozeFiveMinutes() {
        super(5 * 60 * 1000); // 5 minutes
    }
}
