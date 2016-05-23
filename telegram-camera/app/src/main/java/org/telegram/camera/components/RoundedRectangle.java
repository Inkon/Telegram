package org.telegram.camera.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import org.telegram.camera.R;

/**
 * Customizable rectangle with rounded corners
 *
 * @author Danil Kolikov
 */
public class RoundedRectangle extends View {
    private final RectF rect;   // Rectangle
    private final Paint paint;  // Color
    private float begin, end, rounding, verticalPart, horizontalPart;   // Ratios
    private int color;

    public RoundedRectangle(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RoundedRectangle);
        try {
            begin = ta.getFraction(R.styleable.RoundedRectangle_begin_position, 1, 1, 0);
            end = ta.getFraction(R.styleable.RoundedRectangle_end_position, 1, 1, 1);
            rounding = ta.getFraction(R.styleable.RoundedRectangle_rounding, 1, 1, 0);
            verticalPart = ta.getFraction(R.styleable.RoundedRectangle_vertical_part, 1, 1, 1);
            horizontalPart = ta.getFraction(R.styleable.RoundedRectangle_horizontal_part, 1, 1, 1);
            color = ta.getColor(R.styleable.RoundedRectangle_rectangle_color, getResources().getColor(R.color.colorWhite));
        } finally {
            ta.recycle();
        }
        rect = new RectF();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
    }

    /**
     * Get begin of a rectangle
     *
     * @return Value between 0 and 1
     */
    public float getBegin() {
        return begin;
    }

    /**
     * Set begin of a rectangle
     *
     * @param begin Value between 0 and 1
     */
    public void setBegin(float begin) {
        this.begin = begin;
        invalidate();
    }

    /**
     * Get end of a rectangle
     *
     * @return Value between 0 and 1
     */
    public float getEnd() {
        return end;
    }

    /**
     * Set end of a rectangle
     *
     * @param end Value between 0 and 1
     */
    public void setEnd(float end) {
        this.end = end;
        invalidate();
    }

    /**
     * Get rounding of a rectangle. Real rounding is rounding * height of rectangle
     *
     * @return Value between 0 and 1
     */
    public float getRounding() {
        return rounding;
    }

    /**
     * Set end of a rectangle
     *
     * @param rounding Value between 0 and 1
     */
    public void setRounding(float rounding) {
        this.rounding = rounding;
    }

    /**
     * Get part of a height of a view occupied with rectangle
     *
     * @return Value between 0 and 1
     */
    public float getVerticalPart() {
        return verticalPart;
    }

    /**
     * Set end of a rectangle
     *
     * @param verticalPart Value between 0 and 1
     */
    public void setVerticalPart(float verticalPart) {
        this.verticalPart = verticalPart;
        invalidate();
    }

    /**
     * Get part of a width of a view occupied with rectangle
     *
     * @return Value between 0 and 1
     */
    public float getHorizontalPart() {
        return horizontalPart;
    }

    /**
     * Set part of a width of a view occupied with rectangle
     *
     * @param horizontalPart Value between 0 and 1
     */
    public void setHorizontalPart(float horizontalPart) {
        this.horizontalPart = horizontalPart;
        invalidate();
    }

    /**
     * Set scale of a rectangle
     *
     * @param scale Value between 0 and 1
     */
    public void setScale(float scale) {
        this.horizontalPart = scale;
        this.verticalPart = scale;
        invalidate();
    }

    /**
     * Get color of rectangle
     *
     * @return A color
     */
    public int getColor() {
        return color;
    }

    /**
     * Set color of rectangle
     *
     * @param color A color
     */
    public void setColor(int color) {
        this.color = getResources().getColor(color);
        paint.setColor(this.color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        float rectHeight = height * verticalPart;
        float rectWidth = width * horizontalPart;

        float round = rectHeight * rounding;
        float variableWidth = rectWidth - rectHeight;

        rect.left = (width - rectWidth) / 2 + variableWidth * begin;
        rect.right = (width - rectWidth) / 2 + variableWidth * end + rectHeight;
        rect.top = (height - rectHeight) / 2;
        rect.bottom = rect.top + rectHeight;
        canvas.drawRoundRect(rect, round, round, paint);
    }
}
