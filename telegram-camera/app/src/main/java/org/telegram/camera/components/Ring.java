package org.telegram.camera.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import org.telegram.camera.R;

/**
 * Ring with customizable inner radius
 *
 * @author Danil Kolikov
 */
public class Ring extends View {
    private final Paint paint;  // Color of ring
    private float ratio;    // Ratio of inner radius / radius of ring

    public Ring(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.Ring);
        try {
            ratio = ta.getFraction(R.styleable.Ring_inner_circle_ratio, 1, 1, 0);
        } finally {
            ta.recycle();
        }
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(R.color.colorWhite));
        paint.setStyle(Paint.Style.STROKE);
    }

    /**
     * Get current inner circle ratio
     *
     * @return Value between 0 and 1
     */
    public float getRatio() {
        return ratio;
    }

    /**
     * Set current inner circle ratio
     *
     * @param ratio Value between 0 and 1
     */
    public void setRatio(float ratio) {
        this.ratio = ratio;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getMeasuredWidth();
        int radius = width / 2;
        float border = radius * (1 - ratio);
        float finalRadius = radius - border / 2;
        paint.setStrokeWidth(border);
        canvas.drawCircle(width / 2, width / 2, finalRadius, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, heightMeasureSpec);
    }
}
