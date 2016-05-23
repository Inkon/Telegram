package org.telegram.camera.components;

import android.animation.Animator;
import android.view.animation.Animation;

/**
 * Collections of abstract classes with listeners which do nothing except of on the end of animation
 * @author Gleb Zernov
**/
public class Listeners {

    /**
     * Listener of end animation work
     */

    public static abstract class EndAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    /**
     * Listener of end animator work
     */
    public static abstract class EndAnimatorListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}
