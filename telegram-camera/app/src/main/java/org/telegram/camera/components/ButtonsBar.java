package org.telegram.camera.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import org.telegram.camera.R;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Layout with fixed ratio: its height / height in xml
 *
 * @author Danil Kolikov
 */
public class ButtonsBar extends AnimatedFrameLayout {
    private final int partOfScreen;
    public ButtonsBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ButtonsBar);
        try {
            partOfScreen = ta.getInt(R.styleable.ButtonsBar_part_of_screen, 1);
        } finally {
            ta.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec) / partOfScreen;
        int newHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.getMode(heightMeasureSpec));

        super.onMeasure(widthMeasureSpec, newHeightSpec);
    }
}
