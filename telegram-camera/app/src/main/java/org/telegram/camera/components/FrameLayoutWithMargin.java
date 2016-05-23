package org.telegram.camera.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import org.telegram.camera.utils.ComponentUtils;

/**
 * Frame layout that can place components that implement {@link Marginable} according to their desired margin
 *
 * @see Marginable
 * @see ImageViewWithMargin
 * @author Danil Kolikov
 */
public class FrameLayoutWithMargin extends FrameLayout {
    public FrameLayoutWithMargin(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof Marginable) {
                MarginLayoutParams params = (MarginLayoutParams)child.getLayoutParams();
                int margin = ComponentUtils.countMargin(heightMeasureSpec, ((Marginable) child).getMarginPart());
                params.setMargins(margin, margin, margin, margin);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
