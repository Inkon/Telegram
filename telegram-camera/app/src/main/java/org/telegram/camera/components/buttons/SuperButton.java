package org.telegram.camera.components.buttons;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import org.telegram.camera.R;
import org.telegram.camera.components.Animated;
import org.telegram.camera.components.Listeners;
import org.telegram.camera.components.RoundedRectangle;

/**
 * Button which appears on all stages in {@link org.telegram.camera.components.fragments.CameraFragment}
 *
 * This button consists of five {@link RoundedRectangle rectangles}, two of them are supporting
 * and three of them are main and have names: <tt>big</tt>, <tt></tt>medium</tt> and <tt>small</tt>
 * @author Gleb Zernov
 **/

public class SuperButton extends ButtonWithMargin {
    private static final float RATIO = 1.25f;
    private final RoundedRectangle big, middle, small, longTapSmall, longTapBig;
    public static final int
            MODE_CAMERA = 0,
            MODE_RECORD = 1,
            MODE_STOP   = 2;

    private final int
            ANIMATION_DEFAULT_DURATION,
            ANIMATION_FAST,
            ANIMATION_FASTEST;

    private final float
            CAMERA_MIDDLE_PART,
            CAMERA_SMALL_PART,

            RECORD_CIRCLE_PART,
            RECORD_ROUNDING_PART,

            STOP_ROUNDING_PART,
            STOP_SQUARE_PART;

    /**
     * Camera mode. This button have three states: camera (taking picture), video (prepared for record, waiting
     * for user to begin recording), and stop (currently recording video, waiting for stop)
     */
    public int mode = MODE_CAMERA;

