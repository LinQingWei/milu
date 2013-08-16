package com.krdavc.video.recorder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author 作者 E-mail: 383781299@qq.com
 * @version 创建时间：2012-3-10 上午11:17:08
 *          关机adapter
 */
public class ShutDownAdapter extends BaseAdapter
{

    private LayoutInflater inflater;

    private int[] imgs = {R.drawable.ic_lock_silent_mode, R.drawable.ic_lock_airplane_mode, R.drawable.ic_lock_power_off};
    private String[] mainTitle = {"静音模式", "飞行模式", "关机"};
    private String[] secondTitle = {"声音已关闭", "已关闭飞行模式", "您的手机会关机"};

    public ShutDownAdapter(Context context)
    {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return 3;
    }

    @Override
    public Object getItem(int position)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.alertdialog, null);
        }

        ImageView alertDialogImage = (ImageView) convertView.findViewById(R.id.alertDialogImage);
        TextView alertDialogT1 = (TextView) convertView.findViewById(R.id.alertDialogT1);
        TextView alertDialogT2 = (TextView) convertView.findViewById(R.id.alertDialogT2);

        alertDialogImage.setBackgroundResource(imgs[position]);
        alertDialogT1.setText(mainTitle[position]);
        alertDialogT2.setText(secondTitle[position]);
        convertView.setTag(mainTitle[position]);
        return convertView;
    }
}
