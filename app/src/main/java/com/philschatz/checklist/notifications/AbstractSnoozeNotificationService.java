package com.philschatz.checklist.notifications;

import com.philschatz.checklist.ToDoItem;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractSnoozeNotificationService extends AbstractNotificationService {
    private long mMillisToSnooze;

    public AbstractSnoozeNotificationService(long millisToSnooze) {
        super("AbstractSnoozeNotificationService");
        mMillisToSnooze = millisToSnooze;
    }

    protected Map<String, Object> updatedKeys(ToDoItem item) {
        Date newTime = new Date(System.currentTimeMillis() + mMillisToSnooze);
        Map<String, Object> props = new HashMap<>();
        // TODO: Convert this field to a string
        props.put("remindAt", newTime);
        return props;
    }

}
