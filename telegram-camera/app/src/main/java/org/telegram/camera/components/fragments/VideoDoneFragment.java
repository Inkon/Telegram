package org.telegram.camera.components.fragments;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.VideoView;
import org.telegram.camera.R;
import org.telegram.camera.components.Animated;
import org.telegram.camera.components.ButtonsBar;
import org.telegram.camera.utils.FileUtils;

/**
 * Fragment for showing captured video
 *
 * @author Danil Kolikov
 */
public class VideoDoneFragment extends Fragment implements Animated {
    public static final String VIDEO_FIELD = "video";
    private static final int PLAY_RESOURCE = R.drawable.video_play;
    private static final int PAUSE_RESOURCE = R.drawable.video_pause;
    private static final int FIRST_FRAME = 100;

    private ButtonsBar bar;
    private Button cancel, ok;
    private ImageView play;
    private VideoView videoView;
    private SeekBar progress;
    private boolean paused;
    private ObjectAnimator playbackAnimation;
    private Uri videoUri;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup group = (ViewGroup) inflater.inflate(R.layout.fragment_video_done, container, false);
        bar = (ButtonsBar) group.findViewById(R.id.choice_buttons);
        cancel = (Button) group.findViewById(R.id.button_cancel_saving_video);
        ok = (Button) group.findViewById(R.id.button_save_video);
        play = (ImageView) group.findViewById(R.id.button_play);
        videoView = (VideoView) group.findViewById(R.id.video_preview);
        progress = (SeekBar) group.findViewById(R.id.playback_progress);

        progress.setProgressDrawable(getResources().getDrawable(R.drawable.playback_progress));
        play.setImageResource(PLAY_RESOURCE);
        paused = true;
        setListeners();
        return group;
    }

    /**
     * Set click listeners to buttons
     */
    private void setListeners() {
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileUtils.deleteUri(videoUri);
                closeFragment();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileUtils.addFileToGallery(videoUri, getActivity());
                closeFragment();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paused) {
                    playVideo();
                } else {
                    pauseVideo();
                }
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d("TAG", "end reached");
                pauseVideo();
                if (mp.isPlaying()) {
                    mp.pause();
                }
                if (FIRST_FRAME < videoView.getDuration()) {
                    videoView.seekTo(FIRST_FRAME);
                    videoView.start();
                    videoView.pause();
                }
                progress.setProgress(0);
            }
        });

        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean wasPaused;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                wasPaused = paused;
                pauseVideo();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!wasPaused) {
                    playVideo();
                }
            }
        });
    }

    private void pauseVideo() {
        play.setImageResource(PLAY_RESOURCE);
        videoView.pause();
        if (playbackAnimation != null) {
            playbackAnimation.end();
        }
        progress.setProgress(videoView.getCurrentPosition());
        paused = true;
    }

    private void playVideo() {
        play.setImageResource(PAUSE_RESOURCE);
        progress.setMax(videoView.getDuration());

        if (videoView.getCurrentPosition() > videoView.getDuration()) {
            videoView.seekTo(0);
        }

        // It's not possible to get current time of a video playback
        // So I play animation of playback here
        // In a hope in that real video will play on the same speed
        // Luckily it is so, because we don't load video from the Web
        playbackAnimation = ObjectAnimator.ofInt(progress, "progress", videoView.getCurrentPosition(),
                videoView.getDuration());
        playbackAnimation.setDuration(videoView.getDuration() - videoView.getCurrentPosition());
        playbackAnimation.setInterpolator(new LinearInterpolator());

        videoView.start();
        playbackAnimation.start();
        paused = false;
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
        if (getArguments() != null) {
            videoUri = getArguments().getParcelable(VIDEO_FIELD);
        }
        if (videoUri != null) {
            play.setVisibility(View.GONE);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (FIRST_FRAME < videoView.getDuration()) {
                        videoView.seekTo(FIRST_FRAME);
                        videoView.start();
                        videoView.pause();
                    }
                    play.setVisibility(View.VISIBLE);
                }
            });
            videoView.setVideoURI(videoUri);
            progress.setProgress(0);
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
        outState.putParcelable(VIDEO_FIELD, videoUri);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            videoUri = savedInstanceState.getParcelable(VIDEO_FIELD);
        }
    }

    @Override
    public void show(@Nullable EndAnimationListener callback) {
        videoView.setVisibility(View.VISIBLE);
        bar.show(callback);
    }

    @Override
    public void hide(@Nullable EndAnimationListener callback) {
        if (videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        videoView.setVisibility(View.GONE);
        bar.hide(callback);
    }
}
