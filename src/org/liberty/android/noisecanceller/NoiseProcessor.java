/*
Copyright (C) 2010 Haowen Ning

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

package org.liberty.android.noisecanceller;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;


class NoiseProcessor extends Thread{
    private int frequency;
    private int channelConfiguration;
    private int audioEncoding; 
    private int bufferSize;
    private final String TAG = "NoiseProcessor";
    private short[] buffer;
    private AudioRecord audioRecord = null;
    private AudioTrack audioTrack = null;
    private  boolean isRecording;
    private int mAngle = 180;
    private float mAmp = 1.0f;
    private boolean isPaused;

    public NoiseProcessor() throws Exception{
        //frequency = 11025;
        channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        audioEncoding =  AudioFormat.ENCODING_PCM_16BIT;
        //bufferSize = 1024;

        String recordSupport = "";
        String playSupport = "";
        bufferSize = 16384;

        for(int freq : new int[]{11025, 22050, 44100, 16000, 8000}){
            frequency = freq;
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    frequency,
                    channelConfiguration,
                    audioEncoding,
                    bufferSize);
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    frequency,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM);
            if(audioRecord.getState() == AudioRecord.STATE_INITIALIZED &&
                audioTrack.getState() == AudioTrack.STATE_INITIALIZED){
                break;
            }
            if(audioRecord.getState() == AudioRecord.STATE_INITIALIZED){
                recordSupport += "" + freq + " ";
                audioRecord.release();
            }
            if(audioTrack.getState() == AudioTrack.STATE_INITIALIZED){
                playSupport += "" + freq + " ";
                audioTrack.release();
            }
        }
        if(audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED){
            throw new Exception("Can initialize AudioRecord. Supported sample rate: " + recordSupport);
        }

        if(audioTrack.getState() == AudioTrack.STATE_UNINITIALIZED){
            throw new Exception("Can't initial AudioTrack. Supported sample rate: " + playSupport);
        }

        isRecording = true;
        buffer = new short[bufferSize];

    }


    public void run(){
        record();
    }

    @Override
    public void destroy(){
        isRecording = false;
        try{
            wait(200);
        }
        catch(Exception e){
        }

    }

    public void pauseRecord(){
        isPaused = false;
    }

    public void resumeRecord(){
        isPaused = true;
    }

    public float getAmp(){
        return mAmp;
    }

    public int getAngle(){
        return mAngle;
    }

    public void setAmp(float amp){
        mAmp = amp;
    }

    public void setAngle(int angle){
        mAngle = angle;
    }

    public boolean getStatus(){
        return isRecording;
    }

    



    public synchronized void record(){

        audioRecord.startRecording();
        audioTrack.play();
        while(isRecording){
            if(isPaused){
                try{
                    wait(100);
                }
                catch(Exception e){
                }
            }
            else{
                int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                processBuffer(mAmp, mAngle);
                audioTrack.write(buffer, 0, bufferReadResult);
            }
        }
        audioRecord.stop();
        audioTrack.stop();
    }
    static{
        System.loadLibrary("process-buffer");
    }
    
    public native void processBuffer(float amp, int angle);

}



        




