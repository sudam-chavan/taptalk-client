package com.taptalkclient;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.PrintWriter;

/**
 * Created by sudam on 08/08/15.
 */
public class AudioThread extends Thread {

    private static final String TAG = "##AudioThread##";

    private boolean stopped = false;
    private PrintWriter mServerWriter;

    public AudioThread(PrintWriter serverWriter){
        mServerWriter = serverWriter;
        start();
    }

    @Override
    public void run(){
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        AudioRecord recorder = null;
        short[][] buffers = new short[256][160];
        int ix = 0;
        try{
            int N = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    8000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    N * 10);
            recorder.startRecording();
            while(!stopped){
                short [] buffer = buffers[ix++ % buffers.length];
                N = recorder.read(buffer, 0, buffer.length);
                Log.e(TAG, "Read Some: " + buffer);
                sendToserver(buffer);
            }
        }catch (Exception e){
            Log.e(TAG, "EXCEPTION: " + e.getLocalizedMessage());
        }finally {
            if(recorder != null)
                recorder.stop();
        }
    }

    public void stopIt(){
        stopped = true;
    }

    public void sendToserver(final short []buffer){
        mServerWriter.print(buffer.length);
        mServerWriter.print(buffer);
    }
}
