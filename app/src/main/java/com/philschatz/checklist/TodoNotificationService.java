package com.philschatz.checklist;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.philschatz.checklist.notifications.CompleteNotificationService;
import com.philschatz.checklist.notifications.SnoozeOneDay;
import com.philschatz.checklist.notifications.SnoozeFiveMinutes;

/*
 * This generates the homescreen notification for checklist items that have a reminder
 */
public class TodoNotificationService extends IntentService {
    public static final String TODO_DB_PATH = "com.philschatz.checklist.tododatabasepath";
    public static final String TODOITEMSNAPSHOT = "com.philschatz.checklist.todoitemsnapshot";
    public static final String TODOTEXT = "com.philschatz.checklist.todonotificationservicetext";
    public static final String TODOUUID = "com.philschatz.checklist.todonotificationserviceuuid";
    public static final String TODOREMINDAT = "com.philschatz.checklist.todonotificationserviceremindat";

    public TodoNotificationService() {
        super("TodoNotificationService");
    }

    @Override
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

        Log.d("OskarSchindler", "onHandleIntent called");
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        Intent i = new Intent(this, ReminderActivity.class);
////        i.putExtra(TodoNotificationService.TODOUUID, mTodoUUID);
//        i.putExtra(TodoNotificationService.TODOITEMSNAPSHOT, item);
//        i.putExtra(TodoNotificationService.TODO_DB_PATH, dbPath);

        Intent snoozeIntent = new Intent(this, SnoozeFiveMinutes.class);
        snoozeIntent.putExtra(TodoNotificationService.TODOITEMSNAPSHOT, item);
        snoozeIntent.putExtra(TodoNotificationService.TODO_DB_PATH, dbPath);
        PendingIntent snoozePendingIntent = PendingIntent.getService(this, dbPath.hashCode(), snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent snoozeDayIntent = new Intent(this, SnoozeOneDay.class);
        snoozeDayIntent.putExtra(TodoNotificationService.TODOITEMSNAPSHOT, item);
        snoozeDayIntent.putExtra(TodoNotificationService.TODO_DB_PATH, dbPath);
//        PendingIntent snoozeDayPendingIntent = PendingIntent.getActivity(this, 0, snoozeDayIntent, 0);
        PendingIntent snoozeDayPendingIntent = PendingIntent.getService(this, dbPath.hashCode(), snoozeDayIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent deleteIntent = new Intent(this, CompleteNotificationService.class);
        deleteIntent.putExtra(TodoNotificationService.TODOITEMSNAPSHOT, item);
        deleteIntent.putExtra(TodoNotificationService.TODO_DB_PATH, dbPath);
        PendingIntent deletePendingIntent = PendingIntent.getService(this, dbPath.hashCode(), deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action snoozeAction = new Notification.Action.Builder(R.drawable.ic_snooze_white_24dp, "5 min", snoozePendingIntent)
                .build();

        Notification.Action snoozeDayAction = new Notification.Action.Builder(R.drawable.ic_snooze_white_24dp, "1 day", snoozeDayPendingIntent)
                .build();


        Notification.Action completeAction = new Notification.Action.Builder(R.drawable.ic_done_white_24dp, "Complete", deletePendingIntent)
                .build();

        Notification notification = new Notification.Builder(this)
                .setCategory(Notification.CATEGORY_REMINDER)
                .setPriority(Notification.PRIORITY_HIGH) // Useful for the heads up notification so people are reminded
                .setSmallIcon(R.drawable.ic_done_white_24dp)
                .setContentTitle(item.getTitle())
                .setContentText("(list name here)")
                .setAutoCancel(false)
                .setUsesChronometer(true) // Starts ticking up to show how much more reddit time you're spending (beyond the alotted 20min or whatever)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setDeleteIntent(PendingIntent.getService(this, dbPath.hashCode(), deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT))
//                .setContentIntent(PendingIntent.getActivity(this, dbPath.hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(completeAction)
                .addAction(snoozeAction)
                .addAction(snoozeDayAction)
                .setWhen(item.getRemindAt().getTime())
                .build();

        manager.notify(100, notification);
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
