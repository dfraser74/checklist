package com.philschatz.checklist;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Date;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * This is a listener for Firebase events and updates the desktop notifications
 */
class ToDoItemAlarmListener implements ChildEventListener {
    private static String TAG = "ToDoItem Alarm Listener";

    private Context mContext;
    private ToDoList mList;
    private String mListKey;

    public ToDoItemAlarmListener(Context c, ToDoList list, String listKey) {

        mContext = c;
        mList = list;
        mListKey = listKey;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//        // These are here only for migration reasons
//        ToDoItem item = dataSnapshot.getValue(ToDoItem.class);
//        boolean changed = false;
//        if (item.createdAt != null) {
//            item.legacySetCreatedAt(item.createdAt);
//            item.createdAt = null;
//            changed = true;
//        }
//        if (item.completedAt != null) {
//            item.legacySetCompletedAt(item.completedAt);
//            item.completedAt = null;
//            changed = true;
//        }
//        if (item.remindAt != null) {
//            item.legacySetRemindAt(item.remindAt);
//            item.remindAt = null;
//            changed = true;
//        }
//        if (changed) {
//            dataSnapshot.getRef().setValue(item);
//        }
//
        setAlarmIfNecessary(dataSnapshot.getValue(ToDoItem.class), dataSnapshot.getRef());
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        setAlarmIfNecessary(dataSnapshot.getValue(ToDoItem.class), dataSnapshot.getRef());
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        setAlarmIfNecessary(dataSnapshot.getValue(ToDoItem.class), dataSnapshot.getRef());
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        // Ignore moves
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.d(TAG, "Database Disconnect");
    }

    private void setAlarmIfNecessary(ToDoItem item, DatabaseReference dbRef) {
        // Here are the possible states:
        //  hasReminder &&  hasAlarm -> updateAlarm()
        //  hasReminder && !hasAlarm -> createAlarm()
        // !hasReminder &&  hasAlarm -> deleteAlarm()
        // !hasReminder && !hasAlarm -> nothing
        Intent i = new Intent(mContext, TodoNotificationService.class);
        int hashCode = dbRef.getKey().hashCode();
        boolean hasAlarmForItem = hasAlarm(i, hashCode);
        long remindAt = item.remindAt();

        // Only care about reminders when item is not complete
        if (item.isComplete()) {
            remindAt = 0L;
        }


        // Set all the fields for the intent (used by createAlarm and updateAlarm)
        i.putExtra(Const.TODOITEMSNAPSHOT, item);
        i.putExtra(Const.TODOITEMKEY, dbRef.getKey());
        i.putExtra(Const.TODOLISTSNAPSHOT, mList);
        i.putExtra(Const.TODOLISTKEY, mListKey);

        NotificationManager manager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);

        if (remindAt != 0L && !hasAlarmForItem) {
            // If the reminder was set in the past then make sure it appears
            // (just in case; this could occur when undoing an accidentally-completed item)
            if (remindAt < System.currentTimeMillis()) {
                remindAt = System.currentTimeMillis() + 10 * 1000;
            }
            // If there was a desktop notification for this item, then cancel it
            manager.cancel(hashCode);

            createAlarm(i, hashCode, remindAt);
        } else if (remindAt == 0L && hasAlarmForItem) {
            // If there was a desktop notification for this item, then cancel it
            manager.cancel(hashCode);
            deleteAlarm(i, hashCode);
        } else if (remindAt != 0L && hasAlarmForItem) {
            // If there was a desktop notification for this item, then cancel it
            manager.cancel(hashCode);
            updateAlarm(i, hashCode, remindAt);
        } else {
            // Do nothing because the alarm state did not change
        }
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }

    private boolean hasAlarm(Intent i, int requestCode) {
        PendingIntent pi = PendingIntent.getService(mContext, requestCode, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    private void createAlarm(Intent i, int requestCode, long timeInMillis) {
        // TODO: Maybe show the alarm (without sound) a couple minutes early (like the Android Timer)
        AlarmManager am = getAlarmManager();
        PendingIntent pi = PendingIntent.getService(mContext, requestCode, i, PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
        Log.d(TAG, "createAlarm " + requestCode + " time: " + new Date(timeInMillis) + " isBefore?" + new Date(timeInMillis).before(new Date()) + " PI " + pi.toString());
    }

    private void deleteAlarm(Intent i, int requestCode) {
        if (hasAlarm(i, requestCode)) {
            PendingIntent pi = PendingIntent.getService(mContext, requestCode, i, PendingIntent.FLAG_NO_CREATE);
            pi.cancel();
            getAlarmManager().cancel(pi);

            Log.d(TAG, "Alarm PendingIntent Cancelled " + hasAlarm(i, requestCode));
        }
    }

    private void updateAlarm(Intent i, int requestCode, long timeInMillis) {
        // In order to get the time to show up properly in the top-right of the notification (when it does pop up)
        // This needs to create a new Intent (instead of updating an existing one)
        deleteAlarm(i, requestCode);
        createAlarm(i, requestCode, timeInMillis);
//        PendingIntent pi = PendingIntent.getService(mContext, requestCode, i, PendingIntent.FLAG_NO_CREATE);
//        AlarmManager am = getAlarmManager();
//        am.set(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
//        Log.d(TAG, "updateAlarm " + requestCode + " time: " + new Date(timeInMillis) + " isBefore?" + new Date(timeInMillis).before(new Date()) + " PI " + pi.toString());
    }
}
