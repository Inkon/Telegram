package org.telegram.camera.components.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import org.telegram.camera.R;
import org.telegram.camera.components.Animated;
import org.telegram.camera.components.ButtonsBar;
import org.telegram.camera.ui.MainActivity;
import org.telegram.camera.utils.FileUtils;

/**
 * Fragment for showing captured or edited picture
 *
 * @author Danil Kolikov
 */
public class PictureDoneFragment extends Fragment implements Animated {
    public static final String BITMAP_FIELD = "bitmap";
    private ButtonsBar bar;
    private Button cancel, ok;
    private ImageView picture, edit;
    private Bitmap bitmap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup group = (ViewGroup) inflater.inflate(R.layout.fragment_picture_done, container, false);
        bar = (ButtonsBar) group.findViewById(R.id.choice_buttons);
        cancel = (Button) group.findViewById(R.id.button_cancel_saving_picture);
        ok = (Button) group.findViewById(R.id.button_save_picture);
        edit = (ImageView) group.findViewById(R.id.button_edit_picture);
        picture = (ImageView) group.findViewById(R.id.image_preview);
        setListeners();
        return group;
    }

    /**
     * Set new image to show
     * @param bitmap Bitmap of a new image
     */
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        picture.setImageBitmap(bitmap);
    }

    /**
     * Set click listeners to buttons
     */
    private void setListeners() {
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFragment();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FileUtils.SaveFileTask(getActivity(), bitmap).execute();
                closeFragment();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).showImageEditFragment(bitmap);
            }
        });
    }

    /**
     * Play animation and close fragment
     */
    private void closeFragment() {
        hide(new Animated.EndAnimationListener() {
            @Override
            public void onAnimationEnd() {
                getActivity().getFragmentManager().popBackStack();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bitmap == null && getArguments() != null) {
            bitmap = getArguments().getParcelable(BITMAP_FIELD);
        }
        if (bitmap != null) {
            picture.setImageBitmap(bitmap);
            show(null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hide(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BITMAP_FIELD, bitmap);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            bitmap = savedInstanceState.getParcelable(BITMAP_FIELD);
        }
    }

    @Override
    public void show(@Nullable EndAnimationListener callback) {
        bar.show(callback);
    }

    @Override
    public void hide(@Nullable EndAnimationListener callback) {
        bar.hide(callback);
    }
}
