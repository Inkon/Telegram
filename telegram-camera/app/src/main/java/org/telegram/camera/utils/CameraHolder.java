package org.telegram.camera.utils;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

/**
 * Holder of a camera and it's state. It takes pictures, records videos and can be released when isn't required
 *
 * @author Danil Kolikov
 * @author Gleb Zernov
 */
@SuppressWarnings("deprecation")
public class CameraHolder {
    /**
     * Flash turns on automatic
     */
    public static final int FLASH_AUTO = 0;
    /**
     * Flash is always on
     */
    public static final int FLASH_ON = 1;
    /**
     * Flash is always off
     */
    public static final int FLASH_OFF = 2;

    private static final int VIDEO_QUALITY = CamcorderProfile.QUALITY_HIGH;
    private static final String TAG = "CAMERA_HOLDER";

    private final List<Camera.Size> supportedPreviewSizes, supportedPictureSizes;
    /**
     * ID of camera
     *
     * @see CameraUtils#getCameraId(int)
     * @see CameraUtils#getCameraId(int)
     */
    public int id;

    private Camera camera;
    private MediaRecorder recorder;
    private RecordVideoCallback callback;
    private int orientationDegree;
    private File savedVideo;
    private boolean isShowingPreview;

    public CameraHolder(int id, Camera camera) {
        this.id = id;
        this.camera = camera;

        supportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
        supportedPictureSizes = camera.getParameters().getSupportedPictureSizes();
        isShowingPreview = false;
    }

    /**
     * Is this camera front
     *
     * @return True, if it's front, False otherwise
     */
    public boolean isFront() {
        return CameraUtils.getCameraInfo(id).facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    /**
     * Get list of available states of a flash
     *
     * @return List of states
     * @see CameraHolder#FLASH_AUTO
     * @see CameraHolder#FLASH_ON
     * @see CameraHolder#FLASH_ON
     */
    public List<Integer> getFlashStates() {
        List<Integer> result = new ArrayList<Integer>();
        List<String> modes = camera.getParameters().getSupportedFlashModes();
        if (modes == null) {
            return Collections.emptyList();
        }
        for (String s : modes) {
            switch (s) {
                case Camera.Parameters.FLASH_MODE_AUTO:
                    result.add(CameraHolder.FLASH_AUTO);
                    break;
                case Camera.Parameters.FLASH_MODE_OFF:
                    result.add(CameraHolder.FLASH_OFF);
                    break;
                case Camera.Parameters.FLASH_MODE_ON:
                    result.add(CameraHolder.FLASH_ON);
                    break;
                default:
                    break;
            }
        }
        Collections.sort(result);
        return result;
    }

    /**
     * Set flash mode to this camera
     *
     * @param flashMode new mode of a flash
     */
    public void setFlashMode(int flashMode) {
        String mode;
        switch (flashMode) {
            case FLASH_AUTO:
                mode = Camera.Parameters.FLASH_MODE_AUTO;
                break;
            case FLASH_ON:
                mode = Camera.Parameters.FLASH_MODE_ON;
                break;
            default:
                mode = Camera.Parameters.FLASH_MODE_OFF;
                break;
        }
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(mode);
        camera.setParameters(parameters);
    }

    /**
     * Start preview from this camera. Note that {@link CameraHolder#prepareForPhoto(SurfaceView)} or
     * {@link CameraHolder#prepareForVideo(SurfaceView)} should be called first
     */
    public void startPreview() {
        if (!isShowingPreview) {
            camera.startPreview();
            isShowingPreview = true;
        }
    }

    /**
     * Stop preview from this camera
     */
    public void stopPreview() {
        if (isShowingPreview) {
            camera.stopPreview();
            isShowingPreview = false;
        }
    }

    /**
     * Initialise preview
     *
     * @param width   Width of a preview
     * @param height  Height of a preview
     * @param preview Surface to show preview
     * @throws IOException If some errors with camera occured
     */
    private void setPreview(int width, int height, SurfaceView preview) throws IOException {
        Camera.Size pictureSize = CameraUtils.getOptimalPreviewSize(supportedPictureSizes, width, height);
        Camera.Size previewSize = CameraUtils.getOptimalPreviewSize(supportedPreviewSizes, width, height);

        Camera.Parameters parameters = camera.getParameters();
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
        parameters.setPreviewSize(previewSize.width, previewSize.height);

        // Resize preview
        double ratio = (double) previewSize.width / previewSize.height;
        ViewGroup.LayoutParams params = preview.getLayoutParams();
        int newHeight = (int) (ratio * preview.getMeasuredWidth());
        params.height = newHeight;
        params.width = preview.getMeasuredWidth();
        preview.setLayoutParams(params);

        camera.setParameters(parameters);
        if (!isShowingPreview) {
            camera.setPreviewDisplay(preview.getHolder());
        }
        startPreview();
    }

    /**
     * Update orientation of camera according to orientation of activity
     *
     * @param activity Current activity
     */
    public void updateCameraOrientation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        Log.d(TAG, "update orientation to " + rotation);
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        Camera.CameraInfo info = CameraUtils.getCameraInfo(id);
        int result;
        if (isFront()) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        orientationDegree = result;
        camera.setDisplayOrientation(result);
    }

