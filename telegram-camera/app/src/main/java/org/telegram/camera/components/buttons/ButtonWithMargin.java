package org.telegram.camera.components.buttons;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import org.telegram.camera.R;
import org.telegram.camera.components.Animated;
import org.telegram.camera.components.Marginable;

/**
 * Button with margin that can be specified in xml
 *
 * @see Marginable
 * @author Danil Kolikov
 */
public class ButtonWithMargin extends FrameLayout implements Animated, Marginable {
    private final float marginPart;
    private final int showAnimation;
    private final int hideAnimation;

    public ButtonWithMargin(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ButtonWithMargin);
        try {
            marginPart = ta.getFraction(R.styleable.ButtonWithMargin_margin_part, 1, 1, 0);
            showAnimation = ta.getResourceId(R.styleable.ButtonWithMargin_show_animation, R.anim.expand_overshoot);
            hideAnimation = ta.getResourceId(R.styleable.ButtonWithMargin_hide_animation, R.anim.collapse);
        } finally {
            ta.recycle();
        }
    }

    @Override
    public void show(final EndAnimationListener callback) {
        setVisibility(VISIBLE);
        Animation expand = AnimationUtils.loadAnimation(getContext(), showAnimation);
        expand.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        startAnimation(expand);
    }

    @Override
    public void hide(final EndAnimationListener callback) {
        Animation collapse = AnimationUtils.loadAnimation(getContext(), hideAnimation);
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
}
