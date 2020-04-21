package com.example.audioplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by SJin2 on 2020/4/3.
 */
public class ListViewAdapter extends ArrayAdapter<ListItem> {
    private int resourceId;
    private int selectedItem = -1;  //标记是否是被点击的音乐item
    public Context context;
    public List<ListItem> audioList;

    private class ItemHolder {
        TextView titletv;
        TextView artisttv;
        TextView length;
        ImageView albumimg = null;
    }

    public ListViewAdapter(Context Context, int ResourceID, List<ListItem> AudioList) {
        super(Context, ResourceID, AudioList);
        this.context = Context;
        this.resourceId = ResourceID;
        this.audioList = AudioList;
    }//创建有参构造方法，给参数传值。

    @Override
    public View getView(int positon, View convertView, ViewGroup parent) {
        //ListItem item = getItem(positon);
        //View view;
        ItemHolder itemHolder;

        if (convertView == null) {
            //创建布局视图
            //convertView = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listitem_layout, null);
            //创建布局控件
            itemHolder = new ItemHolder();
            itemHolder.titletv = convertView.findViewById(R.id.song_title_tv);
            itemHolder.artisttv = convertView.findViewById(R.id.song_artist_tv);
            itemHolder.length = convertView.findViewById(R.id.song_length_tv);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ItemHolder) convertView.getTag();
        }
        //给控件赋值
        ListItem item = audioList.get(positon);
        itemHolder.titletv.setText(item.title);
        itemHolder.artisttv.setText(item.artist+"_"+item.album);
        //itemHolder.length.setTag(item.lenght);
        itemHolder.length.setText(toTime((int)item.lenght));

        if (item.imgbtm != null) {
            itemHolder.albumimg.setImageBitmap(item.imgbtm);
            Log.d("Steven Log", "Use Music image.");
        } else {
            //专辑图片为null,使用xml中设置的默认图片。
            Log.d("Steven Log", "Use Default image.");
            //报空引用：itemHolder.albumimg.setImageResource(R.mipmap.ic_launcher_round);
            //无法显示：itemHolder.albumimg = convertView.findViewById(R.mipmap.ic_launcher_round);
            //Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            //itemHolder.albumimg.setImageBitmap(bitmap);
        }
        return convertView;
    }
    /*
    private void setImageDrawable () {
        ImageView defaultImage = (ImageView) findViewById(R.mipmap.ic_launcher_round);
        defaultImage.setImageDrawable();
    }
     */

    public String toTime (int length) {
        length /= 1000;   //从ms转化为s
        int hour = length / 3600;
        int minute = length / 60;
        int second = length % 60;
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
    }

    /*
    @Override
    public Object getItem(int position) {
        return audioList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

     */

}
