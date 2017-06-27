package com.tomodian.RNGaplessSound;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

/**
 * Big thanks to:
 * https://stackoverflow.com/questions/26274182/not-able-to-achieve-gapless-audio-looping-so-far-on-android
 */

public class LoopMediaPlayer {

    private static final String TAG = LoopMediaPlayer.class.getSimpleName();

    private Context mContext = null;
    private int mResId   = 0;
    private int mCounter = 1;

    private MediaPlayer mCurrentPlayer = null;
    private MediaPlayer mNextPlayer    = null;

    private float currentVolume = 0;

    public static LoopMediaPlayer create(Context context, int resId) {
        return new LoopMediaPlayer(context, resId);
    }

    private LoopMediaPlayer(Context context, int resId) {
        mContext = context;
        mResId   = resId;

        mCurrentPlayer = MediaPlayer.create(mContext, mResId);
        mCurrentPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mCurrentPlayer.setVolume(currentVolume, currentVolume);
                mCurrentPlayer.start();
            }
        });
        createNextMediaPlayer();
    }

    private void createNextMediaPlayer() {
        mNextPlayer = MediaPlayer.create(mContext, mResId);

        mNextPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mNextPlayer.setVolume(currentVolume, currentVolume);
            }
        });

        mCurrentPlayer.setNextMediaPlayer(mNextPlayer);
        mCurrentPlayer.setOnCompletionListener(onCompletionListener);
    }

    private final MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.release();
            mCurrentPlayer = mNextPlayer;
            createNextMediaPlayer();
            Log.d(TAG, String.format("Loop #%d", ++mCounter));
        }
    };
    // code-read additions:
    public boolean isPlaying() throws IllegalStateException {
        return mCurrentPlayer.isPlaying();
    }

    public void setVolume(float leftVolume, float rightVolume) {
        currentVolume = leftVolume;
        mCurrentPlayer.setVolume(leftVolume, rightVolume);

        if(mNextPlayer != null) {
            mNextPlayer.setVolume(leftVolume, rightVolume);
        }
    }

    public void start() throws IllegalStateException {
        mCurrentPlayer.start();
    }

    public void stop() throws IllegalStateException {
        mCurrentPlayer.stop();
    }

    public void pause() throws IllegalStateException {
        mCurrentPlayer.pause();
    }

    public void release() {
        mCurrentPlayer.release();
        mNextPlayer.release();
    }

    public void reset() {
        mCurrentPlayer.reset();
    }
}