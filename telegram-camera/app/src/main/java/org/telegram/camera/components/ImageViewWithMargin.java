package org.telegram.camera.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import org.telegram.camera.R;
import org.telegram.camera.utils.ComponentUtils;

/**
 * Image view with value of margin that can be specified in xml
 *
 * @see Marginable
 * @see FrameLayoutWithMargin
 * @author Danil Kolikov
 */
public class ImageViewWithMargin extends ImageView implements Marginable, Animated {
    private final float marginPart;

    public ImageViewWithMargin(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ImageViewWithMargin);
        try {
            marginPart = ta.getFraction(R.styleable.ImageViewWithMargin_margin_part, 1, 1, 0);
        } finally {
            ta.recycle();
        }
    }

    @Override
    public void show(final EndAnimationListener callback) {
        setVisibility(VISIBLE);
        Animation expand = AnimationUtils.loadAnimation(getContext(), R.anim.become_clear);
        expand.setDuration(getResources().getInteger(R.integer.expand_duration));
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
        Animation collapse = AnimationUtils.loadAnimation(getContext(), R.anim.become_transparent);
        collapse.setDuration(getResources().getInteger(R.integer.collapse_duration));
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
