package com.philschatz.checklist;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.philschatz.checklist.notifications.CompleteNotificationService;
import com.philschatz.checklist.notifications.Snooze20Minutes;
import com.philschatz.checklist.notifications.Snooze5Minutes;

/*
 * This generates the homescreen notification for checklist items that have a reminder
 */
public class TodoNotificationService extends IntentService {

    public TodoNotificationService() {
        super("TodoNotificationService");
    }

    // !!! Make sure you add an entry to AndroidManifest.xml
    private Notification.Action buildSnooze(Class intentService, String label, ToDoItem item, String listKey, String itemKey, int icon) {
        Intent snoozeIntent = new Intent(this, intentService);
        snoozeIntent.putExtra(Const.TODOITEMSNAPSHOT, item);
        snoozeIntent.putExtra(Const.TODOLISTKEY, listKey);
        snoozeIntent.putExtra(Const.TODOITEMKEY, itemKey);
        int hashCode = itemKey.hashCode();
        PendingIntent snoozePendingIntent = PendingIntent.getService(this, hashCode, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action snoozeAction = new Notification.Action.Builder(icon, label, snoozePendingIntent)
                .build();
        return snoozeAction;
    }

    protected void onHandleIntent(Intent intent) {
//        mTodoText = intent.getStringExtra(TODOTEXT);
//        mTodoUUID = intent.getStringExtra(TODOUUID);
//        mTodoRemindAt = (Date) intent.getSerializableExtra(TODOREMINDAT);
//        if (mTodoRemindAt == null) {
//            throw new RuntimeException("BUG: Missing remindAt");
//        }
        ToDoItem item = (ToDoItem) intent.getSerializableExtra(Const.TODOITEMSNAPSHOT);
        ToDoList list = (ToDoList) intent.getSerializableExtra(Const.TODOLISTSNAPSHOT);
        String listKey = intent.getStringExtra(Const.TODOLISTKEY);
        String itemKey = intent.getStringExtra(Const.TODOITEMKEY);

        if (item == null) {
            throw new RuntimeException("Missing " + Const.TODOITEMSNAPSHOT);
        }
        if (list == null) {
            throw new RuntimeException("Missing " + Const.TODOLISTSNAPSHOT);
        }
        if (listKey == null) {
            throw new RuntimeException("Missing " + Const.TODOLISTKEY);
        }
        if (itemKey == null) {
            throw new RuntimeException("Missing " + Const.TODOITEMKEY);
        }
        final int hashCode = itemKey.hashCode();

        Log.d("OskarSchindler", "onHandleIntent called");
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent editReminderIntent = new Intent(this, AddToDoItemActivity.class);
        editReminderIntent.putExtra(Const.TODOITEMSNAPSHOT, item);
        editReminderIntent.putExtra(Const.TODOLISTKEY, listKey);
        editReminderIntent.putExtra(Const.TODOITEMKEY, itemKey);

//        Intent completeIntent = new Intent(this, CompleteNotificationService.class);
//        completeIntent.putExtra(Const.TODOITEMSNAPSHOT, item);
//        completeIntent.putExtra(Const.TODOLISTKEY, listKey);
//        completeIntent.putExtra(Const.TODOITEMKEY, itemKey);

        Intent snooze20 = new Intent(this, Snooze20Minutes.class);
        snooze20.putExtra(Const.TODOITEMSNAPSHOT, item);
        snooze20.putExtra(Const.TODOLISTKEY, listKey);
        snooze20.putExtra(Const.TODOITEMKEY, itemKey);


        if (!item.hasReminder()) {
            throw new RuntimeException("BUG: just making sure the item has a reminder");
        }

        Notification notification = new Notification.Builder(this)
                .setAutoCancel(false) // hide the notification when an action is performed?
                .setCategory(Notification.CATEGORY_REMINDER)
                .setPriority(Notification.PRIORITY_HIGH) // Useful for the heads up notification so people are reminded
                .setColor(list.getColor())
                .setSmallIcon(R.drawable.ic_done_white_24dp)
                .setContentTitle(item.getTitle())
                .setContentText(list.getTitle())
                .setUsesChronometer(true) // Starts ticking up to show how much more reddit time you're spending (beyond the alotted 20min or whatever)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(PendingIntent.getActivity(this, hashCode, editReminderIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setDeleteIntent(PendingIntent.getService(this, hashCode, snooze20, PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(buildSnooze(Snooze5Minutes.class, "5 min", item, listKey, itemKey, R.drawable.ic_snooze_white_24dp))
                .addAction(buildSnooze(CompleteNotificationService.class, "complete", item, listKey, itemKey, R.drawable.ic_done_white_24dp))
                .setWhen(item.remindAt())
                .build();

        manager.notify(hashCode, notification);

//        Uri defaultRingone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        MediaPlayer mp = new MediaPlayer();
//        try{
//            mp.setDataSource(this, defaultRingone);
//            mp.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
//            mp.prepare();
//            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    mp.release();
//                }
//            });
//            mp.start();
//
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }

    }
}
