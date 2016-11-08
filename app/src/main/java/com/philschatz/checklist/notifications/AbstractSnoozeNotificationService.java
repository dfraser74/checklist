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
        String newTime = ToDoItem.fromLong(System.currentTimeMillis() + mMillisToSnooze);
        Map<String, Object> props = new HashMap<>();
        props.put("remindAt", newTime);
        return props;
    }

}
