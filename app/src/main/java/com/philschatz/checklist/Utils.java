package com.philschatz.checklist;

import android.content.Context;
import android.content.res.TypedArray;

import com.google.firebase.database.DatabaseReference;


public class Utils {


    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return toolbarHeight;
    }

    public static String getFirebasePath(DatabaseReference ref) {
        String key = ref.getKey();
        DatabaseReference parent = ref.getParent();
        if (parent != null) {
            return getFirebasePath(parent) + "/" + key;
        } else if (key != null) {
            return key;
        } else {
            return "";
        }

    }
}