    /**
     * Prepare specified view to capturing photo from camera
     *
     * @param view Surface to show preview
     * @return True, if view was successfully prepared, False otherwise
     */
    public boolean prepareForPhoto(SurfaceView view) {
        try {
            setPreview(((View) view.getParent()).getWidth(), ((View) view.getParent()).getHeight(), view);
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Take picture from camera
     *
     * @param listener Callback that will be called when picture will be ready
     */
    public void takePicture(final OnTakePictureListener listener) {
        final Camera.Size size = camera.getParameters().getPictureSize();
        final int orientation = orientationDegree;
        final boolean front = isFront();
        try {
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    if (listener != null) {
                        listener.onPictureTaken(data, size.width, size.height, orientation, front);
                    }
                }
            });
        } catch (RuntimeException e) {
            Log.e(TAG, "Can't take picture", e);
        }
    }

    /**
     * Prepare specified view to capturing video from camera
     *
     * @param view Surface to show preview
     * @return True, if view was successfully prepared, False otherwise
     */
    public boolean prepareForVideo(SurfaceView view) {
        CamcorderProfile profile = CamcorderProfile.get(id, VIDEO_QUALITY);
        try {
            setPreview(profile.videoFrameWidth, profile.videoFrameHeight, view);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * Prepare recorder for capturing video
     *
     * @return True, if recorder was successfully prepared, False otherwise
     */
    private boolean prepareRecorder() {
        camera.unlock();

        MediaRecorder recorder = new MediaRecorder();

        recorder.setCamera(camera);
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        recorder.setProfile(CamcorderProfile.get(id, VIDEO_QUALITY));
        recorder.setOrientationHint(CameraUtils.getCameraInfo(id).orientation);
        savedVideo = FileUtils.getOutputMediaFile(MEDIA_TYPE_VIDEO);
        recorder.setOutputFile(savedVideo.getPath());

        Log.d(TAG, "video saving path: " + savedVideo.getPath());
        this.recorder = recorder;
        try {
            recorder.prepare();
            return true;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            releaseRecorder();
            return false;
        }
    }

    /**
     * Start recording of video
     *
     * @param callback Callback that will be called when video will be ready
     */
    public void record(RecordVideoCallback callback) {
        if (prepareRecorder()) {
            recorder.start();
            this.callback = callback;
        }
    }

    /**
     * Set callback for recording a video
     *
     * @param callback New callback
     * @see RecordVideoCallback
     */
    public void setRecordVideoCallback(RecordVideoCallback callback) {
        this.callback = callback;
    }

    /**
     * Stop recording of video
     */
    public void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (RuntimeException e) {
                FileUtils.deleteFile(savedVideo);
                savedVideo = null;
            }
            releaseRecorder();
            if (callback != null) {
                callback.onVideoRecorded(savedVideo);
            }
        }
    }

    /**
     * Release camera. Note that camera must be released when you don't use it
     */
    public void releaseCamera() {
        camera.stopPreview();
        camera.release();
    }

    /**
     * Release recorder. Note that recorder must be released when you don't use it
     */
    public void releaseRecorder() {
        if (recorder != null) {
            recorder.reset();
            recorder.release();
            recorder = null;
            try {
                camera.reconnect();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            camera.lock();
        }
    }

    /**
     * Callback for taking pictures
     */
    public interface OnTakePictureListener {
        /**
         * Will be called when picture will be ready
         *
         * @param data        Bytes of a taken picture
         * @param width       Width of a picture
         * @param height      Height of a picture
         * @param orientation Orientation of a picture
         * @param front       Is picture was taken with a front camera
         */
        void onPictureTaken(byte[] data, int width, int height, int orientation, boolean front);
    }

    /**
     * Callback for taking videos
     */
    public interface RecordVideoCallback {
        /**
         * Will be called when video is recorder
         *
         * @param videoFile File with video
         */
        void onVideoRecorded(File videoFile);
    }

}
