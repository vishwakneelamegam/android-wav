package com.example.audiorecorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.FFmpeg;
import com.ibm.cloud.sdk.core.http.HttpMediaType;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    public static final int RequestPermissionCode = 1;
    private static final String API_KEY = "JEZoDtAZDpSBxGJ4RcmyRfoI3HzCHRjQf3JELr-uTISs";
    private static final String URL = "https://api.us-south.speech-to-text.watson.cloud.ibm.com/instances/661f3e57-7e35-42b5-bd58-70cb1502fdcb";
    Button buttonConvert,buttonHear;
    TextView showData;
    JSONArray timeStampData = null;
    String threeGptoMp3 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //used for ibm watson networking else error occur
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        showData = findViewById(R.id.showTrans);
        buttonConvert = findViewById(R.id.playRecord);
        buttonHear = findViewById(R.id.hearWord);
        buttonHear.setOnClickListener(v -> getSelectedString());
        buttonConvert.setOnClickListener(v -> {
            if(checkPermission()){
                new Thread(() -> {
                    convertSpeechToText();
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,"converted to text successfully",Toast.LENGTH_SHORT).show());
                }).start();
            }
            else{
                requestPermission();
            }
        });

    }
    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, RequestPermissionCode);
    }
    public void convertSpeechToText(){
        threeGptoMp3 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "proto2.mp3";
        IamAuthenticator authenticator = new IamAuthenticator(API_KEY);
        SpeechToText speechToText = new SpeechToText(authenticator);
        speechToText.setServiceUrl(URL);
        File audioFile = new File(threeGptoMp3);
        RecognizeOptions options = null;
        try {
            options = new RecognizeOptions.Builder().audio(audioFile).contentType(HttpMediaType.AUDIO_MP3).model("en-US_NarrowbandModel").maxAlternatives(1).timestamps(true).build();
            } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final SpeechRecognitionResults transcript = speechToText.recognize(options).execute().getResult();
            try {
                JSONObject jsobj = new JSONObject(transcript.toString());
                JSONArray jsonArray = jsobj.getJSONArray("results");
                if(jsonArray.length() > 0){
                    JSONArray alternativesArray = jsonArray.getJSONObject(0).getJSONArray("alternatives");
                    if(alternativesArray.length() > 0){
                        JSONObject resultObject = alternativesArray.getJSONObject(0);
                        timeStampData = resultObject.getJSONArray("timestamps");
                        runOnUiThread(() -> {
                            try {
                                showData.setText(resultObject.getString("transcript"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });


                    }else{
                        runOnUiThread(() -> showData.setText(R.string.response));
                    }

                }
                else{
                    runOnUiThread(() -> showData.setText(R.string.response));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }
    public void getSelectedString(){
        int startSelection=showData.getSelectionStart(),endSelection=showData.getSelectionEnd();
        String startTime = "0",endTime = "0",selectedData = showData.getText().toString().substring(startSelection, endSelection).trim();
        if(timeStampData != null && timeStampData.length() > 0){
            for (int j = 0; j < timeStampData.length(); j++) {
                try {
                    if(timeStampData.getJSONArray(j).getString(0).equals(selectedData)){
                        startTime = (timeStampData.getJSONArray(j).getString(1));
                        endTime = (timeStampData.getJSONArray(j).getString(2));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            audioPlayer(startTime,endTime,selectedData);
        }
    }
    public void audioPlayer(String startTime,String endTime,String word){
        String cutAudio = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + word + ".mp3";
        MediaPlayer mp = new MediaPlayer();
        FFmpeg.execute(String.format(" -i %s -ss %s -to %s -c copy %s",threeGptoMp3,startTime,endTime,cutAudio));
        try {
            mp.setDataSource(cutAudio);
            mp.prepare();
            mp.start();
            File fdelete = new File(cutAudio);
            if (fdelete.exists()) {
                if(fdelete.delete()){
                    System.out.println("deleted");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}