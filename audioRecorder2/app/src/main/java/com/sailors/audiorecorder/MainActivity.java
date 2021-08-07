package com.sailors.audiorecorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    Button startBtn,stopBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wavClass wavObj = new wavClass(Environment.getExternalStorageDirectory().getPath());
        startBtn = findViewById(R.id.startRecord);
        stopBtn = findViewById(R.id.stopRecord);
        startBtn.setOnClickListener(v -> {
            if(checkWritePermission()) {
                wavObj.startRecording();
            }
            if(!checkWritePermission()){
                requestWritePermission();
            }
        });
        stopBtn.setOnClickListener(v -> wavObj.stopRecording());
    }
    private boolean checkWritePermission() {
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        return result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED ;
    }
    private void requestWritePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.MODIFY_AUDIO_SETTINGS,WRITE_EXTERNAL_STORAGE},1);
    }

}