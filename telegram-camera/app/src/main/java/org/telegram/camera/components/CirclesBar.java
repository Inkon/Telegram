package org.telegram.camera.components;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.*;
import android.widget.ImageView;
import org.telegram.camera.R;
import org.telegram.camera.components.buttons.ButtonWithMargin;

/**
 * Bark with swapping circles
 * @author Gleb Zernov
 */

public class CirclesBar extends ButtonWithMargin {
    private ImageView leftCircle,
            rightCircle,
            transparentLeftCircle;
    private RoundedRectangle follower;

    private final int partOfScreen = 50, animationDuration = 200;

    public CirclesBar(Context context, AttributeSet attrs) {

        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.circles, this, true);
        leftCircle = (ImageView) getChildAt(0);
        rightCircle = (ImageView) getChildAt(1);
        transparentLeftCircle = (ImageView) getChildAt(2);
        follower = (RoundedRectangle) getChildAt(3);

        setColors(R.color.colorWhite, leftCircle);
        setColors(R.color.transparentWhite, rightCircle, transparentLeftCircle);
        follower.setVisibility(GONE);
    }

    private void setColors(int color, ImageView... imageViews) {
        for (ImageView imageView : imageViews) {
            GradientDrawable drawable = (GradientDrawable) imageView.getDrawable();
            drawable.mutate();
            drawable.setColor(getResources().getColor(color));
        }
    }

    /**
     * Animation invoked when swapping from first (left) to second (right) view
     * In application it equals swapping from recording mode to camera mode
     */

    public void rightAnimation() {
        TranslateAnimation animation = new TranslateAnimation(leftCircle.getX(),
                rightCircle.getX(),
                leftCircle.getY(), rightCircle.getY());
        transparentLeftCircle.setVisibility(GONE);
        Animation.AnimationListener listener = new Listeners.EndAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Animator animator = ObjectAnimator.ofFloat(follower, "end", 0, 1);
                follower.setBegin(0);
                animator.addListener(new Listeners.EndAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Animator secondAnimator = ObjectAnimator.ofFloat(follower, "end", 1, 0);
                        secondAnimator.setDuration(animationDuration);
                        secondAnimator.addListener(new Listeners.EndAnimatorListener() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                follower.setBegin(1);
                                follower.setVisibility(GONE);
                                transparentLeftCircle.setVisibility(VISIBLE);
                            }
                        });
                        secondAnimator.start();

                    }
                });
                animator.setDuration(animationDuration);
                follower.setVisibility(VISIBLE);
                animator.start();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                leftCircle.setVisibility(GONE);
                setColors(R.color.colorWhite, rightCircle);
            }
        };
        animation.setAnimationListener(listener);
        animation.setDuration(animationDuration);
        leftCircle.startAnimation(animation);
    }

    /**
     * Animation invoked when swapping from second (right) to first (left) view
     * In application it equals swapping from recording mode to camera mode
     */

    public void leftAnimation() {
        TranslateAnimation animation = new TranslateAnimation(rightCircle.getX(), leftCircle.getX(), leftCircle.getY(), rightCircle.getY());
        Animation.AnimationListener listener = new Listeners.EndAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                leftCircle.setVisibility(VISIBLE);
                setColors(R.color.transparentWhite, rightCircle);
                rightCircle.setVisibility(GONE);
                follower.setVisibility(VISIBLE);
                Animator animator = ObjectAnimator.ofFloat(follower, "begin", 1, 0);
                follower.setEnd(1);
                animator.setDuration(animationDuration);
                animator.addListener(new Listeners.EndAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Animator secondAnimator = ObjectAnimator.ofFloat(follower, "begin", 0, 1);
                        secondAnimator.setDuration(animationDuration);
                        secondAnimator.addListener(new Listeners.EndAnimatorListener() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                setColors(R.color.transparentWhite, rightCircle);
                                rightCircle.setVisibility(VISIBLE);
                                follower.setEnd(0);
                                follower.setVisibility(GONE);
                            }
                        });
                        secondAnimator.start();
                    }
                });
                animator.start();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        };
        animation.setAnimationListener(listener);
        animation.setDuration(animationDuration);
        leftCircle.startAnimation(animation);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int diam = MeasureSpec.getSize(heightMeasureSpec) / partOfScreen;
        int resultSpec = MeasureSpec.makeMeasureSpec(diam, MeasureSpec.EXACTLY);

        leftCircle.measure(resultSpec, resultSpec);
        rightCircle.measure(resultSpec, resultSpec);
        transparentLeftCircle.measure(resultSpec, resultSpec);


        int newResultSpec = MeasureSpec.makeMeasureSpec(3 * diam, MeasureSpec.EXACTLY);

        follower.measure(newResultSpec, resultSpec);
        rightCircle.setX(leftCircle.getX() + follower.getMeasuredWidth() - diam);
        setMeasuredDimension(newResultSpec, resultSpec);
    }
}
