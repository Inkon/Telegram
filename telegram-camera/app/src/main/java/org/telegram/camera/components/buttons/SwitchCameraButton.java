package org.telegram.camera.components.buttons;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import org.telegram.camera.R;
import org.telegram.camera.components.Ring;

/**
 * Button that switches camera
 *
 * @author Danil Kolikov
 */
public class SwitchCameraButton extends ButtonWithMargin {
    private static final int RING_PART = 3;
    private final float ringRatio;

    private ImageView icon;
    private Ring ring;
    private boolean front;

    public SwitchCameraButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.switch_camera_button, this, true);
        ringRatio = getResources().getFraction(R.fraction.ring_default_ratio, 1, 1);
        icon = (ImageView) getChildAt(0);
        ring = (Ring) getChildAt(1);
        front = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int diam = getMeasuredHeight();
        int newHeight = getMeasuredHeightAndState();
        int ringDiam = diam / RING_PART;
        int resultSpec = MeasureSpec.makeMeasureSpec(diam, MeasureSpec.EXACTLY);
        int ringSpec = MeasureSpec.makeMeasureSpec(ringDiam, MeasureSpec.EXACTLY);

        icon.measure(resultSpec, newHeight);
        ring.measure(ringSpec, ringSpec);

        setMeasuredDimension(resultSpec, newHeight);
    }

    private ObjectAnimator makeRingAnimation(float start, float end) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(ring, "ratio", start, end);
        animator.setDuration(getResources().getInteger(R.integer.rotation_duration));
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        return animator;
    }

    /**
     * Play animation of rotation of icon
     *
     * @param callback Callback
     * @see org.telegram.camera.components.Animated.EndAnimationListener
     */
    public void rotateIcon(final EndAnimationListener callback) {
        Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_ccw);
        ObjectAnimator animator;
        if (front) {
            animator = makeRingAnimation(ringRatio, 0);
        } else {
            animator = makeRingAnimation(0, ringRatio);
        }

        if (callback != null) {
            rotate.setAnimationListener(new Animation.AnimationListener() {
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
        }
        icon.clearAnimation();
        icon.startAnimation(rotate);
        animator.start();
    }

    /**
     * Is front camera on now
     * @return True, if now button represents front mode
     */
    public boolean isFront() {
        return front;
    }

    /**
     * Set front value
     * @param front New value
     */
    public void setFront(boolean front) {
        this.front = front;
    }

    @Override
    public void setOnClickListener(final OnClickListener l) {
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                front = !front;
                if (l != null) {
                    l.onClick(v);
                }
            }
        });
    }
}
