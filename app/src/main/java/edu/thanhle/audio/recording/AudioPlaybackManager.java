package edu.thanhle.audio.recording;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;

public class AudioPlaybackManager implements OnPreparedListener, OnCompletionListener, OnErrorListener
{
    private MediaPlayer player;
    private final AudioPlaybackObserver observer;

    public AudioPlaybackManager(AudioPlaybackObserver observer)
    {
        this.observer = observer;
    }

    public void play(String filePath)
    {
        try
        {
            player = new MediaPlayer();
            player.setOnPreparedListener(this);
            player.setOnCompletionListener(this);
            player.setOnErrorListener(this);
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(filePath);
            player.prepareAsync();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            observer.onPreparedFailed();
        }
    }

    public void dispose()
    {
        if (player != null)
        {
            player.release();
        }
    }

    public void stop()
    {
        if ((player != null) && player.isPlaying())
        {
            player.stop();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer)
    {
        mediaPlayer.start();
        observer.onPrepared();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer)
    {
        observer.onCompleted();
    }

    public int currentPosition()
    {
        return (player != null) ? player.getCurrentPosition() : 0;
    }

    public int duration()
    {
        return (player != null) ? player.getDuration() : 0;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1)
    {
        return true;
    }

    public interface AudioPlaybackObserver
    {
        void onPrepared();

        void onPreparedFailed();

        void onCompleted();
    }
}