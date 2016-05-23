package org.telegram.camera.components.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import org.telegram.camera.R;
import org.telegram.camera.components.Animated;
import org.telegram.camera.components.AnimatedFrameLayout;
import org.telegram.camera.components.CirclesBar;
import org.telegram.camera.components.TimeLabel;
import org.telegram.camera.components.buttons.FlashButton;
import org.telegram.camera.components.buttons.SuperButton;
import org.telegram.camera.components.buttons.SwitchCameraButton;
import org.telegram.camera.ui.MainActivity;
import org.telegram.camera.utils.CameraHolder;
import org.telegram.camera.utils.CameraUtils;
import org.telegram.camera.utils.OnLongPressListener;
import org.telegram.camera.utils.OnSwipeTouchListener;

import java.io.File;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Fragment that shows preview from camera and takes pictures and videos
 *
 * @author Danil Kolikov
 */
public class CameraFragment extends Fragment implements Animated {
    private static final String FRONT_CAMERA = "frontCamera";

    private static final int MODE_CAMERA = SuperButton.MODE_CAMERA;
    private static final int MODE_VIDEO = SuperButton.MODE_RECORD;
    private static final int MODE_VIDEO_RECORD = SuperButton.MODE_STOP;

    private SwitchCameraButton switchCamera;
    private FlashButton flashButton;
    private TimeLabel timeLabel;
    private AnimatedFrameLayout background;
    private SurfaceView cameraView;
    private CirclesBar circlesBar;
    private SuperButton superButton;

    private CameraHolder cameraHolder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup group = (ViewGroup) inflater.inflate(R.layout.fragment_camera, container);

        cameraView = (SurfaceView) group.findViewById(R.id.camera_preview);
        background = (AnimatedFrameLayout) group.findViewById(R.id.camera_buttons_background);

        switchCamera = (SwitchCameraButton) group.findViewById(R.id.button_switch_camera);
        flashButton = (FlashButton) group.findViewById(R.id.flash_button);
        superButton = (SuperButton) group.findViewById(R.id.super_button);

