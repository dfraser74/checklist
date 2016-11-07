package com.philschatz.checklist;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.Date;

/**
 * This is a listener for Firebase events and updates the desktop notifications
 */
class ToDoItemAlarmListener implements ChildEventListener {
    private static String TAG = "ToDoItem Alarm Listener";

    private Context mContext;

    public ToDoItemAlarmListener(Context c) {
        mContext = c;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        setAlarmIfNecessary(dataSnapshot.getValue(ToDoItem.class));
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        setAlarmIfNecessary(dataSnapshot.getValue(ToDoItem.class));
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        setAlarmIfNecessary(dataSnapshot.getValue(ToDoItem.class));
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        // Ignore moves
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.d(TAG, "Database Disconnect");
    }

    private void setAlarmIfNecessary(ToDoItem item) {
        // Here are the possible states:
        //  hasReminder &&  hasAlarm -> nothing; (maybe update the alarm)
        //  hasReminder && !hasAlarm -> createAlarm();
        // !hasReminder &&  hasAlarm -> deleteAlarm();
        // !hasReminder && !hasAlarm -> nothing;
        Intent i = new Intent(mContext, TodoNotificationService.class);
        int hashCode = item.getIdentifier().hashCode();
        boolean hasAlarmForItem = hasAlarm(i, hashCode);
        Date remindAt = item.getRemindAt();

        // Only care about reminders when item is not complete and reminder time is after now
        if (item.getCompletedAt() != null || (remindAt != null && remindAt.before(new Date()))) {
            remindAt = null;
        }

        if (remindAt != null && !hasAlarmForItem) {
            i.putExtra(TodoNotificationService.TODOUUID, item.getIdentifier());
            i.putExtra(TodoNotificationService.TODOTEXT, item.getTitle());
            i.putExtra(TodoNotificationService.TODOREMINDAT, item.getRemindAt());
            createAlarm(i, hashCode, remindAt.getTime());
        } else if (remindAt == null && hasAlarmForItem) {
            deleteAlarm(i, hashCode);
        } else if (remindAt != null && hasAlarmForItem) {
            updateAlarm(i, hashCode, remindAt.getTime());
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
        PendingIntent pi = PendingIntent.getService(mContext, requestCode, i, PendingIntent.FLAG_NO_CREATE);
        AlarmManager am = getAlarmManager();
        am.set(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
        Log.d(TAG, "updateAlarm " + requestCode + " time: " + new Date(timeInMillis) + " isBefore?" + new Date(timeInMillis).before(new Date()) + " PI " + pi.toString());
    }
}
