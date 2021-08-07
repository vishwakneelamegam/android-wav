package com.sailors.audiorecorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class wavClass {
    String filePath = null;
    String tempRawFile = "temp_record.raw";
    String tempWavFile = "final_record.wav";
    final int bpp = 16;
    int sampleRate = 44100;
    int channel = AudioFormat.CHANNEL_IN_STEREO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    AudioRecord recorder = null;
    int bufferSize = 0;
    Thread recordingThread;
    boolean isRecording = false;
    public wavClass(String path){
        try{
            filePath = path;
            bufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private String getPath(String name){
        try {
            return filePath + "/" + name;
        }
        catch (Exception e){
            return null;
        }
    }
    private void writeRawData(){
        try{
            if(filePath != null) {
                byte[] data = new byte[bufferSize];
                String path = getPath(tempRawFile);
                FileOutputStream fileOutputStream = new FileOutputStream(path);
                if(fileOutputStream != null){
                    int read;
                    while (isRecording){
                        read = recorder.read(data, 0, bufferSize);
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            try {
                                fileOutputStream.write(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                fileOutputStream.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void wavHeader(FileOutputStream fileOutputStream,long totalAudioLen,long totalDataLen,int channels,long byteRate){
        try {
            byte[] header = new byte[44];
            header[0] = 'R'; // RIFF/WAVE header
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[4] = (byte) (totalDataLen & 0xff);
            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
            header[7] = (byte) ((totalDataLen >> 24) & 0xff);
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            header[12] = 'f'; // 'fmt ' chunk
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            header[16] = 16; // 4 bytes: size of 'fmt ' chunk
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            header[20] = 1; // format = 1
            header[21] = 0;
            header[22] = (byte) channels;
            header[23] = 0;
            header[24] = (byte) ((long) sampleRate & 0xff);
            header[25] = (byte) (((long) sampleRate >> 8) & 0xff);
            header[26] = (byte) (((long) sampleRate >> 16) & 0xff);
            header[27] = (byte) (((long) sampleRate >> 24) & 0xff);
            header[28] = (byte) (byteRate & 0xff);
            header[29] = (byte) ((byteRate >> 8) & 0xff);
            header[30] = (byte) ((byteRate >> 16) & 0xff);
            header[31] = (byte) ((byteRate >> 24) & 0xff);
            header[32] = (byte) (2 * 16 / 8); // block align
            header[33] = 0;
            header[34] = bpp; // bits per sample
            header[35] = 0;
            header[36] = 'd';
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = (byte) (totalAudioLen & 0xff);
            header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
            header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
            header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
            fileOutputStream.write(header, 0, 44);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void createWavFile(String tempPath,String wavPath){
        try {
            FileInputStream fileInputStream = new FileInputStream(tempPath);
            FileOutputStream fileOutputStream = new FileOutputStream(wavPath);
            byte[] data = new byte[bufferSize];
            int channels = 2;
            long byteRate = bpp * sampleRate * channels / 8;
            long totalAudioLen = fileInputStream.getChannel().size();
            long totalDataLen = totalAudioLen + 36;
            wavHeader(fileOutputStream,totalAudioLen,totalDataLen,channels,byteRate);
            while (fileInputStream.read(data) != -1) {
                fileOutputStream.write(data);
            }
            fileInputStream.close();
            fileOutputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void startRecording(){
        try{
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channel,audioEncoding, bufferSize);
            int status = recorder.getState();
            if(status == 1){
                recorder.startRecording();
                isRecording = true;
            }
            recordingThread = new Thread(this::writeRawData);
            recordingThread.start();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void stopRecording(){
        try{
            if(recorder != null) {
                isRecording = false;
                int status = recorder.getState();
                if (status == 1) {
                    recorder.stop();
                }
                recorder.release();
                recordingThread = null;
                createWavFile(getPath(tempRawFile),getPath(tempWavFile));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
