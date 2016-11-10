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


        mToDoTextBodyEditText = (EditText) findViewById(R.id.userToDoEditText);
        mToDoSendFloatingActionButton = (FloatingActionButton) findViewById(R.id.makeToDoFloatingActionButton);

        mToDoTextBodyEditText.requestFocus();
        mToDoTextBodyEditText.setText(mUserEnteredText);
        InputMethodManager imm = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
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


        mToDoSendFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUserEnteredText.length() > 0) {
                    String capitalizedString = Character.toUpperCase(mUserEnteredText.charAt(0)) + mUserEnteredText.substring(1);
                    mUserToDoList.setTitle(capitalizedString);
                } else {
                    // Crude just-in-case validation
                    return;
                }

                hideKeyboard(mToDoTextBodyEditText);
                app.send(this, "Action", "Make/Edit Todo List");

                // Save
                MainActivity.getReference("/lists").child(mUserToDoListId).setValue(mUserToDoList);

                makeResult(RESULT_OK);
                finish();
            }
        });

    }


    private String getThemeSet() {
        return getSharedPreferences(MainActivity.THEME_PREFERENCES, MODE_PRIVATE).getString(MainActivity.THEME_SAVED, MainActivity.LIGHTTHEME);
    }

    private void makeResult(int result) {
        Intent i = new Intent();
        i.putExtra(ToDoListActivity.TODOLIST, mUserToDoList);
        i.putExtra(MainActivity.TODOITEM_ID, mUserToDoListId);
        setResult(result, i);

    }

    @Override
    public void onBackPressed() {
        makeResult(RESULT_OK);
        super.onBackPressed();
    }

    public void hideKeyboard(EditText et) {

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(this) != null) {
                    app.send(this, "Action", "Discard Todo List");
                    NavUtils.navigateUpFromSameTask(this);
                }
                hideKeyboard(mToDoTextBodyEditText);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

