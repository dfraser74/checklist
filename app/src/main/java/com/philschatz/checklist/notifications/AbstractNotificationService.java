package com.philschatz.checklist.notifications;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.philschatz.checklist.Const;
import com.philschatz.checklist.MainActivity;
import com.philschatz.checklist.ToDoItem;
import com.philschatz.checklist.TodoNotificationService;

import java.util.Map;

/**
 * Little helper for handling Notification Actions.
 *
 * Given a ToDoItem subclasses define what fields should be updated as a result of the action.
 *
 * Example actions are: Mark item as complete, remind me in 5 minutes, remind me in 1 day
 */
public abstract class AbstractNotificationService extends IntentService {

    // TODO: Make this an activity per https://developer.android.com/guide/topics/ui/notifiers/notifications.html#Compatibility
    // See "For example, if you want to use addAction() to provide a control that stops and starts media playback, first implement this control in an Activity in your app."
    public AbstractNotificationService(String name) {
        super(name);
    }

    protected abstract Map<String, Object> updatedKeys(ToDoItem item);

    @Override
    protected final void onHandleIntent(Intent intent) {
        String listKey = intent.getStringExtra(Const.TODOLISTKEY);
        String itemKey = intent.getStringExtra(Const.TODOITEMKEY);
        ToDoItem item = (ToDoItem) intent.getSerializableExtra(Const.TODOITEMSNAPSHOT);
        int hashCode = intent.getIntExtra(Const.NOTIFICATIONID, itemKey.hashCode());

        if (listKey == null) { throw new RuntimeException("missing: " + Const.TODOLISTKEY); }
        if (itemKey == null) { throw new RuntimeException("missing: " + Const.TODOITEMKEY); }
        if (item    == null) { throw new RuntimeException("missing: " + Const.TODOITEMSNAPSHOT); }

        DatabaseReference dbRef = MainActivity.getListItemReference(listKey, itemKey);

        Map<String, Object> props = updatedKeys(item);

        if (props.isEmpty()) {
            throw new RuntimeException("BUG? None of the props changed");
        }

        dbRef.updateChildren(props); // TODO: Add a handler for success/failure

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(hashCode);
        Log.d(this.getClass().getSimpleName(), "cancelled notification");
    }

}
