package com.esc.test.apps.common.adaptors;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.esc.test.apps.data.objects.pojos.CubeID;

public class CubeAdapter extends BaseAdapter {

    private final Context context;
    private final CubeID[] bPosition;
    private final int screenWidth;

    public CubeAdapter(Context context, CubeID[] bPosition) {
        this.context = context;
        this.bPosition = bPosition;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView button;
        if (view == null) {
            button = new ImageView(context);
            button.setLayoutParams(new GridView.LayoutParams(screenWidth/10, screenWidth/10));
            button.setBackground(null);
            button.setTag(bPosition[i]);
        } else button = (ImageView) view;
        return button;
    }

    @Override
    public int getCount() {
        return bPosition.length;
    }

    @Override
    public Object getItem(int i) {
        return bPosition[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public static int[] getGridAdapter(String pos) {
        int[] adapter = new int[2];
        int i = Integer.parseInt(pos);
        adapter[0] = i / 16;
        adapter[1] = i - (16 * adapter[0]);
        return adapter;
    }
}
