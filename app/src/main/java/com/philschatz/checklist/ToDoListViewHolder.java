package com.philschatz.checklist;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

/**
 * This needs to be static because FirebaseAdapter will instantiate this programatically
 */
@SuppressWarnings("deprecation")
class ToDoListViewHolder extends RecyclerView.ViewHolder {

    ToDoList mItem;
    String mItemPath;
    MainActivity mContext;
    View mView;
    LinearLayout linearLayout;
    TextView mToDoTextview;
    ImageView mColorImageView;
    RelativeTimeTextView mTimeTextView;
//            int color = -1;

    public ToDoListViewHolder(View v) {
        super(v);
        mView = v;
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToDoList item = mItem;

                Intent i = new Intent(mContext, ToDoListActivity.class);
                i.putExtra(TodoNotificationService.TODO_DB_PATH, mItemPath);
                i.putExtra(ToDoListActivity.TODOLIST, item);
                mContext.startActivity(i);
            }
        });
        mToDoTextview = (TextView) v.findViewById(R.id.toDoListItemTextview);
        mTimeTextView = (RelativeTimeTextView) v.findViewById(R.id.todoListItemTimeTextView);
//                mColorTextView = (TextView)v.findViewById(R.id.toDoColorTextView);
        mColorImageView = (ImageView) v.findViewById(R.id.toDoListItemColorImageView);
        linearLayout = (LinearLayout) v.findViewById(R.id.listItemLinearLayout);
    }

}