    public SuperButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.super_button, this, true);

        big = (RoundedRectangle)getChildAt(0);
        middle = (RoundedRectangle)getChildAt(1);
        small = (RoundedRectangle)getChildAt(2);
        longTapBig = (RoundedRectangle) getChildAt(3);
        longTapSmall = (RoundedRectangle) getChildAt(4);

        CAMERA_MIDDLE_PART = 1f - 1f / getResources().getInteger(R.integer.camera_middle_part);
        CAMERA_SMALL_PART = 1f / getResources().getInteger(R.integer.camera_small_part);
        RECORD_CIRCLE_PART = 1f / getResources().getInteger(R.integer.record_circle_part);
        RECORD_ROUNDING_PART = 1f / getResources().getInteger(R.integer.record_rounding_part);
        STOP_ROUNDING_PART = 1f / getResources().getInteger(R.integer.stop_rounding_part);
        STOP_SQUARE_PART = 1f / getResources().getInteger(R.integer.stop_square_part);

        ANIMATION_DEFAULT_DURATION = getResources().getInteger(R.integer.animation_default_duration);
        ANIMATION_FAST = getResources().getInteger(R.integer.animation_fast);
        ANIMATION_FASTEST = getResources().getInteger(R.integer.animation_fastest);

        big.setRounding(RECORD_ROUNDING_PART);
        longTapBig.setVisibility(GONE);
        longTapSmall.setVisibility(GONE);
        changeMode(mode);
    }

    /**
     * Changing current mode of button.
     * This button have three states: camera (taking picture), video (prepared for record, waiting
     * for user to begin recording), and stop (currently recording video, waiting for stop)
     * @param newMode setting a new mode
     */
    public void changeMode(int newMode){
        mode = newMode;
        switch (mode){
            case MODE_CAMERA:
                big.setColor(R.color.colorWhite);
                middle.setColor(R.color.colorBlue);
                small.setColor(R.color.colorBlueLight);
                middle.setScale(CAMERA_MIDDLE_PART);
                small.setScale(CAMERA_SMALL_PART);

                longTapBig.setVisibility(GONE);
                longTapSmall.setVisibility(GONE);
                big.setVisibility(VISIBLE);
                middle.setVisibility(VISIBLE);
                small.setVisibility(VISIBLE);
                squaring(big, middle, small);
                break;
            case MODE_RECORD:
                big.setColor(R.color.colorWhite);
                small.setColor(R.color.colorRed);

                small.setScale(RECORD_CIRCLE_PART);

                big.setVisibility(VISIBLE);
                middle.setVisibility(GONE);
                small.setVisibility(VISIBLE);
                squaring(small, middle);
                break;
            case MODE_STOP:
                middle.setColor(R.color.colorRed);
                small.setColor(R.color.colorWhite);

                small.setRounding(STOP_ROUNDING_PART);
                small.setScale(STOP_SQUARE_PART);

                big.setVisibility(GONE);
                middle.setVisibility(VISIBLE);
                small.setVisibility(VISIBLE);
                squaring(small, middle);
                break;
        }
    }

    /**
     * Make all rectangles squares
     * @param rectangles array of rectangles which will mutate to squares
     */
    private void squaring(RoundedRectangle ... rectangles){
        for (RoundedRectangle rectangle : rectangles){
            rectangle.setBegin(0.5f);
            rectangle.setEnd(0.5f);
        }
    }

    /**
     * Starting animation due to {@link #mode} and <tt>newMode</tt>
     * @param newMode new mode
     * @see #changeMode(int)
     */
    public void startAnimation(int newMode){
        setClickable(false);
        switch (mode){
            case MODE_CAMERA:
                switch (newMode){
                    case MODE_CAMERA:
                        takePictureAnimation();
                        break;
                    case MODE_RECORD:
                        rightSwipeAnimation();
                        break;
                }
                break;
            case MODE_RECORD:
                if (newMode == MODE_RECORD){
                    recordToStopAnimation();
                } else {
                    leftSwipeAnimation();
                }
                break;
        }
        mode = newMode;
    }

    /**
     * Animation of tapping camera button
     */
    private void takePictureAnimation() {
        Animation imButtonAnimation = AnimationUtils.loadAnimation(
                getContext(), R.anim.camera_button_animation);
        middle.startAnimation(imButtonAnimation);
    }

    /**
     * Animation of swipe from camera mode to video record mode
     */
    private void rightSwipeAnimation(){
        setClickable(false);
        Animator[] animators = rightSwipeAnimators(big, middle, small);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animators);
        set.setDuration(ANIMATION_DEFAULT_DURATION);
        set.addListener(new Listeners.EndAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Animator animator = ObjectAnimator.ofFloat(small, "scale", 0f, RECORD_CIRCLE_PART);
                squaring(small);
                small.setColor(R.color.colorRed);
                small.setAlpha(1f);
                animator.setDuration(ANIMATION_FAST);
                animator.addListener(new Listeners.EndAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setClickable(true);
                        changeMode(MODE_RECORD);
                        middle.setAlpha(1f);
                    }
                });
                animator.start();
            }
        });
        set.start();
    }

    /**
     * Create animators for {@link #rightSwipeAnimation()} to <tt>big</tt>, <tt>medium</tt> and <tt>small</tt>
     * rounded rectangles (parts of button)
     * @param rectangles parts of button
     * @return rectangle animators
     * @see #rightSwipeAnimation()
     */
    private Animator[] rightSwipeAnimators(RoundedRectangle ... rectangles){
        Animator[] result = new Animator[rectangles.length * 3];
        int i = -3;
        for (RoundedRectangle rectangle : rectangles){
            Animator begin = ObjectAnimator.ofFloat(rectangle, "begin", 0.5f, 0);
            Animator alpha = ObjectAnimator.ofFloat(rectangle, "alpha", 1f, rectangle.equals(big) ? 1 : 0);
            Animator end = ObjectAnimator.ofFloat(rectangle, "end", 0.5f, 1f);
            System.arraycopy(new Animator[]{begin, alpha, end}, 0, result, i += 3, 3);
        }
        return result;
    }

    /**
     * Animation opposite to {@link #rightSwipeAnimation()}
     */
    private void leftSwipeAnimation(){
        setClickable(false);
        middle.setAlpha(0);
        middle.setVisibility(VISIBLE);
        middle.setRounding(RECORD_ROUNDING_PART);
        small.setColor(R.color.colorBlueLight);
        middle.setColor(R.color.colorBlue);


        Animator[] animators = leftSwipeAnimators(middle, big);

        animators[animators.length - 1] = ObjectAnimator.ofFloat(small, "scale", 0f, CAMERA_SMALL_PART);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        animatorSet.addListener(new ChangeModeOnEndAnimatorListener(MODE_CAMERA));
        animatorSet.setDuration(ANIMATION_DEFAULT_DURATION);
        animatorSet.start();
    }

    /**
     * Create animators as {@link #rightSwipeAnimation()} for {@link #leftSwipeAnimation()}
     * @param rectangles parts of button
     * @return animators for button
     */
    private Animator[] leftSwipeAnimators(RoundedRectangle ... rectangles){
        Animator[] result = new Animator[rectangles.length * 3 + 1];
        int i = -3;
        for (RoundedRectangle rectangle : rectangles){
            Animator begin = ObjectAnimator.ofFloat(rectangle, "begin", 0, 0.5f);
            Animator alpha = ObjectAnimator.ofFloat(rectangle, "alpha", rectangle.equals(big) ? 1 : 0, 1f);
            Animator end = ObjectAnimator.ofFloat(rectangle, "end", 1f, 0.5f);
            System.arraycopy(new Animator[]{begin, alpha, end}, 0, result, i += 3, 3);
        }
        return  result;
    }

    /**
     * Animation of long tap to camera button, switching from camera mode to currently recording
     * @param callback action which will be invoke on animation complete
     * @see org.telegram.camera.components.Animated.EndAnimationListener
     */
    public void longTapAnimation(final EndAnimationListener callback) {
        Log.d("abc", "on anim");
        setClickable(false);
        squaring(longTapBig, longTapSmall);
        longTapSmall.setVisibility(VISIBLE);
        longTapSmall.setRounding(1);
        longTapSmall.setColor(R.color.colorWhite);
        float fromSmall = 0.1f;
        longTapSmall.setScale(fromSmall);

        longTapBig.setVisibility(VISIBLE);
        longTapBig.setRounding(1);
        longTapBig.setColor(R.color.colorRed);
        float fromBig = 0;
        longTapBig.setScale(fromBig);

        Animator[] animators = new Animator[]{
                ObjectAnimator.ofFloat(middle, "scale", CAMERA_MIDDLE_PART, 0f),
                ObjectAnimator.ofFloat(small, "scale", CAMERA_SMALL_PART, 0f),
                ObjectAnimator.ofFloat(big, "scale", 1f, 0f),
                ObjectAnimator.ofFloat(longTapSmall, "scale", fromSmall, STOP_SQUARE_PART),
                ObjectAnimator.ofFloat(longTapSmall, "rounding", 1, STOP_ROUNDING_PART),
                ObjectAnimator.ofFloat(longTapBig, "scale", fromBig, 1)};
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animators);
        set.setDuration(ANIMATION_DEFAULT_DURATION);

        set.addListener(new Listeners.EndAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                big.setScale(1f);
                changeMode(MODE_CAMERA);
                big.setVisibility(GONE);
                small.setVisibility(GONE);
                middle.setVisibility(GONE);
                longTapBig.setVisibility(VISIBLE);
                longTapSmall.setVisibility(VISIBLE);
                setClickable(true);
                if (callback != null){
                    callback.onAnimationEnd();
                }
            }
        });
        set.start();
    }

    /**
     * Animation plays when video starts recording
     */
    private void recordToStopAnimation(){
        setClickable(false);
        final float from = 0.1f;
        middle.setColor(R.color.colorRed);
        small.setColor(R.color.colorWhite);
        middle.setScale(from);
        small.setScale(from);
        middle.setVisibility(VISIBLE);

        Animator begin = ObjectAnimator.ofFloat(big, "begin", 0, 0.5f);
        Animator end = ObjectAnimator.ofFloat(big, "end", 1, 0.5f);
        AnimatorSet set = new AnimatorSet();
        set.play(begin).with(end);
        set.setDuration(ANIMATION_FASTEST);
        set.addListener(new Listeners.EndAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Animator middleAnim = ObjectAnimator.ofFloat(middle, "scale", from, 1f);
                Animator smallAnim = ObjectAnimator.ofFloat(small, "scale", from, STOP_SQUARE_PART);
                Animator smallAnimRounding = ObjectAnimator.ofFloat(small, "rounding", 1, STOP_ROUNDING_PART);
                Animator bigAnim = ObjectAnimator.ofFloat(big, "scale", 1f, 0f);
                AnimatorSet secondSet = new AnimatorSet();
                secondSet.play(smallAnim).with(middleAnim).with(smallAnimRounding).with(bigAnim);
                secondSet.setDuration(ANIMATION_FAST);
                secondSet.addListener(new ChangeModeOnEndAnimatorListener(MODE_STOP));
                secondSet.start();
            }
        });
        set.start();
    }

    /**
     * Animation plays when video stops recording
     * @param callback action which will be performed on the end of action
     */
    public void stopToRecordAnimation(final Animated.EndAnimationListener callback){
        setClickable(false);
        small.setVisibility(GONE);
        small.setRounding(1);
        big.setScale(0f);
        big.setBegin(0f);
        big.setEnd(1f);
        big.setVisibility(VISIBLE);
        Animator bigAnim = ObjectAnimator.ofFloat(big, "scale", 0, 1);
        bigAnim.setDuration(ANIMATION_DEFAULT_DURATION);
        bigAnim.addListener(new Listeners.EndAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Animator midAnim = ObjectAnimator.ofFloat(middle, "scale", 1, RECORD_CIRCLE_PART);
                midAnim.setDuration(ANIMATION_DEFAULT_DURATION );
                midAnim.addListener(new Listeners.EndAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setClickable(true);
                        changeMode(MODE_RECORD);
                        if (callback != null){
                            callback.onAnimationEnd();
                        }
                    }
                });
                midAnim.start();
            }
        });
        bigAnim.start();
    }

    /**
     * Animator listener which do nothing except of switching "clicability" of layout
     * and changing mode to new
     */
    private class ChangeModeOnEndAnimatorListener extends  Listeners.EndAnimatorListener{
        private int newMode;

        private ChangeModeOnEndAnimatorListener(int newMode){
            this.newMode = newMode;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            setClickable(true);
            changeMode(newMode);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int newWidth = (int)(height * RATIO);
        int newSpec = MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY);
        big.measure(newSpec, heightMeasureSpec);
        middle.measure(newSpec, heightMeasureSpec);
        small.measure(newSpec, heightMeasureSpec);
        longTapBig.measure(newSpec, heightMeasureSpec);
        longTapSmall.measure(newSpec, heightMeasureSpec);
        setMeasuredDimension(newSpec, heightMeasureSpec);
    }
}