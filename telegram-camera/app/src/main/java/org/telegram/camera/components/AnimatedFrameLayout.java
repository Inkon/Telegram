package org.telegram.camera.components;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import org.telegram.camera.R;
import org.telegram.camera.utils.ComponentUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * FrameLayout with animations of showing, hiding, falling and raising
 *
 * @author Danil Kolikov
 * @see Animated
 */
public class AnimatedFrameLayout extends FrameLayoutWithMargin implements Animated {
    public final float finalTransparency;   // final transparency of layout
    private final long duration;            // Duration of animation

    public AnimatedFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        finalTransparency = getResources().getFraction(R.fraction.default_transparency, 1, 1);
        duration = getResources().getInteger(R.integer.background_animation_duration);
    }

    /**
     * Set alpha value of layout's background
     *
     * @param alpha New alpha value
     */
    public void setBackgroundAlpha(float alpha) {
        Drawable background = getBackground();
        background.mutate();
        background.setAlpha((int) (255 * alpha));
    }


    @Override
    public void show(final EndAnimationListener callback) {
        final AtomicInteger counter = new AtomicInteger(getChildCount());
        EndAnimationListener listener = new EndAnimationListener() {
            @Override
            public void onAnimationEnd() {
                if (counter.decrementAndGet() == 0 && callback != null) {
                    callback.onAnimationEnd();
                }
            }
        };
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof Animated) {
                ((Animated) view).show(listener);
            } else {
                view.setVisibility(VISIBLE);
                listener.onAnimationEnd();
            }
        }
    }

    @Override
    public void hide(final EndAnimationListener callback) {
        final AtomicInteger counter = new AtomicInteger(getChildCount());
        EndAnimationListener listener = new EndAnimationListener() {
            @Override
            public void onAnimationEnd() {
                if (counter.decrementAndGet() == 0) {
                    if (callback != null) {
                        callback.onAnimationEnd();
                    }
                }
            }
        };
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof Animated) {
                ((Animated) view).hide(listener);
            } else {
                listener.onAnimationEnd();
            }
        }
    }

    /**
     * Play animation of falling from the screen
     *
     * @param callback Callback
     * @see org.telegram.camera.components.Animated.EndAnimationListener
     */
    public void fall(final EndAnimationListener callback) {
        clearAnimation();
        startAnimation(ComponentUtils.makeTranslationAnimation(0, 1, true, duration,
                new EndAnimationListener() {
                    @Override
                    public void onAnimationEnd() {
                        setVisibility(GONE);
                        if (callback != null) {
                            callback.onAnimationEnd();
                        }
                    }
                }));
    }

    /**
     * Play animation of rising to the screen
     *
     * @param callback Callback
     * @see org.telegram.camera.components.Animated.EndAnimationListener
     */
    public void rise(final EndAnimationListener callback) {
        clearAnimation();
        setVisibility(VISIBLE);
        startAnimation(ComponentUtils.makeTranslationAnimation(1, 0, true,
                duration, new EndAnimationListener() {
                    @Override
                    public void onAnimationEnd() {
                        if (callback != null) {
                            callback.onAnimationEnd();
                        }
                    }
                }));
    }

    private Animator makeAlphaAnimation(float start, final float end, final Animated.EndAnimationListener callback) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "backgroundAlpha", start, end);
        animator.setDuration(duration);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        return animator;
    }

    /**
     * Make background transparent
     *
     * @param callback Callback
     * @see org.telegram.camera.components.Animated.EndAnimationListener
     */
    public void makeTransparent(final Animated.EndAnimationListener callback) {
        setBackgroundAlpha(1f);
        makeAlphaAnimation(1, finalTransparency, new Animated.EndAnimationListener() {
            @Override
            public void onAnimationEnd() {
                setBackgroundAlpha(finalTransparency);
                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }
        }).start();
    }

    /**
     * Make background clear
     *
     * @param callback Callback
     * @see org.telegram.camera.components.Animated.EndAnimationListener
     */
    public void makeClear(final Animated.EndAnimationListener callback) {
        setBackgroundAlpha(1f);
        makeAlphaAnimation(finalTransparency, 1, callback).start();
    }
}
