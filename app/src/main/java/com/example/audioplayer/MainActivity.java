package com.example.audioplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    static ListView mListView;
    static List<ListItem> itemsList = new ArrayList<ListItem>();
    private AudioService mService;
    private ComponentName component;

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

    //通过一个URI可以获取外置存储音频文件
    public static Uri MUSIC_URL = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    public static Cursor mCursor;
    //ARTIST 歌手, ALBUM 专辑, DURATION 时长, DATA 音频路径，
    public static String[] mCursorCols = new String[] {
            //"audio._id AS _id", // index must match IDCOLIDX below
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.DURATION};


    //为bindservice准备参数
    private IBinder mbinder;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder ibinder) {
            Log.d("Binder:","client onServiceConnected called.");
            mbinder = ibinder;
            //android.os.Parcel _data = android.os.Parcel.obtain();
            //android.os.Parcel _reply = android.os.Parcel.obtain();
            //mService = mbinder.transact(0, _data, _reply, 1);
            mService = ((AudioService.AudioBinder)mbinder).getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("Binder:","client onServiceDisconnected called.");
            mService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        initButton();
        initListView();
    }

    private void initButton() {
        final Button button_audio = this.findViewById(R.id.button_audio);
        button_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("StevenLog:", "click Audio button.");
                Intent intent = new Intent(MainActivity.this, AudioPlayerActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initListView() {
        mListView = this.findViewById(R.id.listView_audio);
        List<ListItem> itemsList = null;
        new PlaylistAsyncTask().execute(itemsList);
        mListView.setOnItemClickListener(this);
    }

    public class PlaylistAsyncTask extends AsyncTask<List<ListItem>, Void, List<ListItem>> {
        @Override
        protected List<ListItem> doInBackground(List<ListItem>... params){
            mCursor = getContentResolver().query(MUSIC_URL,mCursorCols,
                    "duration > 10000", null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (mCursor == null) {
                return null;
            }
            try {
                mCursor.moveToFirst();
                Log.d("Steven Log", "itemsAsyncTask had been called.");
                while (!mCursor.isAfterLast()) {
                    /*
                    Log.d("Steven Log", "doInBackground had been called.");
                    int titleColumn = mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                    int artistColumn = mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                    String item = mCursor.getString(artistColumn) + " _ " + mCursor.getString(titleColumn);
                    itemsList.add(item);
                     */
                    ListItem item = new ListItem();
                    item.title = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    item.artist = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    item.album = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                    item.lenght = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    item.imgid = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    //item.path = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    item.imgbtm = getAlbumArt(item.imgid);
                    /*
                    Log.d("Steven Log: MusicTitle", item.title);
                    Log.d("Steven Log: artist", item.artist);
                    Log.d("Steven Log: album", item.album);
                    Log.d("Steven Log: lenght", Integer.toString(item.lenght));
                    Log.d("Steven Log: imgid", Integer.toString(item.imgid));
                    */
                    itemsList.add(item);
                    mCursor.moveToNext();
                }
            } catch (MalformedParameterizedTypeException e) {
                e.printStackTrace();
            }
            return itemsList;
        }
        @Override
        protected void onPostExecute(List<ListItem> itemsList) {
            //ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, itemsList);
            ListViewAdapter adapter = new ListViewAdapter(MainActivity.this, 0, itemsList);
            MainActivity.mListView.setAdapter(adapter);
        }
    }

    private Bitmap getAlbumArt(int albumID) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = getContentResolver().query(Uri.parse(mUriAlbums + "/" + Integer.toString(albumID)), projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        Bitmap bm = null;
        if (album_art != null) {
            //            Log.e("LOCAL",album_art);
            bm = BitmapFactory.decodeFile(album_art);
        }
        return bm;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Log.d("StevenLog:", "click Item.");
        mCursor.moveToPosition(position);
        String data = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

        //1.启动service
        Log.d("StevenBind", "1. ItemClick: Create Intent");
        Intent mIntent = new Intent(AudioService.PLAY_ACTION);
        component = new ComponentName(this, AudioService.class);
        mIntent.setComponent(component);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mIntent.setAction(MediaStore.INTENT_ACTION_MEDIA_SEARCH);
        mIntent.putExtra("data", data);
        mIntent.putExtra("position", position);
        //startService(mIntent);
        Log.d("StevenBind", "2. Start bindService.");
        bindService(mIntent, conn, BIND_AUTO_CREATE);
        /*在调起Service和调起界面之前睡眠1s，但是没必要；之前的代码binder实现有问题。
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
         */


        //错误代码，transact--onTransact 如何Override？----20200420
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
            mbinder.transact(0, _data, _reply, 1);
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            _reply.recycle();
            _data.recycle();
        }




        //2. 调起播放界面
        Intent intent = new Intent(MainActivity.this, AudioPlayerActivity.class);
        intent.putExtra("data", data);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(MediaStore.INTENT_ACTION_MEDIA_SEARCH);
        intent.putExtra("data", data);
        intent.putExtra("position", position);
        startActivity(intent, null);
    }

}
