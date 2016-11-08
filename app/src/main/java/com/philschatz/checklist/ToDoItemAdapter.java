package com.philschatz.checklist;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.Date;

/**
 * This is a single ToDoItem in a list (FirebaseRecyclerAdapter)
 */
class ToDoItemAdapter extends FirebaseRecyclerAdapter<ToDoItem, ToDoItemViewHolder> implements ItemTouchHelperClass.ItemTouchHelperAdapter {

    private MainActivity mainActivity;
    private final String TAG = ToDoItemAdapter.class.getSimpleName();

    private MainActivity mContext;
    private ToDoItem mJustCompletedToDoItem;
    private DatabaseReference mJustCompletedToDoItemRef;


    public ToDoItemAdapter(MainActivity mainActivity, MainActivity context, Query items) {
        super(ToDoItem.class, R.layout.list_circle_try, ToDoItemViewHolder.class, items);
        this.mainActivity = mainActivity;
        mContext = context;
    }

    @Override
    public void populateViewHolder(ToDoItemViewHolder holder, ToDoItem item, int position) {

        holder.mContext = mContext;
        holder.mItem = item;
        holder.mItemId = getRef(position).getKey();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(MainActivity.THEME_PREFERENCES, Context.MODE_PRIVATE);
        //Background color for each to-do item. Necessary for night/day mode
        int bgColor;
        //color of title text in our to-do item. White for night mode, dark gray for day mode
        int todoTextColor;
        if (sharedPreferences.getString(MainActivity.THEME_SAVED, MainActivity.LIGHTTHEME).equals(MainActivity.LIGHTTHEME)) {
            bgColor = Color.WHITE;
            todoTextColor = mContext.getResources().getColor(R.color.secondary_text);
        } else {
            bgColor = Color.DKGRAY;
            todoTextColor = Color.WHITE;
        }
        holder.linearLayout.setBackgroundColor(bgColor);

        if (item.legacyGetRemindAt() != null || item.legacyGetCompletedAt() != null) {
            holder.mToDoTextview.setMaxLines(1);
            holder.mTimeTextView.setVisibility(View.VISIBLE);
        } else {
            holder.mTimeTextView.setVisibility(View.GONE);
            holder.mToDoTextview.setMaxLines(2);
        }
        holder.mToDoTextview.setText(item.getTitle());
        holder.mToDoTextview.setTextColor(todoTextColor);
        if (item.legacyGetCompletedAt() != null) {
            holder.mToDoTextview.setTextColor(Color.LTGRAY);
            holder.mTimeTextView.setTextColor(Color.LTGRAY);
        }
        if (item.legacyGetCompletedAt() != null) {
            holder.mToDoTextview.setPaintFlags(holder.mToDoTextview.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        //            holder.mColorTextView.setBackgroundColor(Color.parseColor(item.getTodoColor()));

        //            TextDrawable myDrawable = TextDrawable.builder().buildRoundRect(item.getTitle().substring(0,1),Color.RED, 10);
        //We check if holder.color is set or not
        //            if(item.getTodoColor() == null){
        //                ColorGenerator generator = ColorGenerator.MATERIAL;
        //                int color = generator.getRandomColor();
        //                item.setTodoColor(color+"");
        //            }
        //            Log.d("OskarSchindler", "Color: "+item.getTodoColor());


        String firstLetter = item.getTitle().substring(0, 1);
        // Use the first letter as the hash for the color
        int color = ColorGenerator.MATERIAL.getColor(firstLetter);
        TextDrawable myDrawable = TextDrawable.builder().beginConfig()
                .textColor(Color.WHITE)
                .useFont(Typeface.DEFAULT)
                .toUpperCase()
                .endConfig()
                .buildRound(firstLetter, color);

        holder.mColorImageView.setImageDrawable(myDrawable);
        if (item.legacyGetCompletedAt() != null) {
            Date time = item.legacyGetCompletedAt();
            holder.mTimeTextView.setReferenceTime(time.getTime());
        } else if (item.legacyGetRemindAt() != null) {
            Date time = item.legacyGetRemindAt();
            holder.mTimeTextView.setReferenceTime(time.getTime());
        }

    }

    // ItemTouchHelperAdapter methods
    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        Log.d(TAG, "TODO: Item was moved but we do not implement that yet");
    }

    @Override
    public void onItemRemoved(final int position) {
        //Remove this line if not using Google Analytics
        mContext.app.send(this, "Action", "Swiped Todo Away");

        mJustCompletedToDoItem = getItem(position);
        mJustCompletedToDoItemRef = getRef(position);

        // Toggle the "completedAt" field
        Date completedAt = mJustCompletedToDoItem.legacyGetCompletedAt();
        completedAt = (completedAt == null) ? new Date() : null;
        mJustCompletedToDoItem.legacySetCompletedAt(completedAt);

        // Save
        mJustCompletedToDoItemRef.setValue(mJustCompletedToDoItem);

//        Intent i = new Intent(mContext, TodoNotificationService.class);
//        mContext.deleteAlarm(i, mJustCompletedToDoItem.getIdentifier().hashCode());

        String toShow = mJustCompletedToDoItem.getTitle();
        toShow = (toShow.length() > 20) ? toShow.substring(0, 20) + "..." : toShow;

        Snackbar.make(mContext.mCoordLayout, "Completed " + toShow, Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Comment the line below if not using Google Analytics
                        mainActivity.app.send(this, "Action", "UNDO Pressed");
//                        if (mJustCompletedToDoItem.legacyGetRemindAt() != null) {
//                            Intent i = new Intent(mContext, TodoNotificationService.class);
//                            i.putExtra(TodoNotificationService.TODOTEXT, mJustCompletedToDoItem.getTitle());
//                            i.putExtra(TodoNotificationService.TODOUUID, mJustCompletedToDoItem.getIdentifier());
//                            mContext.createAlarm(i, mJustCompletedToDoItem.getIdentifier().hashCode(), mJustCompletedToDoItem.legacyGetRemindAt().getTime());
//                        }
                        // TODO: PHIL Insertion order should be a float so we can always insert between 2 items

                        // Toggle completedAt
                        Date completedAt = mJustCompletedToDoItem.legacyGetCompletedAt();
                        completedAt = (completedAt == null) ? new Date() : null;
                        mJustCompletedToDoItem.legacySetCompletedAt(completedAt);

                        // Save changes
                        mJustCompletedToDoItemRef.setValue(mJustCompletedToDoItem);
                    }
                }).show();
    }


}
