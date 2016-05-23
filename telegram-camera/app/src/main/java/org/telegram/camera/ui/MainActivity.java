package org.telegram.camera.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import org.telegram.camera.R;
import org.telegram.camera.components.Animated;
import org.telegram.camera.components.buttons.SuperButton;
import org.telegram.camera.components.fragments.CameraFragment;
import org.telegram.camera.components.fragments.ImageEditFragment;
import org.telegram.camera.components.fragments.PictureDoneFragment;
import org.telegram.camera.components.fragments.VideoDoneFragment;
import org.telegram.camera.utils.CameraHolder;
import org.telegram.camera.utils.FileUtils;

import java.io.File;

/**
 * Main activity of the TelegramCamera
 */
public class MainActivity extends AppCompatActivity implements CameraHolder.OnTakePictureListener,
        FragmentManager.OnBackStackChangedListener, CameraHolder.RecordVideoCallback {
    private static final String TAG = "MainCameraActivity";
    private static final String FRAGMENT_CAMERA_TAG = "camera";
    private static final String FRAGMENT_PICTURE_DONE_TAG = "picture_done";
    private static final String FRAGMENT_VIDEO_DONE_TAG = "camera_done";
    private static final String FRAGMENT_IMAGE_EDIT_TAG = "image_edit";
    private static final int PERMISSION_CHECK = 0;

    private SuperButton superButton;
    private boolean permissionsGranted;

    /**
     * Show message if there is no available camera
     */
    public static void showNoCameraMessage(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.no_camera_title)
                .setMessage(R.string.no_camera_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        activity.finish();
                    }
                })
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.default_layout);
        superButton = (SuperButton) findViewById(R.id.super_button);
        getFragmentManager().addOnBackStackChangedListener(this);
        if (!FileUtils.isExternalStorageMounted()) {
            showNoFreeSpaceErrorMessage();
        }
        grantPermissions();
    }

    /**
     * Check permissions in runtime. Special fix for Android Marshmallow+ devices
     */
    private void grantPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsGranted = false;
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, PERMISSION_CHECK);
        } else {
            permissionsGranted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CHECK:
                if (grantResults.length == 4
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED
                        ) {
                    showCameraFragment();
                } else {
                    showNoCameraMessage(this);
                }
        }
    }

    @Override
    public void onBackPressed() {
        final FragmentManager manager = getFragmentManager();
        if (manager.getBackStackEntryCount() == 0) {
            super.onBackPressed();
            return;
        }

        // I can't use getFragmentManager.popBackStack, because it will skip cool animations
        FragmentManager.BackStackEntry entry = manager.getBackStackEntryAt(
                manager.getBackStackEntryCount() - 1);
        String tag = entry.getName();
        Fragment last = manager.findFragmentByTag(tag);
        if (last instanceof Animated) {
            ((Animated) last).hide(new Animated.EndAnimationListener() {
                @Override
                public void onAnimationEnd() {
                    manager.popBackStack();
                }
            });
        } else {
            manager.popBackStack();
        }
    }

    @Override
    public void onBackStackChanged() {
        // When backStack changed, play show animation
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            showCameraFragment();
        } else {
            FragmentManager manager = getFragmentManager();
            FragmentManager.BackStackEntry entry = manager.getBackStackEntryAt(
                    manager.getBackStackEntryCount() - 1);
            String tag = entry.getName();
            Fragment last = manager.findFragmentByTag(tag);
            if (last instanceof Animated) {
                ((Animated) last).show(null);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!permissionsGranted) {
            return;
        }
        // If there are no other fragments, load camera
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            showCameraFragment();
        }
    }

    /**
     * Load camera fragment. It will load camera and prepare it for taking photos
     */
    private void showCameraFragment() {
        Log.e(TAG, "showCameraFragment: shown");
        CameraFragment cameraFragment = (CameraFragment) getFragmentManager().findFragmentByTag(FRAGMENT_CAMERA_TAG);
        cameraFragment.show(null);
    }

    /**
     * Show fragment for choosing save or decline picture
     *
     * @param bitmap A bitmap of a picture
     */
    private void showPictureDoneFragment(final Bitmap bitmap) {
        CameraFragment cameraFragment = (CameraFragment) getFragmentManager().findFragmentByTag(FRAGMENT_CAMERA_TAG);
        cameraFragment.hide(new Animated.EndAnimationListener() {
            @Override
            public void onAnimationEnd() {
                Bundle bundle = new Bundle();
                bundle.putParcelable(PictureDoneFragment.BITMAP_FIELD, bitmap);

                Fragment fragment = new PictureDoneFragment();
                fragment.setArguments(bundle);

                getFragmentManager().beginTransaction()
                        .add(android.R.id.content, fragment, FRAGMENT_PICTURE_DONE_TAG)
                        .addToBackStack(FRAGMENT_PICTURE_DONE_TAG)
                        .commit();
            }
        });
    }

    /**
     * Show fragment with choosing what to do with a video
     *
     * @param video Uri of a video file
     */
    private void showVideoDoneFragment(final Uri video) {
        CameraFragment cameraFragment = (CameraFragment) getFragmentManager().findFragmentByTag(FRAGMENT_CAMERA_TAG);
        cameraFragment.hide(new Animated.EndAnimationListener() {
            @Override
            public void onAnimationEnd() {
                Bundle bundle = new Bundle();
                bundle.putParcelable(VideoDoneFragment.VIDEO_FIELD, video);

                Fragment fragment = new VideoDoneFragment();
                fragment.setArguments(bundle);

                getFragmentManager().beginTransaction()
                        .add(android.R.id.content, fragment, FRAGMENT_VIDEO_DONE_TAG)
                        .addToBackStack(FRAGMENT_VIDEO_DONE_TAG)
                        .commit();
            }
        });
    }

    /**
     * Show a fragment for editing an image
     *
     * @param bitmap A bitmap of an image
     */
    public void showImageEditFragment(final Bitmap bitmap) {
        PictureDoneFragment pictureDoneFragment = (PictureDoneFragment) getFragmentManager().
                findFragmentByTag(FRAGMENT_PICTURE_DONE_TAG);
        pictureDoneFragment.hide(new Animated.EndAnimationListener() {
            @Override
            public void onAnimationEnd() {
                Bundle bundle = new Bundle();
                bundle.putParcelable(PictureDoneFragment.BITMAP_FIELD, bitmap);

                Fragment fragment = new ImageEditFragment();
                fragment.setArguments(bundle);

                getFragmentManager().beginTransaction()
                        .add(android.R.id.content, fragment, FRAGMENT_IMAGE_EDIT_TAG)
                        .addToBackStack(FRAGMENT_IMAGE_EDIT_TAG)
                        .commit();
            }
        });
    }

    /**
     * Hide image edit fragment. It's a invariant that below it on a backstack will be PictureEditFragment,
     * so it's possible to set new image to it here
     *
     * @param bitmap Edited image
     */
    public void hideImageEditFragment(final Bitmap bitmap) {
        Log.e(TAG, "finished");
        Bundle bundle = new Bundle();
        bundle.putParcelable(PictureDoneFragment.BITMAP_FIELD, bitmap);

        PictureDoneFragment pictureDone = (PictureDoneFragment) getFragmentManager()
                .findFragmentByTag(FRAGMENT_PICTURE_DONE_TAG);
        pictureDone.setBitmap(bitmap);
        getFragmentManager().popBackStack();
    }

    /**
     * Show message that there is no space to save data
     */
    private void showNoFreeSpaceErrorMessage() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.no_free_space)
                .setMessage(R.string.no_free_space_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                })
                .show();
    }

    @Override
    public void onPictureTaken(byte[] data, int width, int height, int orientation, boolean front) {
        Bitmap picture = FileUtils.getPicture(data, width, height, orientation, front);
        superButton.setClickable(true);
        showPictureDoneFragment(picture);
        Log.d(TAG, "Image taken");
    }

    @Override
    public void onVideoRecorded(File videoFile) {
        if (videoFile == null) return;
        Uri videoURI = Uri.fromFile(videoFile);
        showVideoDoneFragment(videoURI);
        Log.d(TAG, "Video taken");
    }
}
