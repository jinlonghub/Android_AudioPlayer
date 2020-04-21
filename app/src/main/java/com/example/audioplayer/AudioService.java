package com.example.audioplayer;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Random;

/**
 * Created by SJin2 on 2020/3/20.
 */
public class AudioService extends Service {

    Intent sIntent ;
    Intent nullIntent = null;
    private AudioBinder mBinder = new AudioBinder();;
    public static MediaPlayer mAudioPlayer = new MediaPlayer();;
    //播放模式
    public static int MODE = 1;
    public static final int ALL_CYCLE = 1;        //全部循环
    public static final int SINGLE_CYCLE = 2;     //单曲循环
    public static final int RANDOM_PLAY = 3;      //随机播放
    private int mPlayPosition = 0;
    public static final String PLAY_ACTION = "com.tutor.music.PLAY_ACTION";
    public static final String PAUSE_ACTION = "com.tutor.music.PAUSE_ACTION";
    public static final String NEXT_ACTION = "com.tutor.music.NEXT_ACTION";
    public static final String PREVIOUS_ACTION = "com.tutor.music.PREVIOUS_ACTION";
    //video
    public static final String VIDEO_PLAY_ACTION = "com.tutor.video.PLAY_ACTION";

    //构造函数
    public AudioService() { }


    @Override
    public void onCreate() {
        Log.d("Binder:", "onCreate tirggered ...");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioPlayer.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Binder:", "onBind tirggered ...");
        sIntent = intent;
        return mBinder;
    }

    public class AudioBinder extends Binder {
        //private AudioBinder() { }

        AudioService getService() {
            return AudioService.this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel replay, int flags) {
            Log.d("Binder:", "onTransact tirggered ...");
            switch (code) {
                case 0: {
                    if (sIntent.equals(PLAY_ACTION)) {
                        play(sIntent);
                    }
                    return true;
                }
            }
            //return super.onTransact(code, data, replay, flags);
            return true;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d("StevneLog:", "onStartCommand had been called.");
        String action = intent.getAction();
        if(action.equals(PLAY_ACTION)){
            play(intent);
        }else
        if(action.equals(PAUSE_ACTION)){
            pause();
        }else
        if(action.equals(NEXT_ACTION)){
            next(nullIntent);
        }else
        if(action.equals(PREVIOUS_ACTION)){
            previous(nullIntent);
        }else if(action.equals(VIDEO_PLAY_ACTION)){
            //VideoActivity.video_play();
        }
        return 1;  //具体return值需要在研究一下--20200320
    }

    public void play(Intent intent) {
        playMusic(intent);
    }

    public void pause() {
        stopSelf();
    }

    public void previous(Intent nullIntent) {
        if (mPlayPosition == 0) {
            mPlayPosition = MainActivity.mCursor.getCount() - 1;
        } else {
            mPlayPosition--;
        }
        playMusic(nullIntent);
    }

    public void next(Intent nullIntent) {
        if (mPlayPosition == MainActivity.mCursor.getCount() - 1) {
            mPlayPosition = 0;
        } else {
            mPlayPosition++;
        }
        playMusic(nullIntent);
    }

    public void playMusic(Intent intent) {
        Log.d("StevneLog:", "PlayMusic had been called.");
        String dataSource;
        String info;
        mAudioPlayer.reset();
        if (intent == nullIntent) {
            dataSource = getDateByPosition(MainActivity.mCursor, mPlayPosition);
            info = getInfoByPosition(MainActivity.mCursor, mPlayPosition);
        } else {
            dataSource = intent.getStringExtra("data");
            int position = Integer.parseInt(intent.getStringExtra("position"));
            info = getInfoByPosition(MainActivity.mCursor, position);
        }
        Toast.makeText(getApplicationContext(), dataSource, Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), info, Toast.LENGTH_SHORT).show();
        try {
            mAudioPlayer.setDataSource(dataSource);
            //mAudioPlayer.prepare();
            //mAudioPlayer.start();
            //异步缓冲：准备及监听
            mAudioPlayer.prepareAsync();
            mAudioPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mAudioPlayer.start();
                }
            });

            //播放结束监听
            mAudioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    switch (MODE) {
                        case SINGLE_CYCLE:
                            //单曲循环
                            mAudioPlayer.start();
                            break;
                        case ALL_CYCLE:
                            //全部循环
                            //isSetData = false;
                            next(nullIntent);
                            break;
                        case RANDOM_PLAY:
                            //随机播放
                            //isSetData = false;
                            Random random = new Random();
                            mPlayPosition = random.nextInt(MainActivity.mCursor.getCount());
                            playMusic(nullIntent);
                            break;
                        default:
                    }
                }
            });
        } catch (IllegalArgumentException e1) {
            e1.printStackTrace();
        } catch (IllegalStateException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    //根据位置来获取歌曲位置
    public String getDateByPosition(Cursor c, int position){
        c.moveToPosition(position);
        int dataColumn = c.getColumnIndex(MediaStore.Audio.Media.DATA);
        String data = c.getString(dataColumn);
        return data;
    }

    //获取当前播放歌曲演唱者及歌名
    public String getInfoByPosition(Cursor c,int position){
        c.moveToPosition(position);
        int titleColumn = c.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artistColumn = c.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        String info = c.getString(artistColumn)+" "  + c.getString(titleColumn);
        return info;
    }

    public String getTrackName() {
        synchronized (this) {
            if (MainActivity.mCursor == null) {
                return null;
            }
            return MainActivity.mCursor.getString(MainActivity.mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        }
    }

}
