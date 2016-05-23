package org.telegram.camera.components.buttons;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import org.telegram.camera.R;
import org.telegram.camera.components.Animated;
import org.telegram.camera.components.Marginable;
import org.telegram.camera.utils.ComponentUtils;

/**
 * Circle button with radius equal to height of button
 *
 * @author Danil Kolikov
 */
public class CircleButton extends Button implements Animated, Marginable {
    private final float marginPart;

    public CircleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleButton);
        try {
            marginPart = ta.getFraction(R.styleable.CircleButton_margin_part, 1, 1, 0);
        } finally {
            ta.recycle();
        }
    }

    @Override
    public void show(final EndAnimationListener callback) {
        setVisibility(VISIBLE);
        Animation expand = AnimationUtils.loadAnimation(getContext(), R.anim.expand_overshoot);
        expand.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                callback.onAnimationEnd();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        startAnimation(expand);
    }

    @Override
    public void hide(final EndAnimationListener callback) {
        Animation collapse = AnimationUtils.loadAnimation(getContext(), R.anim.collapse);
        collapse.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setVisibility(View.GONE);
                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(collapse);
    }

    @Override
    public float getMarginPart() {
        return marginPart;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(heightMeasureSpec, heightMeasureSpec);
    }
}
