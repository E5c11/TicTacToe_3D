package com.esc.test.apps.adapters;

import android.app.Application;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.esc.test.apps.gamestuff.CubeID;

public class CubeAdapter extends BaseAdapter {

    private final Application application;
    private final CubeID[] bPosition;
    private final int screenWidth;

    public CubeAdapter(Application application, CubeID[] bPosition) {
        this.application = application;
        this.bPosition = bPosition;
        DisplayMetrics metrics = application.getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView button;
        if (view == null) {
            button = new ImageView(application);
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
