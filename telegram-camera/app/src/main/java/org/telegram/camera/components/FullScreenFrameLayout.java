package org.telegram.camera.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * {@link FrameLayout} that takes full screen
 *
 * @author Danil Kolikov
 */
public class FullScreenFrameLayout extends FrameLayout {
    public FullScreenFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int newWidth = getResources().getDisplayMetrics().widthPixels;
        int newHeight = getResources().getDisplayMetrics().heightPixels;

        super.onMeasure(MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY));
    }
}
