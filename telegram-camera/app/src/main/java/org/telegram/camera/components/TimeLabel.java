package org.telegram.camera.components;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import org.telegram.camera.R;
import org.telegram.camera.utils.ComponentUtils;

import java.util.Calendar;

/**
 * Label that shows current time of video
 *
 * @author Danil Kolikov
 */
public class TimeLabel extends FrameLayout implements Animated {
    private static final int ROUNDING_PART = 6;
    private static final long INTERVAL = 1000;

    private final int duration;
    private final ImageView rectangle;
    private final TextView label;
    private long startTime;
    private boolean resetted, started;

    public TimeLabel(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.time_label, this, true);
        rectangle = (ImageView) getChildAt(0);
        label = (TextView) getChildAt(1);
        duration = getResources().getInteger(R.integer.background_animation_duration);

        GradientDrawable background = (GradientDrawable) rectangle.getDrawable();
        background.mutate();
        background.setColor(getResources().getColor(R.color.colorBlack));
        background.setAlpha((int) (256 * getResources().getFraction(R.fraction.default_transparency, 1, 1)));
    }

    /**
     * Set current time in human-readable format
     * @param time current time
     */
    private void setTime(long time) {
        long seconds = time % 60;
        long minutes = (time / 60) % 60;
        long hours = time / 60 / 60;
        String curTime = String.format("%02d:%02d", minutes, seconds);
        if (hours != 0) {
            curTime = String.format("%02d:", hours) + curTime;
        }
        label.setText(curTime);
    }

    /**
     * Start timer. It will be increase value every 1 second
     */
    public void start() {
        startTime = Calendar.getInstance().getTimeInMillis();
        started = true;
        setTime(0);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (resetted || !started) {
                    resetted = false;
                    return;
                }
                setTime((Calendar.getInstance().getTimeInMillis() - startTime) / 1000);
                if (started) {
                    postDelayed(this, INTERVAL);
                }
            }
        }, INTERVAL);
    }

    /**
     * Stop timer
     */
    public void stop() {
        started = false;
        resetted = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int rectMargin = ComponentUtils.countMargin(heightMeasureSpec, 0.75f);
        int margin = ComponentUtils.countMargin(heightMeasureSpec, 1f);

        MarginLayoutParams params = (MarginLayoutParams) rectangle.getLayoutParams();
        params.setMargins(0, rectMargin, 0, rectMargin);

        params = (MarginLayoutParams) label.getLayoutParams();
        params.setMargins(margin - rectMargin, margin, margin - rectMargin, margin);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = rectangle.getMeasuredHeight();
        int rounding = height / ROUNDING_PART;

        GradientDrawable background = (GradientDrawable) rectangle.getDrawable();
        background.setCornerRadius(rounding);
    }

    @Override
    public void show(Animated.EndAnimationListener callback) {
        clearAnimation();
        setVisibility(VISIBLE);
        startAnimation(ComponentUtils.makeTranslationAnimation(-1, 0, false, duration, callback));
    }

    @Override
    public void hide(final Animated.EndAnimationListener callback) {
        clearAnimation();
        startAnimation(ComponentUtils.makeTranslationAnimation(0, -1, false, duration, new EndAnimationListener() {
            @Override
            public void onAnimationEnd() {
                setVisibility(GONE);
                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }
        }));
    }
}
