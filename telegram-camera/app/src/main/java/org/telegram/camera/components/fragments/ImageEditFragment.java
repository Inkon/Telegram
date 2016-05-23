package org.telegram.camera.components.fragments;

import android.app.Fragment;
import android.graphics.*;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.theartofdev.edmodo.cropper.CropImageView;
import org.telegram.camera.R;
import org.telegram.camera.components.SpinnerView;
import org.telegram.camera.components.buttons.CircleButton;
import org.telegram.camera.ui.MainActivity;
import org.telegram.camera.utils.FileUtils;

import static org.telegram.camera.components.fragments.PictureDoneFragment.BITMAP_FIELD;

/**
 * Fragment which appears when user click edit button on {@link PictureDoneFragment}
 * @author Gleb Zernov
 */

public class ImageEditFragment extends Fragment {
    private static final float SPINNER_MAX_DEGREE = 90;
    private Bitmap bitmap, smallBitmap;
    private static final String TAG = "IMAGE_EDIT";
    private CropImageView cropImageView;
    private ImageView imageView;
    private Button cancel, done, reset;
    private SpinnerView spinner;
    private TextView angle;
    private CircleButton rotate;
    private float currentAngle, baseAngle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup group = (ViewGroup)inflater.inflate(R.layout.fragment_image_edit, container, false);
        cancel = (Button)group.findViewById(R.id.cancel);
        done  = (Button)group.findViewById(R.id.done);
        reset =  (Button)group.findViewById(R.id.reset);
        cropImageView = (CropImageView)group.findViewById(R.id.crop_view);
        imageView = (ImageView)group.findViewById(R.id.edit_view);
        spinner = (SpinnerView)group.findViewById(R.id.spinner);
        angle = (TextView)group.findViewById(R.id.angle_label);
        rotate = (CircleButton) group.findViewById(R.id.rotate_button);
        setListeners();
        cropImageView.setScaleType(CropImageView.ScaleType.FIT_CENTER);
        cropImageView.setAutoZoomEnabled(true);
        currentAngle = 0;
        baseAngle = 0;
        return group;
    }

    /**
     * Set click listeners to buttons
     */
    private void setListeners() {
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).hideImageEditFragment(bitmap);
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bm = cropImageView.getCroppedImage();
                ((MainActivity)getActivity()).hideImageEditFragment(bm);
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImageView.setImageBitmap(bitmap);
                cropImageView.getCropRect();
                spinner.setValue(0);
                currentAngle = 0;
                baseAngle = 0;
                rotate(0);
            }
        });

        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseAngle += 90;
                if (baseAngle >= 360) {
                    baseAngle -= 360;
                }
                currentAngle = baseAngle + spinner.getValue() * SPINNER_MAX_DEGREE;
                setAngle(currentAngle);
                rotateBig(currentAngle);
            }
        });
        spinner.setListener(new SpinnerView.OnValueChangedListener() {
            @Override
            public void onValueChanged(float newValue) {
                rotate(-newValue * SPINNER_MAX_DEGREE);
            }
        });
        spinner.setTouchListener(new SpinnerView.OnSpinnerTouchedListener() {
            @Override
            public void onTouch() {
                int newHeight = cropImageView.getMeasuredHeight();
                int newWidth = newHeight * bitmap.getWidth() / bitmap.getHeight();
                // Make low-res image
                smallBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
                cropImageView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                rotate(currentAngle - baseAngle);
            }

            @Override
            public void onRelease() {
                spinner.setClickable(false);
                // Replace image with rotated hi-res
                rotateBig(currentAngle);
                cropImageView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                spinner.setClickable(true);
            }
        });
    }

    /**
     * Setting bitmap to a fragment from old one {@link PictureDoneFragment}
     * @see PictureDoneFragment#bitmap
     */
    @Override
    public void onResume() {
        super.onResume();
        if (getArguments() != null) {
            bitmap = getArguments().getParcelable(BITMAP_FIELD);
            cropImageView.setImageBitmap(bitmap);
        }
    }

    /**
     * Setting angle of image rotation
     * @param angle finite number of which image will be rotate
     * @see #rotate(float)
     */
    private void setAngle(float angle) {
        String text = String.format("%.1f°", angle);
        this.angle.setText(text);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Rotates image on current degree
     * @param degree amount of degrees of which image will be rotated
     * @see FileUtils#rotate(Bitmap, float)
     */
    private void rotate(float degree){
        currentAngle = baseAngle + degree;
        Bitmap rotatedBitmap = FileUtils.rotate(smallBitmap, currentAngle);
        setAngle(currentAngle);
        imageView.setImageBitmap(rotatedBitmap);
    }

    /**
     * Rotates hi-res bitmap
     * @param degree amount of degrees of which image will be rotate
     * @see #rotate(float)
     */
    private void rotateBig(float degree){
        Bitmap rotatedBitmap = FileUtils.rotate(bitmap, degree);
        cropImageView.setImageBitmap(rotatedBitmap);
    }

}
