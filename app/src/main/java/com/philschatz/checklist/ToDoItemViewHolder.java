package com.philschatz.checklist;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
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
class ToDoItemViewHolder extends RecyclerView.ViewHolder {

    ToDoList mList;
    ToDoItem mItem;
    String mItemKey;
    FragmentActivity mContext;
    View mView;
    LinearLayout linearLayout;
    TextView mToDoTextview;
    ImageView mColorImageView;
    RelativeTimeTextView mTimeTextView;
//            int color = -1;

    public ToDoItemViewHolder(View v) {
        super(v);
        mView = v;
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(mContext, AddToDoItemActivity.class);
                i.putExtra(Const.TODOLISTSNAPSHOT, mList);
                i.putExtra(Const.TODOITEMSNAPSHOT, mItem);
                i.putExtra(Const.TODOITEMKEY, mItemKey);
                mContext.startActivityForResult(i, MainActivity.REQUEST_ID_TODO_ITEM);
            }
        });
        mToDoTextview = (TextView) v.findViewById(R.id.toDoListItemTextview);
        mTimeTextView = (RelativeTimeTextView) v.findViewById(R.id.todoListItemTimeTextView);
//                mColorTextView = (TextView)v.findViewById(R.id.toDoColorTextView);
        mColorImageView = (ImageView) v.findViewById(R.id.toDoListItemColorImageView);
        linearLayout = (LinearLayout) v.findViewById(R.id.listItemLinearLayout);
    }

}
