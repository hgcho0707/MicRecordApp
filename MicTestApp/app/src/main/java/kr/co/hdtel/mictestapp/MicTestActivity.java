package kr.co.hdtel.mictestapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MicTestActivity extends AppCompatActivity implements View.OnClickListener, AudioAdapter.OnIconClickListener {
    private ImageButton mAudioRecordImageBtn;
    private TextView mAudioRecordText;
    private RadioGroup mRadioGroup;

    private MediaRecorder mMediaRecorder;
    private String mAudioFileName;
    private boolean isRecording = false;

    private MediaPlayer mMediaPlayer = null;
    private Boolean isPlaying = false;
    private ImageView mPlayIcon;

    private AudioAdapter mAudioAdapter;
    private ArrayList<Uri> mAudioList;

    private int mAudioSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mic_test);
        initView();
        setRecyclerView();
        selectMic();
    }

    private void initView() {
        mAudioRecordImageBtn = findViewById(R.id.btn_audio_record);
        mAudioRecordText = findViewById(R.id.tv_audio_record);
        mRadioGroup = findViewById(R.id.rg_select_mic);
        mAudioList = new ArrayList<>();
        mAudioAdapter = new AudioAdapter(this, mAudioList);
        mAudioAdapter.setOnItemClickListener(this);
        mAudioRecordImageBtn.setOnClickListener(this);
        mAudioSource = MediaRecorder.AudioSource.MIC;
    }

    private void setRecyclerView() {
        RecyclerView audioRecyclerView = findViewById(R.id.recyclerview);
        audioRecyclerView.setAdapter(mAudioAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        audioRecyclerView.setLayoutManager(mLayoutManager);
    }

    private void selectMic() {
        mRadioGroup.check(R.id.rb_first);
        mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_first) {
                mAudioSource = MediaRecorder.AudioSource.MIC;
            } else if (checkedId == R.id.rb_second){
                mAudioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
            }
        });
    }

    // 오디오 파일 권한 체크
    private boolean checkAudioPermission() {
        String recordPermission = Manifest.permission.RECORD_AUDIO;
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), recordPermission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            int PERMISSION_CODE = 21;
            ActivityCompat.requestPermissions(this, new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }

    // 녹음 시작
    private void startRecording(int audioSource) {
        //Media Recorder 생성 및 설정
        String recordPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        mMediaRecorder = new MediaRecorder();
        if(audioSource == MediaRecorder.AudioSource.MIC){
            mAudioFileName = recordPath + "/" + "BasicMic_" + timeStamp + "_" + "audio.mp4";
            mMediaRecorder.setAudioSource(audioSource);
        } else if(audioSource == MediaRecorder.AudioSource.VOICE_RECOGNITION){
            mAudioFileName = recordPath + "/" + "VoiceRecognitionMic_" + timeStamp + "_" + "audio.mp4";
            mMediaRecorder.setAudioSource(audioSource);
        }

        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mAudioFileName);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
        mMediaRecorder.setAudioEncodingBitRate(384000);
        mMediaRecorder.setAudioSamplingRate(48000);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMediaRecorder.start();
    }

    // 녹음 종료
    private void stopRecording() {
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;
        Uri audioUri = Uri.parse(mAudioFileName);
        mAudioList.add(audioUri);

        mAudioAdapter.notifyDataSetChanged();
    }

    // 녹음 파일 재생
    private void playAudio(File file) {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(file.getAbsolutePath());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mPlayIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_audio_pause, null));
        isPlaying = true;

        mMediaPlayer.setOnCompletionListener(mp -> stopAudio());

    }

    // 녹음 파일 중지
    private void stopAudio() {
        mPlayIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_audio_play, null));
        isPlaying = false;
        mMediaPlayer.stop();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_audio_record) {
            if (isRecording) {
                isRecording = false;
                mAudioRecordImageBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_record, null));
                mAudioRecordText.setText("녹음 시작");
                stopRecording();
            } else {
                if (checkAudioPermission()) {
                    isRecording = true;
                    mAudioRecordImageBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_recording_red, null));
                    mAudioRecordText.setText("녹음 중");
                    startRecording(mAudioSource);
                }
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        String uriName = String.valueOf(mAudioList.get(position));
        File file = new File(uriName);

        if (isPlaying) {
            if (mPlayIcon == view) {
                stopAudio();
            } else {
                stopAudio();
                mPlayIcon = (ImageView) view;
                playAudio(file);
            }
        } else {
            mPlayIcon = (ImageView) view;
            playAudio(file);
        }
    }
}
