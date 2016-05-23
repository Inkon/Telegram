package org.telegram.camera.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import org.telegram.camera.R;

/**
 * Look at that incredible imitates real-world spinner. It can be rotated with finger touch
 *
 * @author Danil Kolikov
 */
public class SpinnerView extends View {
    // Ratio of bar height to view height
    private static final float MIDDLE_BAR_PART = 0.75f;
    private static final float SHORT_BAR_PART = 0.5f;

    private static final int BAR_COUNT = 32;
    private static final int MIDDLE_BAR = BAR_COUNT / 2;
    private static final double EPS = 0.5 / BAR_COUNT;  // Half of space between bars

    private final float partOfWidth, partOfHeight;
    private final Paint whiteColor, barColor;
    private float displace;     // Displacing of spinner from zero point
    private float previousX;    // Previous touch position
    private Bar shortBar, middleBar, bigBar;
    private RectF bar;

    private int length;         // Length of spinner
    private OnValueChangedListener listener;
    private OnSpinnerTouchedListener touchListener;

    public SpinnerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SpinnerView);
        try {
            partOfWidth = ta.getFraction(R.styleable.SpinnerView_part_of_width, 1, 1, 1);
            partOfHeight = ta.getFraction(R.styleable.SpinnerView_part_of_height, 1, 1, 1);
        } finally {
            ta.recycle();
        }

        whiteColor = new Paint();
        whiteColor.setColor(getResources().getColor(R.color.colorWhite));

        barColor = new Paint();
        barColor.setColor(getResources().getColor(R.color.colorBlue));

        displace = 0;
        previousX = 0;
        bar = new RectF();
        shortBar = new Bar();
        middleBar = new Bar();
        bigBar = new Bar();
        setWillNotDraw(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float newDisplace = displace + (x - previousX) / length;
                if (newDisplace < -0.5) {
                    newDisplace = -0.5f;
                }
                if (newDisplace > 0.5) {
                    newDisplace = 0.5f;
                }
                if (newDisplace != displace) {
                    displace = newDisplace;
                    invalidate();
                    if (listener != null) {
                        listener.onValueChanged(displace);
                    }
                }
                previousX = x;
                break;
            case MotionEvent.ACTION_DOWN:
                previousX = x;
                if (touchListener != null) {
                    touchListener.onTouch();
                }
                break;
            case MotionEvent.ACTION_UP:
                previousX = 0;
                if (touchListener != null) {
                    touchListener.onRelease();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int middleHeight = getMeasuredHeight() / 2;
        int middleWidth = length / 2;
        int startBar = 0, endBar = BAR_COUNT;
        if (displace < 0) {
            startBar = (int) (displace * BAR_COUNT);
        }
        if (displace > 0) {
            endBar += (int) (displace * BAR_COUNT);
        }

        for (int i = startBar; i < endBar; i++) {
            Paint color = whiteColor;
            float width = shortBar.width, height = shortBar.height;
            if (i == MIDDLE_BAR) {
                width = middleBar.width;
                height = middleBar.height;
            }
            double phase = (double) i / BAR_COUNT - displace;
            if (phase < 0 || phase > 1) {
                continue;
            }
            if (Math.abs(0.5 - phase) < EPS) {
                height = (float) (height + (bigBar.height - height) *
                        (1 - 2 * Math.abs(0.5 - phase) * BAR_COUNT));
                width = height * Bar.BAR_RATIO;
            }

            float rounding = width * Bar.BAR_ROUNDING_RATIO;
            double position = (double) i / BAR_COUNT;

            if (0.5 - EPS <= position && position <= 0.5 + EPS + displace ||
                    0.5 + displace - EPS <= position && position <= 0.5) {
                color = barColor;
            }
            setAlpha(color, Math.PI * phase);

            float upperLeftHeight = middleHeight - height / 2;
            float upperLeftWidth = (float) (length * Math.cos(Math.PI * phase) / 2) + middleWidth;
            bar.left = upperLeftWidth - width / 2;
            bar.right = upperLeftWidth + width;
            bar.top = upperLeftHeight;
            bar.bottom = upperLeftHeight + height;
            canvas.drawRoundRect(bar, rounding, rounding, color);
        }
    }

    private void setAlpha(Paint paint, double phase) {
        int gray = (int) (255 * (Math.abs(Math.sin(phase))));
        paint.setAlpha(gray);
    }

    /**
     * Get value of spinner
     *
     * @return Returns value between -0.5 and 0.5
     */
    public float getValue() {
        return displace;
    }

    /**
     * Set value of spinner
     *
     * @param value new value. Must be between -0.5 and 0.5
     */
    public void setValue(float value) {
        this.displace = value;
        invalidate();
    }

    /**
     * Set listener to value change
     *
     * @param listener A listener
     * @see OnValueChangedListener
     */
    public void setListener(OnValueChangedListener listener) {
        this.listener = listener;
    }

    /**
     * Sets touch listener
     *
     * @param touchListener A listener
     * @see OnSpinnerTouchedListener
     */
    public void setTouchListener(OnSpinnerTouchedListener touchListener) {
        this.touchListener = touchListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = (int) (MeasureSpec.getSize(heightMeasureSpec) * partOfHeight);
        shortBar.setHeight(height * SHORT_BAR_PART);
        middleBar.setHeight(height * MIDDLE_BAR_PART);
        bigBar.setHeight(height);
        length = (int) (MeasureSpec.getSize(widthMeasureSpec) * partOfWidth);
        setMeasuredDimension(length, height);
    }

    /**
     * Listener of changing value
     */
    public interface OnValueChangedListener {
        /**
         * Will be called when value is changed
         *
         * @param newValue Value between -0.5 and 0.5
         */
        void onValueChanged(float newValue);
    }

    /**
     * Listener of a view touch
     */
    public interface OnSpinnerTouchedListener {
        /**
         * Will be called when user touches this spinner
         */
        void onTouch();

        /**
         * Will be called when user releases
         */
        void onRelease();
    }

    /**
     * Class that is used for storing size of bars
     */
    private class Bar {
        private static final float BAR_RATIO = 0.05f;
        private static final float BAR_ROUNDING_RATIO = 0.4f;

        float height, width;

        void setHeight(float height) {
            this.height = height;
            width = height * BAR_RATIO;
        }
    }
}
