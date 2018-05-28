package edu.thanhle.audio.recording;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.util.Date;

import edu.thanhle.audio.recording.AudioPlaybackManager.AudioPlaybackObserver;

public class AudioRecord implements AudioPlaybackObserver
{
    private static final int MAX_RECORD_TIME = 30; // in seconds
    private static final String TYPE_AUDIO = "aac";

    private final File file;
    private final Context context;
    private final AudioRecordObserver observer;
    private final AudioRecordingManager recordingManager;
    private final AudioPlaybackManager playbackManager;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private AudioRecordingState state = AudioRecordingState.INITIALIZED;
    private long startTime = 0L;

    public enum AudioRecordingState
    {
        INITIALIZED, RECORDING, PLAYING, DONE
    }

    public AudioRecord(Context context, AudioRecordObserver observer)
    {
        this.context = context;
        this.observer = observer;
        this.file = createFile();
        this.recordingManager = new AudioRecordingManager();
        this.playbackManager = new AudioPlaybackManager(this);
    }

    private File createFile()
    {
        try
        {
            String tempFileName = String.valueOf(new Date().getTime()) + "." + TYPE_AUDIO;
            File file = new File(context.getCacheDir(), tempFileName);
            Log.d("FILE RECORD", file.getPath());

            if (!file.exists())
            {
                file.createNewFile();
            }

            return file;
        }
        catch (Exception e)
        {
            observer.onRecordError();

            return null;
        }
    }

    public AudioRecordingState state()
    {
        return state;
    }

    public String filePath()
    {
        return file.getAbsolutePath();
    }

    public void release()
    {
        interrupt();

        if (recordingManager != null)
        {
            recordingManager.dispose();
        }

        if (playbackManager != null)
        {
            playbackManager.dispose();
        }
    }

    public void initialize()
    {
        state = AudioRecordingState.INITIALIZED;

        updateUI();
    }

    public boolean closeRequested()
    {
        interrupt();

        return (state != AudioRecordingState.INITIALIZED);
    }

    public void onBackground()
    {
        interrupt();
        handler.post(this::updateUI);
    }

    private void interrupt()
    {
        if (state == AudioRecordingState.RECORDING)
        {
            done();
        }
        else if (state == AudioRecordingState.PLAYING)
        {
            stop();
        }
    }

    private void updateUI()
    {
        observer.onUpdate(state);
    }

    public void record()
    {
        try
        {
            recordingManager.record(file.getAbsolutePath());
            startTime = SystemClock.uptimeMillis();
            handler.postDelayed(recordTimeUpdate, 1);

            state = AudioRecordingState.RECORDING;

            updateUI();
        }
        catch (Exception e)
        {
            observer.onRecordError();
        }
    }

    public void done()
    {
        handler.removeCallbacks(recordTimeUpdate);
        recordingManager.stop();

        state = AudioRecordingState.DONE;

        updateUI();
    }

    public void play()
    {
        playbackManager.play(file.getAbsolutePath());

        state = AudioRecordingState.PLAYING;

        updateUI();
    }

    public void stop()
    {
        handler.removeCallbacks(playbackTimeUpdate);
        playbackManager.stop();

        state = AudioRecordingState.DONE;

        updateUI();
    }

    @Override
    public void onPrepared()
    {
        handler.postDelayed(playbackTimeUpdate, 1);
    }

    @Override
    public void onPreparedFailed()
    {
        observer.onRecordError();
    }

    @Override
    public void onCompleted()
    {
        stop();
    }

    private final Runnable recordTimeUpdate = new Runnable()
    {
        public void run()
        {
            long timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            double updatedTime = (double) timeInMilliseconds / 1000;

            int progress = (int) (updatedTime / MAX_RECORD_TIME * 100);
            observer.onProgress(progress);

            if (updatedTime < MAX_RECORD_TIME)
            {
                handler.postDelayed(this, 1);
            }
            else
            {
                done();
            }
        }
    };

    private final Runnable playbackTimeUpdate = new Runnable()
    {
        public void run()
        {
            int current = playbackManager.currentPosition();
            int total = playbackManager.duration();

            if (total > 0)
            {
                int progress = (int) (current / (float) total * 100);
                observer.onProgress(progress);

                if (current < total)
                {
                    handler.postDelayed(this, 1);
                }
                else
                {
                    stop();
                }
            }
        }
    };

    public interface AudioRecordObserver
    {
        void onUpdate(AudioRecordingState state);

        void onProgress(int progress);

        void onRecordError();
    }
}