        timeLabel = (TimeLabel) group.findViewById(R.id.time_label);
        circlesBar = (CirclesBar) group.findViewById(R.id.circles_bar);
        cameraView.setZOrderOnTop(false);
        setListeners();
        return group;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            switchCamera.setFront(savedInstanceState.getBoolean(FRONT_CAMERA));
        } else {
            if (CameraUtils.hasBackCamera()) {
                switchCamera.setFront(false);
            } else {
                switchCamera.setFront(true);
            }
        }
    }

    @Override
    public void show(@Nullable EndAnimationListener callback) {
        if (cameraHolder == null) {
            if (!CameraUtils.hasCamera()) {
                MainActivity.showNoCameraMessage(getActivity());
                return;
            }
            loadCamera(switchCamera.isFront());
        } else {
            checkFeaturesAvailability();
        }
        setLayoutAccordingToMode();
        superButton.show(callback);
        if (cameraHolder != null) {
            showPreview();
        }
    }

    /**
     * Display buttons for switch back/front camera and flash if they're needed
     */
    private void checkFeaturesAvailability() {
        if (cameraHolder == null) {
            return;
        }
        List<Integer> flashStates = cameraHolder.getFlashStates();
        if (!flashStates.isEmpty()) {
            flashButton.setStates(flashStates);
            if (flashButton.getVisibility() == GONE) {
                flashButton.show(null);
            }
        }
        if (CameraUtils.hasTwoCameras()) {
            if (switchCamera.getVisibility() == GONE) {
                switchCamera.show(null);
            }
        }
    }

    /**
     * Change background according to current mode
     */
    private void setLayoutAccordingToMode() {
        switch (superButton.mode) {
            case MODE_CAMERA:
                background.setBackgroundAlpha(1);
                break;
            case MODE_VIDEO:
                background.setBackgroundAlpha(background.finalTransparency);
                break;
        }
    }

    /**
     * Show preview from camera
     */
    private void showPreview() {
        cameraView.setVisibility(VISIBLE);
        switch (superButton.mode) {
            case MODE_CAMERA:
                cameraHolder.prepareForPhoto(cameraView);
                break;
            case MODE_VIDEO:
                cameraHolder.prepareForVideo(cameraView);
                break;
        }
    }

    /**
     * Update toolbars according to mode. E.G. if we record video, then hide flash and show timer
     *
     * @param newMode New mode
     */
    private void changeVideoRecordingMode(int newMode) {
        if (newMode == MODE_VIDEO_RECORD) {
            background.fall(null);
            if (flashButton.getVisibility() == VISIBLE) {
                flashButton.hide(null);
            }
            timeLabel.start();
            timeLabel.show(null);
            circlesBar.hide(null);
        } else {
            background.rise(null);
            timeLabel.stop();
            timeLabel.hide(null);
            circlesBar.show(null);
            setLayoutAccordingToMode();
        }
    }

    /**
     * Change mode of fragment
     *
     * @param newMode New mode
     */
    private void changeMode(int newMode) {
        superButton.startAnimation(newMode);
        switch (newMode) {
            case MODE_CAMERA:
                background.makeClear(null);
                circlesBar.leftAnimation();
                break;
            case MODE_VIDEO:
                background.makeTransparent(null);
                circlesBar.rightAnimation();
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FRONT_CAMERA, switchCamera.isFront());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (superButton.mode == MODE_VIDEO_RECORD) {
            cameraHolder.setRecordVideoCallback(null);
            cameraHolder.stopRecording();
            superButton.changeMode(MODE_VIDEO);
        }
        if (cameraHolder != null) {
            cameraHolder.releaseCamera();
            cameraHolder = null;
        }
    }

    @Override
    public void hide(@Nullable EndAnimationListener callback) {
        if (switchCamera.getVisibility() == VISIBLE) {
            switchCamera.hide(null);
        }
        if (flashButton.getVisibility() == VISIBLE) {
            flashButton.hide(null);
        }
        cameraView.setVisibility(GONE);
        superButton.hide(callback);
        if (cameraHolder != null) {
            cameraHolder.stopPreview();
        }
    }

    /**
     * Set listeners to buttons
     */
    private void setListeners() {
        superButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!superButton.isClickable()){
                    return;
                }
                superButton.startAnimation(superButton.mode);
                Log.d("Camera", "" + superButton.mode);
                switch (superButton.mode) {
                    case SuperButton.MODE_CAMERA:
                        if (cameraHolder != null) {
                            cameraHolder.takePicture((MainActivity) getActivity());
                        }
                        break;
                    case SuperButton.MODE_RECORD:
                        if (cameraHolder == null) {
                            return;
                        }
                        cameraHolder.record(new CameraHolder.RecordVideoCallback() {
                            @Override
                            public void onVideoRecorded(final File videoFile) {
                                superButton.stopToRecordAnimation(new EndAnimationListener() {
                                    @Override
                                    public void onAnimationEnd() {
                                        ((MainActivity) getActivity()).onVideoRecorded(videoFile);
                                    }
                                });
                            }
                        });
                        changeVideoRecordingMode(MODE_VIDEO_RECORD);
                        break;
                    case SuperButton.MODE_STOP:
                        cameraHolder.stopRecording();
                        changeVideoRecordingMode(MODE_VIDEO);
                        break;
                }
            }
        });

        OnLongPressListener longPressListener = new OnLongPressListener() {
            @Override
            public void onPressed() {
                if (cameraHolder == null) return;
                if (cameraHolder.prepareForVideo(cameraView)) {
                    changeVideoRecordingMode(MODE_VIDEO_RECORD);
                    superButton.longTapAnimation(new EndAnimationListener() {
                        @Override
                        public void onAnimationEnd() {
                            cameraHolder.record(new CameraHolder.RecordVideoCallback() {
                                @Override
                                public void onVideoRecorded(final File videoFile) {
                                    ((MainActivity) getActivity()).onVideoRecorded(videoFile);
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onReleased() {
                if (cameraHolder == null) return;
                cameraHolder.stopRecording();
                changeVideoRecordingMode(MODE_CAMERA);
                superButton.changeMode(SuperButton.MODE_CAMERA);
            }
        };

        superButton.setOnTouchListener(longPressListener);
        superButton.setOnLongClickListener(longPressListener);

        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera.rotateIcon(null);
                if (cameraHolder != null) {
                    if (superButton.mode == MODE_VIDEO) {
                        cameraHolder.releaseRecorder();
                    }
                    cameraHolder.releaseCamera();
                    loadCamera(switchCamera.isFront());
                }
            }
        });

        flashButton.setListener(new FlashButton.StateChangedListener() {
            @Override
            public void onFlashStateChanged(FlashButton v, int newState) {
                if (cameraHolder != null) {
                    cameraHolder.setFlashMode(newState);
                }
            }
        });

        cameraView.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
            @Override
            public void onSwipeRight() {
                if (cameraHolder != null && superButton.mode == MODE_VIDEO) {
                    cameraHolder.releaseRecorder();
                    changeMode(MODE_CAMERA);
                    if (!cameraHolder.prepareForPhoto(cameraView)) {
                        changeMode(MODE_VIDEO);
                    }
                }
            }

            @Override
            public void onSwipeLeft() {
                if (cameraHolder != null && superButton.mode == MODE_CAMERA) {
                    changeMode(MODE_VIDEO);
                    if (!cameraHolder.prepareForVideo(cameraView)) {
                        changeMode(MODE_CAMERA);
                    }
                }
            }
        });
    }

    /**
     * Acquire camera
     *
     * @param facing True, is front camera is required, false, otherwise
     * @see org.telegram.camera.utils.CameraUtils.LoadCameraTask
     */
    private void loadCamera(boolean facing) {
        cameraHolder = null;
        new CameraUtils.LoadCameraTask(new CameraUtils.CameraLoadedListener() {
            @Override
            public void onCameraLoaded(CameraHolder cameraHolder) {
                if (cameraHolder == null) {
                    MainActivity.showNoCameraMessage(getActivity());
                    return;
                }
                if (getActivity() != null) {
                    CameraFragment.this.cameraHolder = cameraHolder;
                    checkFeaturesAvailability();
                    cameraHolder.updateCameraOrientation(getActivity());
                    showPreview();
                }
            }
        }).execute(facing);
    }

}
