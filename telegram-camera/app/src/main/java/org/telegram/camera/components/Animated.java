package org.telegram.camera.components;

import android.support.annotation.Nullable;

/**
 * Interface for views that can be animated
 *
 * @author Danil Kolikov
 */
public interface Animated {
    /**
     * Listener of end of animation
     */
    interface EndAnimationListener {
        /**
         * Do this action when animation ends
         */
        void onAnimationEnd();
    }
    /**
     * Play animation of showing view
     */

    void show(@Nullable EndAnimationListener callback);

    /**
     * Play animation of hiding view
     */

    void hide(@Nullable EndAnimationListener callback);
}
