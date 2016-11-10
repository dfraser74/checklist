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

/**
 * This is a single ToDoList in a list (FirebaseRecyclerAdapter)
 */
class ToDoListAdapter extends FirebaseRecyclerAdapter<ToDoList, ToDoListViewHolder> implements ItemTouchHelperClass.ItemTouchHelperAdapter {

    private MainActivity mainActivity;
    private final String TAG = ToDoListAdapter.class.getSimpleName();

    private MainActivity mContext;
    private ToDoList mJustCompletedToDoList;
    private DatabaseReference mJustCompletedToDoListRef;


    public ToDoListAdapter(MainActivity mainActivity, MainActivity context, Query items) {
        super(ToDoList.class, R.layout.list_circle_try, ToDoListViewHolder.class, items);
        this.mainActivity = mainActivity;
        mContext = context;
    }

    @Override
    public void populateViewHolder(ToDoListViewHolder holder, ToDoList item, int position) {

        holder.mContext = mContext;
        holder.mItem = item;
        holder.mItemPath = "/items/" + getRef(position).getKey();
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

        holder.mTimeTextView.setVisibility(View.GONE);
        holder.mToDoTextview.setMaxLines(2);
        holder.mToDoTextview.setText(item.getTitle());
        holder.mToDoTextview.setTextColor(todoTextColor);

        String firstLetter = item.getTitle().substring(0, 2);
        // Use the first letter as the hash for the color
        int color = item.getColor();
        TextDrawable myDrawable = TextDrawable.builder().beginConfig()
                .textColor(Color.WHITE)
                .useFont(Typeface.DEFAULT)
//                .toUpperCase()
                .endConfig()
                .buildRound(firstLetter, color);

        holder.mColorImageView.setImageDrawable(myDrawable);

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

        mJustCompletedToDoList = getItem(position);
        mJustCompletedToDoListRef = getRef(position);

        // Toggle the "isArchived" field
        mJustCompletedToDoList.isArchivedSet(true);

        // Save
        mJustCompletedToDoListRef.setValue(mJustCompletedToDoList);

        String toShow = mJustCompletedToDoList.getTitle();
        toShow = (toShow.length() > 20) ? toShow.substring(0, 20) + "..." : toShow;

        Snackbar.make(mContext.mCoordLayout, "Archived " + toShow, Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Comment the line below if not using Google Analytics
                        mainActivity.app.send(this, "Action", "UNDO Pressed");
//                        if (mJustCompletedToDoList.legacyGetRemindAt() != null) {
//                            Intent i = new Intent(mContext, TodoNotificationService.class);
//                            i.putExtra(TodoNotificationService.TODOTEXT, mJustCompletedToDoList.getTitle());
//                            i.putExtra(TodoNotificationService.TODOUUID, mJustCompletedToDoList.getIdentifier());
//                            mContext.createAlarm(i, mJustCompletedToDoList.getIdentifier().hashCode(), mJustCompletedToDoList.legacyGetRemindAt().getTime());
//                        }
                        // TODO: PHIL Insertion order should be a float so we can always insert between 2 items

                        // Toggle completedAt
                        mJustCompletedToDoList.isArchivedSet(false);

                        // Save changes
                        mJustCompletedToDoListRef.setValue(mJustCompletedToDoList);
                    }
                }).show();
    }


}
