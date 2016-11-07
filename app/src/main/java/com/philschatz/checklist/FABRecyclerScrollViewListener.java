package com.philschatz.checklist;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

// This makes the FloatingActionButton disappear when you scroll down
// It assumes the FloatingActionButton uses a CoordinatorLayout.LayoutParams
public class FABRecyclerScrollViewListener extends CustomRecyclerScrollViewListener {
    private FloatingActionButton mFab;

    public FABRecyclerScrollViewListener(FloatingActionButton fab) {
        mFab = fab;
    }

    @Override
    public void show() {
        mFab.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    @Override
    public void hide() {
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mFab.getLayoutParams();
        int fabMargin = lp.bottomMargin;
        mFab.animate().translationY(mFab.getHeight() + fabMargin).setInterpolator(new AccelerateInterpolator(2.0f)).start();
    }
}
