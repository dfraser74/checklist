package com.philschatz.checklist;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.Date;

/*
 * This generates the homescreen notification for checklist items that have a reminder
 */
public class TodoNotificationService extends IntentService {
    public static final String TODOTEXT = "com.philschatz.checklist.todonotificationservicetext";
    public static final String TODOUUID = "com.philschatz.checklist.todonotificationserviceuuid";
    public static final String TODOREMINDAT = "com.philschatz.checklist.todonotificationserviceremindat";
    private String mTodoText;
    private String mTodoUUID;
    private Context mContext;
    private Date mTodoRemindAt;

    public TodoNotificationService() {
        super("TodoNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mTodoText = intent.getStringExtra(TODOTEXT);
        mTodoUUID = intent.getStringExtra(TODOUUID);
        mTodoRemindAt = (Date) intent.getSerializableExtra(TODOREMINDAT);
        if (mTodoRemindAt == null) {
            throw new RuntimeException("BUG: Missing remindAt");
        }

        Log.d("OskarSchindler", "onHandleIntent called");
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent i = new Intent(this, ReminderActivity.class);
        i.putExtra(TodoNotificationService.TODOUUID, mTodoUUID);
        Intent deleteIntent = new Intent(this, DeleteNotificationService.class);
        deleteIntent.putExtra(TODOUUID, mTodoUUID);

        Intent snoozeIntent = new Intent(Intent.ACTION_VIEW);
        snoozeIntent.setData(Uri.parse("http://philschatz.com"));
        PendingIntent snoozePendingIntent = PendingIntent.getActivity(this, 0, snoozeIntent, 0);

        Notification.Action snoozeAction = new Notification.Action.Builder(R.drawable.ic_snooze_white_24dp, "5 min", snoozePendingIntent)
                .build();

        Notification.Action snoozeDayAction = new Notification.Action.Builder(R.drawable.ic_snooze_white_24dp, "1 day", snoozePendingIntent)
                .build();


        Notification.Action completeAction = new Notification.Action.Builder(R.drawable.ic_done_white_24dp, "Complete", PendingIntent.getService(this, mTodoUUID.hashCode(), deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .build();

        Notification notification = new Notification.Builder(this)
                .setCategory(Notification.CATEGORY_REMINDER)
                .setPriority(Notification.PRIORITY_HIGH) // Useful for the heads up notification so people are reminded
                .setSmallIcon(R.drawable.ic_done_white_24dp)
                .setContentTitle(mTodoText)
                .setContentText("(list name here)")
                .setAutoCancel(false)
                .setUsesChronometer(true) // Starts ticking up to show how much more reddit time you're spending (beyond the alotted 20min or whatever)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setDeleteIntent(PendingIntent.getService(this, mTodoUUID.hashCode(), deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setContentIntent(PendingIntent.getActivity(this, mTodoUUID.hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(snoozeAction)
                .addAction(snoozeDayAction)
                .addAction(completeAction)
                .setWhen(mTodoRemindAt.getTime())
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
