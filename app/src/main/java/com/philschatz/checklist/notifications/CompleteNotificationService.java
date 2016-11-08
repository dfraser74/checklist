package com.philschatz.checklist.notifications;

import com.philschatz.checklist.ToDoItem;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CompleteNotificationService extends AbstractNotificationService {

    public CompleteNotificationService() {
        super("CompleteNotificationService");
    }

    protected Map<String, Object> updatedKeys(ToDoItem item) {
        Map<String, Object> props = new HashMap<>();
        // TODO: Convert this field to a string
        props.put("completedAt", new Date());

        // Leave the following line commented so 1. we can remember when the reminder was originally set and 2. if the user presses undo
        // props.put("remindAt", null);
        return props;
    }

}
