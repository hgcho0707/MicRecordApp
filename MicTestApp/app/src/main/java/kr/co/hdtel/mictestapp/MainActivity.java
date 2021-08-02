package kr.co.hdtel.mictestapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private String mPath = null;
    private Button mRecord;
    private Button mPlay;
    private Button mStop;
    private Date mDate;

    boolean isRecording = false;
    private static String FILE_NAME_FORMAT = "yyyyMMddHHmmss";

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1==1){
                mRecord.setEnabled(true);
                mPlay.setEnabled(true);
                mStop.setEnabled(false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mPath = Environment.getExternalStorageDirectory().getAbsolutePath() + getFileName(".mp4");
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        }
    }

    private void initView(){
        mRecorder = new MediaRecorder();
        mRecord = findViewById(R.id.btn_record);
        mPlay = findViewById(R.id.btn_play);
        mStop = findViewById(R.id.btn_stop);
        mRecord.setEnabled(true);
        mPlay.setEnabled(false);
        mStop.setEnabled(false);
        mRecord.setOnClickListener(this);
        mPlay.setOnClickListener(this);
        mStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_record :
                setRecord();
                break;

            case R.id.btn_play :
                setPlay();
                break;

            case R.id.btn_stop :
                setStop();
                break;

            default : break;
        }
    }

    private void setRecord(){
        mRecord.setEnabled(false);
        mPlay.setEnabled(false);
        mStop.setEnabled(true);
        mPath = Environment.getExternalStorageDirectory().getAbsolutePath() + getFileName(".mp4");

        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
        mRecorder.setAudioEncodingBitRate(384000);
        mRecorder.setAudioSamplingRate(48000);

        Log.d("Record", "file path is " + mPath);
        mRecorder.setOutputFile(mPath);
        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        isRecording = true;
    }


    private void setPlay(){
        mRecord.setEnabled(false);
        mPlay.setEnabled(false);
        mStop.setEnabled(true);

        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mPath);
            mPlayer.prepare();
            mPlayer.start();

            Thread thread = new Thread(new myThread());
            thread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setStop(){
        mRecord.setEnabled(true);
        mPlay.setEnabled(true);
        mStop.setEnabled(false);

        if(isRecording){
            try {
                mRecorder.stop();
                isRecording = false;
                Toast.makeText(getApplicationContext(), "녹음중지", Toast.LENGTH_LONG).show();
            } catch (IllegalStateException e){
                mRecorder = null;
                mRecorder = new MediaRecorder();
            }

            mRecorder.release();
            mRecorder = null;

        } else {
            mPlayer.stop();
            mPlayer = null;
            Toast.makeText(getApplicationContext(), "재생중지", Toast.LENGTH_LONG).show();
        }
    }

    public class myThread implements Runnable{
        @Override
        public void run() {
            while(true){
                if(!mPlayer.isPlaying()) {
                    mPlayer.stop();
                    mPlayer = null;

                    Message msg = new Message();
                    msg.arg1 = 1;
                    handler.sendMessage(msg);
                    return;
                }
            }
        }
    }

    private String getFileName(String extension) {
        mDate = new Date();
        return "/test/"+ date2DateString(FILE_NAME_FORMAT, mDate) + extension;
    }

    public static String date2DateString(String format, Date date) {
        SimpleDateFormat dt = new SimpleDateFormat(format);
        return dt.format(date);
    }
}