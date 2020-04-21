package com.example.audioplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class AudioPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mPrevious;
    private Button mPlay;
    private Button mNext;
    private Button mPause;
    private Button mMode;
    private TextView mText;
    private ComponentName component;
    String data;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        verifyStoragePermissions(this);

        final Intent receiveIntent = this.getIntent();
        if(receiveIntent != null){
            Log.d ("StevenLog:","Get click intent to switch to Audio Activity.");
        } else if (receiveIntent.getFlags() == Intent.FLAG_ACTIVITY_NEW_TASK) {
            component = new ComponentName(this, AudioService.class);
            receiveIntent.setComponent(component);
            receiveIntent.setAction(AudioService.PLAY_ACTION);
            startService(receiveIntent);
        }
        data = receiveIntent.getStringExtra("data");
        position = receiveIntent.getIntExtra("position", 0);
        initToHomeButton();
        initTextView();
        initCtlButton();
        initSeekbar();
    }

    //为bindservice准备参数
    private IBinder mbinder = null;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder ibinder) {
            Log.d("Binder:","client onServiceConnected called.");
            mbinder = ibinder;
        }
        public void onServiceDisconnected(ComponentName name) {
            Log.d("Binder:","client onServiceDisconnected called.");
        }
    };

    //获取读写权限
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_AUDIO_SETTINGS = 2;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.WAKE_LOCK
    };
    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity, "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
            permission = ActivityCompat.checkSelfPermission(activity, "android.permission.MODIFY_AUDIO_SETTINGS");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_AUDIO_SETTINGS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initToHomeButton() {
        final Button mButton = this.findViewById(R.id.button_AudiotoHome);
        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initTextView() {
        final TextView mTextView = this.findViewById(R.id.textview_mediainfo);
        //mTextView.setText();
    }

    private void initCtlButton() {
        mPrevious = this.findViewById(R.id.previous);
        mPlay = this.findViewById(R.id.play);
        mNext = this.findViewById(R.id.next);
        mPause = this.findViewById(R.id.pause);
        mMode = this.findViewById(R.id.button_mode);
        mText = this.findViewById(R.id.textview_mode);
        //实例化onClick()
        mPrevious.setOnClickListener(this);
        mPlay.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mPause.setOnClickListener(this);
        mMode.setOnClickListener(this);
    }

    @Override    //CtlButton点击事件监听
    public void onClick(View v) {
        component = new ComponentName(this, AudioService.class);
        //position = MainActivity.mCursor.getPosition();
        if(v == mPrevious){
            Intent mIntent = new Intent(AudioService.PREVIOUS_ACTION);
            mIntent.setComponent(component);
            startService(mIntent);
            Log.d("StevneLog:", "previous button had been press.");
        }else if(v == mPlay){
            Intent mIntent = new Intent(AudioService.PLAY_ACTION);
            mIntent.setComponent(component);
            mIntent.putExtra("data", data);
            mIntent.putExtra("position", position);
            startService(mIntent);
        }else if(v == mNext){
            Intent mIntent = new Intent(AudioService.NEXT_ACTION);
            mIntent.setComponent(component);
            startService(mIntent);
        }else if(v == mPause){
            Intent mIntent = new Intent(AudioService.PAUSE_ACTION);
            mIntent.setComponent(component);
            startService(mIntent);
        }else if(v == mMode){
            switch (AudioService.MODE){
                case AudioService.ALL_CYCLE:
                    AudioService.MODE = AudioService.SINGLE_CYCLE;
                    mText.setText("单曲循环");
                    break;
                case AudioService.SINGLE_CYCLE:
                    AudioService.MODE = AudioService.RANDOM_PLAY;
                    mText.setText("随机播放");
                    break;
                case AudioService.RANDOM_PLAY:
                    AudioService.MODE = AudioService.ALL_CYCLE;
                    mText.setText("列表循环");
                    break;
                default:
            }
        }
    }

    private void initSeekbar() {
        SeekBar mSeekbar = this.findViewById(R.id.seekbar_audio);
        //mSeekbar.setMax();
    }


}
