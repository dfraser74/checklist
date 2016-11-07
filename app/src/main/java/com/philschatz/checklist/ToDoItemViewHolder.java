package com.philschatz.checklist;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This needs to be static because FirebaseAdapter will instantiate this programatically
 */
@SuppressWarnings("deprecation")
class ToDoItemViewHolder extends RecyclerView.ViewHolder {

    ToDoItem mItem;
    String mItemId;
    MainActivity mContext;
    View mView;
    LinearLayout linearLayout;
    TextView mToDoTextview;
    ImageView mColorImageView;
    TextView mTimeTextView;
//            int color = -1;

    public ToDoItemViewHolder(View v) {
        super(v);
        mView = v;
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToDoItem item = mItem;
                String itemId = mItemId;

                Intent i = new Intent(mContext, AddToDoActivity.class);
                i.putExtra(MainActivity.TODOITEM, item);
                i.putExtra(MainActivity.TODOITEM_ID, itemId);
                mContext.startActivityForResult(i, MainActivity.REQUEST_ID_TODO_ITEM);
            }
        });
        mToDoTextview = (TextView) v.findViewById(R.id.toDoListItemTextview);
        mTimeTextView = (TextView) v.findViewById(R.id.todoListItemTimeTextView);
//                mColorTextView = (TextView)v.findViewById(R.id.toDoColorTextView);
        mColorImageView = (ImageView) v.findViewById(R.id.toDoListItemColorImageView);
        linearLayout = (LinearLayout) v.findViewById(R.id.listItemLinearLayout);
    }

}
