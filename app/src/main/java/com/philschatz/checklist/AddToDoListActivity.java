package com.philschatz.checklist;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.Date;

public class AddToDoListActivity extends AppCompatActivity {
    AnalyticsApplication app;
    private EditText mToDoTextBodyEditText;
    private ToDoList mUserToDoList;
    private String mUserToDoListId;
    private FloatingActionButton mToDoSendFloatingActionButton;
    private String mUserEnteredText;
    private Toolbar mToolbar;
//    private LinearLayout mContainerLayout;
    private String theme;

    @Override
    protected void onResume() {
        super.onResume();
        app.send(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        app = (AnalyticsApplication) getApplication();
//        setContentView(R.layout.new_to_do_layout);
        //Need references to these to change them during light/dark mode
        ImageButton reminderIconImageButton;
        TextView reminderRemindMeTextView;


        theme = getSharedPreferences(MainActivity.THEME_PREFERENCES, MODE_PRIVATE).getString(MainActivity.THEME_SAVED, MainActivity.LIGHTTHEME);
        if (theme.equals(MainActivity.LIGHTTHEME)) {
            setTheme(R.style.CustomStyle_LightTheme);
            Log.d("OskarSchindler", "Light Theme");
        } else {
            setTheme(R.style.CustomStyle_DarkTheme);
        }

        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_to_do);
        //Testing out a new layout
        setContentView(R.layout.activity_add_to_do_list);

        //Show an X in place of <-
        final Drawable cross = getResources().getDrawable(R.drawable.ic_clear_white_24dp);
        if (cross != null) {
            cross.setColorFilter(getResources().getColor(R.color.icons), PorterDuff.Mode.SRC_ATOP);
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(cross);

        }


        mUserToDoList = (ToDoList) getIntent().getSerializableExtra(ToDoListActivity.TODOLIST);
        mUserToDoListId = getIntent().getStringExtra(MainActivity.TODOITEM_ID);

        mUserEnteredText = mUserToDoList.getTitle();

        reminderIconImageButton = (ImageButton) findViewById(R.id.userToDoReminderIconImageButton);
        reminderRemindMeTextView = (TextView) findViewById(R.id.userToDoRemindMeTextView);
        if (theme.equals(MainActivity.DARKTHEME)) {
            reminderIconImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_alarm_add_white_24dp));
            reminderRemindMeTextView.setTextColor(Color.WHITE);
        }


//        mContainerLayout = (LinearLayout) findViewById(R.id.todoReminderAndDateContainerLayout);
        mToDoTextBodyEditText = (EditText) findViewById(R.id.userToDoEditText);
//        mLastSeenTextView = (TextView)findViewById(R.id.toDoLastEditedTextView);
        mToDoSendFloatingActionButton = (FloatingActionButton) findViewById(R.id.makeToDoFloatingActionButton);


//        mContainerLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                hideKeyboard(mToDoTextBodyEditText);
//            }
//        });

//        TextInputLayout til = (TextInputLayout)findViewById(R.id.toDoCustomTextInput);
//        til.requestFocus();
        mToDoTextBodyEditText.requestFocus();
        mToDoTextBodyEditText.setText(mUserEnteredText);
        InputMethodManager imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
//        imm.showSoftInput(mToDoTextBodyEditText, InputMethodManager.SHOW_IMPLICIT);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        mToDoTextBodyEditText.setSelection(mToDoTextBodyEditText.length());


        mToDoTextBodyEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mUserEnteredText = s.toString();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


//        String lastSeen = formatDate(DATE_FORMAT, mLastEdited);
//        mLastSeenTextView.setText(String.format(getResources().getString(R.string.last_edited), lastSeen));


        mToDoSendFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(mToDoTextBodyEditText);
                app.send(this, "Action", "Make Todo List");
                makeResult(RESULT_OK);
                finish();
            }
        });

    }


    private String getThemeSet() {
        return getSharedPreferences(MainActivity.THEME_PREFERENCES, MODE_PRIVATE).getString(MainActivity.THEME_SAVED, MainActivity.LIGHTTHEME);
    }

    public void hideKeyboard(EditText et) {

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }


    public void makeResult(int result) {
        Intent i = new Intent();
        if (mUserEnteredText.length() > 0) {

            String capitalizedString = Character.toUpperCase(mUserEnteredText.charAt(0)) + mUserEnteredText.substring(1);
            mUserToDoList.setTitle(capitalizedString);
        } else {
            mUserToDoList.setTitle(mUserEnteredText);
        }

//        mUserToDoList.setTodoColor(mUserColor);
        i.putExtra(ToDoListActivity.TODOLIST, mUserToDoList);
        i.putExtra(MainActivity.TODOITEM_ID, mUserToDoListId);
        setResult(result, i);
    }

    @Override
    public void onBackPressed() {
        makeResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(this) != null) {
                    app.send(this, "Action", "Discard Todo");
                    makeResult(RESULT_CANCELED);
                    NavUtils.navigateUpFromSameTask(this);
                }
                hideKeyboard(mToDoTextBodyEditText);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    @Override
//    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hour, int minute) {
//        setTime(hour, minute);
//    }
//
//    @Override
//    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
//        setDate(year, month, day);
//    }
//
}

