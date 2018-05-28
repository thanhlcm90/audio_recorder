package edu.thanhle.audio.recording;

import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;
import android.media.MediaRecorder.AudioSource;
import android.media.MediaRecorder.OutputFormat;

import java.io.IOException;

public class AudioRecordingManager
{
    private final MediaRecorder recorder;
    private boolean isRecording = false;

    public static final int AUDIO_BIT_RATE = 1 * 1024 * 1024; // in bits/s
    public static final int AUDIO_SAMPLING_RATE = 44100; // in Hz

    public AudioRecordingManager()
    {
        this.recorder = new MediaRecorder();
    }

    public void record(String filePath) throws IOException
    {
        recorder.setAudioSource(AudioSource.VOICE_RECOGNITION);
        recorder.setOutputFormat(OutputFormat.MPEG_4);
        recorder.setAudioEncoder(AudioEncoder.AAC);
        recorder.setAudioEncodingBitRate(AUDIO_BIT_RATE);
        recorder.setAudioSamplingRate(AUDIO_SAMPLING_RATE);

        recorder.setOutputFile(filePath);
        recorder.prepare();
        recorder.start();

        isRecording = true;
    }

    public void stop()
    {
        if (isRecording)
        {
            isRecording = false;

            try
            {
                recorder.stop();
                recorder.reset();
            }
            catch (Exception e)
            {
                // ignore
            }
        }
    }

    public void dispose()
    {
        recorder.release();
    }
}