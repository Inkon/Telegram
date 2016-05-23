package org.telegram.camera.utils;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Collections of functions for work with camera
 *
 * @author Danil Kolikov
 * @author Gleb Zernov
 */
@SuppressWarnings("deprecation")
public class CameraUtils {
    /**
     * Get instance of camera and fill id
     *
     * @param facing Facing of camera. See constants in {@link android.hardware.Camera.CameraInfo}
     * @return Id of camera or -1, if there is no such camera
     */
    public static int getCameraId(int facing) {
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.CameraInfo info = getCameraInfo(i);
            if (info.facing == facing) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Try to get camera by id
     *
     * @param cameraId ID of camera
     * @return Camera, or null, if can't open it
     */
    public static Camera getCameraById(int cameraId) {
        try {
            return Camera.open(cameraId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get info about camera
     *
     * @param cameraId ID of camera
     * @return Information about camera
     */
    public static Camera.CameraInfo getCameraInfo(int cameraId) {
        Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        return info;
    }

    /**
     * Check if phone has front camera
     *
     * @return True, if has, False, otherwise
     */
    public static boolean hasFrontCamera() {
        return getCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT) != -1;
    }


    /**
     * Check if phone has back camera
     *
     * @return True, if has, False, otherwise
     */
    public static boolean hasBackCamera() {
        return getCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT) != -1;
    }


    /**
     * Check if phone has any camera
     *
     * @return True, if has, False, otherwise
     */
    public static boolean hasCamera() {
        return hasBackCamera() || hasFrontCamera();
    }


    /**
     * Check if phone has both cameras
     *
     * @return True, if has, False, otherwise
     */
    public static boolean hasTwoCameras() {
        return hasFrontCamera() && hasBackCamera();
    }

    /**
     * Select optimal size for preview
     *
     * @param sizes Possible sizes of previews
     * @param w Desired width
     * @param h Desired height
     * @return Found size, that is close to specified
     */
    public static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        if (h > w) {
            int t = h;
            h = w;
            w = t;
        }
        double targetRatio = (double) h / w;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }

        return optimalSize;
    }

    /**
     * Interface for LoadCameraTask
     */
    public interface CameraLoadedListener {
        /**
         * Called when camera was loaded
         *
         * @param cameraHolder New CameraHolder, or null, if camera was blocked
         */
        void onCameraLoaded(CameraHolder cameraHolder);
    }

    /**
     * AsyncTask for loading camera. Pass in execute facing of camera (True - front, False - otherwise)
     */
    @SuppressWarnings("deprecation")
    public static class LoadCameraTask extends AsyncTask<Boolean, Object, CameraHolder> {
        private CameraLoadedListener listener;

        public LoadCameraTask(CameraLoadedListener listener) {
            this.listener = listener;
        }

        @Override
        protected void onPostExecute(@Nullable CameraHolder cameraHolder) {
            if (listener != null) {
                listener.onCameraLoaded(cameraHolder);
            }
        }

        @Override
        protected CameraHolder doInBackground(Boolean... params) {
            boolean front = params[0];
            int cameraId = CameraUtils.getCameraId(
                    front ? Camera.CameraInfo.CAMERA_FACING_FRONT :
                            Camera.CameraInfo.CAMERA_FACING_BACK);
            Camera camera = CameraUtils.getCameraById(cameraId);
            if (camera == null) {
                return null;
            }
            return new CameraHolder(cameraId, camera);
        }
    }
}
