package com.philschatz.checklist;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.philschatz.checklist.notifications.CompleteNotificationService;
import com.philschatz.checklist.notifications.Snooze1Day;
import com.philschatz.checklist.notifications.Snooze20Minutes;
import com.philschatz.checklist.notifications.Snooze2Minutes;

/*
 * This generates the homescreen notification for checklist items that have a reminder
 */
public class TodoNotificationService extends IntentService {
    public static final String TODO_DB_PATH = "com.philschatz.checklist.tododatabasepath";
    public static final String TODOITEMSNAPSHOT = "com.philschatz.checklist.todoitemsnapshot";
    public static final String TODOTEXT = "com.philschatz.checklist.todonotificationservicetext";
    public static final String TODOUUID = "com.philschatz.checklist.todonotificationserviceuuid";
    public static final String TODOREMINDAT = "com.philschatz.checklist.todonotificationserviceremindat";
    public static final String NOTIFICATION_ID = "com.philschatz.checklist.todonotificationid";

    public TodoNotificationService() {
        super("TodoNotificationService");
    }

    // !!! Make sure you add an entry to AndroidManifest.xml
    private Notification.Action buildSnooze(Class intentService, String label, ToDoItem item, String dbPath) {
        Intent snoozeIntent = new Intent(this, intentService);
        snoozeIntent.putExtra(TodoNotificationService.TODOITEMSNAPSHOT, item);
        snoozeIntent.putExtra(TodoNotificationService.TODO_DB_PATH, dbPath);
        int hashCode = dbPath.hashCode();
        PendingIntent snoozePendingIntent = PendingIntent.getService(this, hashCode, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action snoozeAction = new Notification.Action.Builder(R.drawable.ic_snooze_white_24dp, label, snoozePendingIntent)
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
        ToDoItem item = (ToDoItem) intent.getSerializableExtra(TODOITEMSNAPSHOT);
        String dbPath = intent.getStringExtra(TODO_DB_PATH);

        if (item == null) {
            throw new RuntimeException("Missing " + TODOITEMSNAPSHOT);
        }
        if (dbPath == null) {
            throw new RuntimeException("Missing " + TODO_DB_PATH);
        }
        final int hashCode = dbPath.hashCode();

        Log.d("OskarSchindler", "onHandleIntent called");
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        Intent i = new Intent(this, ReminderActivity.class);
////        i.putExtra(TodoNotificationService.TODOUUID, mTodoUUID);
//        i.putExtra(TodoNotificationService.TODOITEMSNAPSHOT, item);
//        i.putExtra(TodoNotificationService.TODO_DB_PATH, dbPath);

        Intent completeIntent = new Intent(this, CompleteNotificationService.class);
        completeIntent.putExtra(TodoNotificationService.TODOITEMSNAPSHOT, item);
        completeIntent.putExtra(TodoNotificationService.TODO_DB_PATH, dbPath);


        if (!item.hasReminder()) {
            throw new RuntimeException("BUG: just making sure the item has a reminder");
        }

        Notification notification = new Notification.Builder(this)
                .setAutoCancel(true) // hide the notification when an action is performed?
                .setCategory(Notification.CATEGORY_REMINDER)
                .setPriority(Notification.PRIORITY_HIGH) // Useful for the heads up notification so people are reminded
                .setSmallIcon(R.drawable.ic_done_white_24dp)
                .setContentTitle(item.getTitle())
                .setContentText("(list name here)")
                .setAutoCancel(false)
                .setUsesChronometer(true) // Starts ticking up to show how much more reddit time you're spending (beyond the alotted 20min or whatever)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setDeleteIntent(PendingIntent.getService(this, dbPath.hashCode(), completeIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(buildSnooze(Snooze2Minutes.class, "2 min", item, dbPath))
                .addAction(buildSnooze(Snooze20Minutes.class, "20 min", item, dbPath))
                .addAction(buildSnooze(Snooze1Day.class, "1 day", item, dbPath))
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
