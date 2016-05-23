package org.telegram.camera.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import org.telegram.camera.components.Animated;

/**
 * Some functions for work with components
 *
 * @author Danil Kolikov
 */
public class ComponentUtils {
    /**
     * Count margin for {@link org.telegram.camera.components.FrameLayoutWithMargin FrameLayoutWithMargin}
     *
     * @param spec       Current measure spec
     * @param marginPart Which part of specified size should take margin
     * @return Value of margin
     */
    public static int countMargin(int spec, float marginPart) {
        return (int) (View.MeasureSpec.getSize(spec) / (1 + 2 * marginPart) * marginPart);
    }

    /**
     * Make Translation animation
     *
     * @param start          Start Y value of translation
     * @param end            End Y value
     * @param relativeToSelf True, if start and end specified relative to view, False - if relative to parent
     * @param duration       Duration of animation
     * @param callback       Callback
     * @return New Animation
     * @see org.telegram.camera.components.Animated.EndAnimationListener
     */
    public static Animation makeTranslationAnimation(float start, final float end, boolean relativeToSelf, long duration,
                                                     final Animated.EndAnimationListener callback) {
        Animation animation = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, 0,
                relativeToSelf ? TranslateAnimation.RELATIVE_TO_SELF : TranslateAnimation.RELATIVE_TO_PARENT, start,
                relativeToSelf ? TranslateAnimation.RELATIVE_TO_SELF : TranslateAnimation.RELATIVE_TO_PARENT, end);

        animation.setDuration(duration);
        animation.setAnimationListener(new Animation.AnimationListener() {
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
        return animation;
    }
}
