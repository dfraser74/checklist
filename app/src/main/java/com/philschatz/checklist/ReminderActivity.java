package com.philschatz.checklist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import fr.ganfra.materialspinner.MaterialSpinner;

public class ReminderActivity extends AppCompatActivity {
    public static final String EXIT = "com.philschatz.checklist.exit";
    String theme;
    AnalyticsApplication app;
    private TextView mtoDoTextTextView;
    private Button mRemoveToDoButton;
    private MaterialSpinner mSnoozeSpinner;
    private String[] snoozeOptionsArray;
    private StoreRetrieveData storeRetrieveData;
    private ArrayList<ToDoItem> mToDoItems;
    private ToDoItem mItem;
    private DatabaseReference mDbRef;
    private TextView mSnoozeTextView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        app = (AnalyticsApplication) getApplication();
        app.send(this);

        theme = getSharedPreferences(MainActivity.THEME_PREFERENCES, MODE_PRIVATE).getString(MainActivity.THEME_SAVED, MainActivity.LIGHTTHEME);
        if (theme.equals(MainActivity.LIGHTTHEME)) {
            setTheme(R.style.CustomStyle_LightTheme);
        } else {
            setTheme(R.style.CustomStyle_DarkTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_layout);
        storeRetrieveData = new StoreRetrieveData(this, MainActivity.FILENAME);
        mToDoItems = new ArrayList<>();

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));


        Intent i = getIntent();
        mItem = (ToDoItem) i.getSerializableExtra(TodoNotificationService.TODOITEMSNAPSHOT);
        String dbPath = i.getStringExtra(TodoNotificationService.TODO_DB_PATH);
        mDbRef = MainActivity.getFirebaseDatabase().getReference(dbPath);

        snoozeOptionsArray = getResources().getStringArray(R.array.snooze_options);

        mRemoveToDoButton = (Button) findViewById(R.id.toDoReminderRemoveButton);
        mtoDoTextTextView = (TextView) findViewById(R.id.toDoReminderTextViewBody);
        mSnoozeTextView = (TextView) findViewById(R.id.reminderViewSnoozeTextView);
        mSnoozeSpinner = (MaterialSpinner) findViewById(R.id.todoReminderSnoozeSpinner);

//        mtoDoTextTextView.setBackgroundColor(item.getTodoColor());
        mtoDoTextTextView.setText(mItem.getTitle());

        if (theme.equals(MainActivity.LIGHTTHEME)) {
            mSnoozeTextView.setTextColor(getResources().getColor(R.color.secondary_text));
        } else {
            mSnoozeTextView.setTextColor(Color.WHITE);
            mSnoozeTextView.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_snooze_white_24dp, 0, 0, 0
            );
        }

        mRemoveToDoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                app.send(this, "Action", "Todo Removed from Reminder Activity");
                mItem.isArchivedSet(true);
                saveData(mItem);
                changeOccurred();
                closeApp();
//                finish();
            }
        });


//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, snoozeOptionsArray);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_text_view, snoozeOptionsArray);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        mSnoozeSpinner.setAdapter(adapter);
//        mSnoozeSpinner.setSelection(0);

    }

    private void closeApp() {
        Intent i = new Intent(ReminderActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        i.putExtra(EXIT, true);
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREF_DATA_SET_CHANGED, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(EXIT, true);
        editor.apply();
        startActivity(i);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reminder, menu);
        return true;
    }

    private void changeOccurred() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREF_DATA_SET_CHANGED, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(MainActivity.CHANGE_OCCURED, true);
//        editor.commit();
        editor.apply();
    }

    private Date addTimeToDate(int mins) {
        app.send(this, "Action", "Snoozed", "For " + mins + " minutes");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, mins);
        return calendar.getTime();
    }

    private int valueFromSpinner() {
        switch (mSnoozeSpinner.getSelectedItemPosition()) {
            case 0:
                return 1;
            case 1:
                return 10;
            case 2:
                return 30;
            case 3:
                return 60;
            default:
                return 0;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toDoReminderDoneMenuItem:
                Date date = addTimeToDate(valueFromSpinner());
                mItem.remindAtSet(date.getTime());
                Log.d("OskarSchindler", "Date Changed to: " + date);
                changeOccurred();
                saveData(mItem);
//                closeApp();
                finish();
                //foo
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        try{
//            storeRetrieveData.saveToFile(mToDoItems);
//        }
//        catch (JSONException | IOException e){
//            e.printStackTrace();
//        }
//    }

    private void saveData(ToDoItem item) {
        mDbRef.setValue(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        try{
//            storeRetrieveData.saveToFile(mToDoItems);
//        }
//        catch (JSONException | IOException e){
//            e.printStackTrace();
//        }
    }
}
