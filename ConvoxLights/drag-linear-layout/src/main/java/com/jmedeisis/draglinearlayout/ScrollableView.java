package com.jmedeisis.draglinearlayout;

/**
 * Created by carson on 6/22/15.
 * This is a sort of hacky way to allow the DragLinearLayout to accept either a
 * HorizontalScrollView or a ScrollView, because they share no suitable parent class.
 */
public interface ScrollableView {
    int getScrollX();
    int getScrollY();
    int getHeight();
    int getWidth();
    boolean removeCallbacks(Runnable action);
    void smoothScrollBy(int dx, int dy);
    boolean post(Runnable action);
}
