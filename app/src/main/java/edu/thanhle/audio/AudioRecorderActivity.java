package edu.thanhle.audio;

import android.Manifest.permission;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import edu.thanhle.audio.recording.AudioRecord;
import edu.thanhle.audio.recording.AudioRecord.AudioRecordObserver;
import edu.thanhle.audio.recording.AudioRecord.AudioRecordingState;
import edu.thanhle.audio.recording.Permissions;

public class AudioRecorderActivity extends AppCompatActivity implements AudioRecordObserver
{
    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    private static final int PERMISSION_REQUEST_RECORD = 1000;
    private ViewContainer ui;
    private AudioRecord audioRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);

        keepScreenOn();

        Permissions permissions = new Permissions(this);
        permissions.request(PERMISSION_REQUEST_RECORD, permission.RECORD_AUDIO, permission.READ_EXTERNAL_STORAGE, permission.WRITE_EXTERNAL_STORAGE);

        ui = new ViewContainer(this);
        audioRecord = new AudioRecord(this, this);
        audioRecord.initialize();

        setupEvent();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD)
        {
            if ((grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED))
            {
                Toast.makeText(this, "You must accept the record audio permission request", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
        }
    }


    private void setupEvent()
    {
        ui.buttonRecord.setOnClickListener(view -> audioRecord.record());
        ui.buttonDone.setOnClickListener(view -> audioRecord.done());
        ui.buttonStop.setOnClickListener(view -> audioRecord.stop());
        ui.buttonPlay.setOnClickListener(view -> audioRecord.play());
        ui.buttonRetry.setOnClickListener(view -> {
            if (audioRecord.state() == AudioRecordingState.PLAYING)
            {
                audioRecord.stop();
            }
            audioRecord.initialize();
        });
        ui.buttonAttach.setOnClickListener(view -> finish());
    }

    private void keepScreenOn()
    {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void visible(View view)
    {
        view.setVisibility(View.VISIBLE);
    }

    private void gone(View view)
    {
        view.setVisibility(View.GONE);
    }

    private Uri uriFromFile(File file)
    {
        return FileProvider.getUriForFile(this, AUTHORITY, file);
    }

    @Override
    public void onUpdate(AudioRecordingState state)
    {
        gone(ui.buttonRecord);
        gone(ui.buttonStop);
        gone(ui.buttonPlay);
        gone(ui.buttonRetry);
        gone(ui.buttonAttach);
        gone(ui.buttonDone);
        gone(ui.buttonDoneDisabled);
        ui.message.setText("");

        switch (state)
        {
            case INITIALIZED:
                visible(ui.buttonRecord);
                visible(ui.buttonDoneDisabled);
                ui.message.setText(R.string.recording_initialized);
                ui.progress.setProgress(0);
                break;

            case RECORDING:
                visible(ui.buttonDone);
                ui.message.setText(R.string.recording_recording);
                break;

            case DONE:
                visible(ui.buttonPlay);
                visible(ui.buttonRetry);
                visible(ui.buttonAttach);
                ui.message.setText(R.string.recording_done);
                ui.progress.setProgress(100);
                break;

            case PLAYING:
                visible(ui.buttonStop);
                visible(ui.buttonRetry);
                visible(ui.buttonAttach);
                ui.message.setText(R.string.recording_playing);
                break;
        }
    }

    @Override
    public void onProgress(int progress)
    {
        ui.progress.setProgress(progress);
    }

    @Override
    public void onRecordError()
    {
        // TODO show alert dialog to warn error
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        audioRecord.onBackground();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        audioRecord.release();
    }

    public static class ViewContainer
    {
        public View buttonRecord;
        public View buttonDone;
        public View buttonDoneDisabled;
        public View buttonPlay;
        public View buttonStop;
        public View buttonRetry;
        public View buttonAttach;
        public TextView message;
        public ProgressBar progress;

        public ViewContainer(Activity root)
        {
            message = root.findViewById(R.id.message);
            buttonRecord = root.findViewById(R.id.button_record);
            buttonDone = root.findViewById(R.id.button_done);
            buttonDoneDisabled = root.findViewById(R.id.button_done_disabled);
            buttonPlay = root.findViewById(R.id.button_play);
            buttonStop = root.findViewById(R.id.button_stop);
            buttonRetry = root.findViewById(R.id.button_retry);
            buttonAttach = root.findViewById(R.id.button_attach);
            progress = root.findViewById(R.id.progress);
        }
    }
}
