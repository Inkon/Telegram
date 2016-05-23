package org.telegram.camera.utils;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Listener of a long press
 *
 * @author Danil Kolikov
 */
public abstract class OnLongPressListener implements View.OnTouchListener, View.OnLongClickListener {
    private boolean pressed = false;

    /**
     * Will be called when user press and holds view
     */
    public abstract void onPressed();

    /**
     * Will be called when user releases view
     */
    public abstract void onReleased();

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (pressed) {
                    pressed = false;
                    Log.d("TAG", "RELEASED");
                    onReleased();
                    return true;
                }
        }
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        pressed = true;
        onPressed();
        return true;
    }
}
