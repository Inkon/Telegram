package org.telegram.camera.components.buttons;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import org.telegram.camera.R;
import org.telegram.camera.components.AnimatedFrameLayout;
import org.telegram.camera.utils.CameraHolder;
import org.telegram.camera.utils.ComponentUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Button which represents state of camera's flash
 *
 * @author Danil Kolikov
 */
public class FlashButton extends AnimatedFrameLayout implements View.OnClickListener {
    private static final int[] icons = new int[]{R.drawable.flash_auto, R.drawable.flash_on, R.drawable.flash_off};
    private static final String BASE = "base";
    private static final String STATE = "state";
    private static final String STATES = "states";

    private final ImageView icon, animated;
    private final Animation fallAnimation, fadeAnimation;
    private StateChangedListener listener;
    private int currentState;
    private int[] states;

    public FlashButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.flash_button, this, true);
        icon = (ImageView) getChildAt(0);
        animated = (ImageView) getChildAt(1);

        icon.setVisibility(VISIBLE);
        animated.setVisibility(GONE);

        fallAnimation = AnimationUtils.loadAnimation(context, R.anim.fall);
        fadeAnimation = AnimationUtils.loadAnimation(context, R.anim.fade);

        setOnClickListener(this);
        states = new int[]{CameraHolder.FLASH_AUTO, CameraHolder.FLASH_OFF, CameraHolder.FLASH_ON};
        setState(0);
    }

    /**
     * Set possible states of button
     *
     * @param states possible states
     * @see CameraHolder#getFlashStates()
     */
    public void setStates(List<Integer> states) {
        this.states = new int[states.size()];
        for (int i = 0; i < states.size(); i++) {
            this.states[i] = states.get(i);
        }
        setState(this.states[0]);
    }

    /**
     * Get current state of flash
     * @return Current state
     */
    public int getState() {
        return states[currentState];
    }

    /**
     * Set new state of flash
     * @param state New state
     */
    public void setState(int state) {
        icon.setImageResource(icons[state]);
        currentState = state;
    }

    /**
     * Set new listener of changing values
     * @param listener New listener
     */
    public void setListener(StateChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if (states.length == 1) {
            if (listener != null) {
                listener.onFlashStateChanged(this, states[0]);
            }
            return;
        }

        final int newState = (currentState + 1) % states.length;
        final AtomicInteger counter = new AtomicInteger(2);
        fadeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setState(newState);
                if (counter.decrementAndGet() == 0) {
                    animated.setVisibility(GONE);
                    if (listener != null) {
                        listener.onFlashStateChanged(FlashButton.this, states[newState]);
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        fallAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (counter.decrementAndGet() == 0) {
                    animated.setVisibility(GONE);
                    if (listener != null) {
                        listener.onFlashStateChanged(FlashButton.this, states[newState]);
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        icon.clearAnimation();
        animated.clearAnimation();
        animated.setImageResource(icons[newState]);
        icon.startAnimation(fadeAnimation);

        animated.setVisibility(VISIBLE);
        animated.startAnimation(fallAnimation);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int margin = ComponentUtils.countMargin(heightMeasureSpec, 1);

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            ViewGroup.MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
            params.setMargins(margin, margin, margin, margin);
        }
        super.onMeasure(heightMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void show(final EndAnimationListener callback) {
        setVisibility(VISIBLE);
        Animation show = AnimationUtils.loadAnimation(getContext(), R.anim.fall);
        show.setAnimationListener(new Animation.AnimationListener() {
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
        startAnimation(show);
    }

    @Override
    public void hide(final EndAnimationListener callback) {
        Animation hide = AnimationUtils.loadAnimation(getContext(), R.anim.fade);
        hide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setVisibility(GONE);
                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(hide);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle values = new Bundle();
        values.putParcelable(BASE, super.onSaveInstanceState());
        values.putInt(STATE, currentState);
        values.putIntArray(STATE, states);
        return values;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle saved = (Bundle) state;
            super.onRestoreInstanceState(saved.getParcelable(BASE));
            currentState = saved.getInt(STATE);
            states = saved.getIntArray(STATES);
            setState(currentState);
        }
    }

    /**
     * Listener of changing button's state
     */
    public interface StateChangedListener {
        /**
         * Will be called when state is changed
         *
         * @param v        Current flash button
         * @param newState New state. States are located in {@link CameraHolder}
         */
        void onFlashStateChanged(FlashButton v, int newState);
    }
}
