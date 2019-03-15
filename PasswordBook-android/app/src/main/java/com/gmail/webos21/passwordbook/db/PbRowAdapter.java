package com.gmail.webos21.passwordbook.db;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.webos21.android.ild.ImageLoader;
import com.gmail.webos21.passwordbook.R;

import java.util.List;

public class PbRowAdapter extends BaseAdapter {

    private List<PbRow> pbRows;
    private PbDbInterface pbDb;

    private ImageLoader imgLoader;

    public PbRowAdapter(Context context) {
        PbDbManager dbMan = PbDbManager.getInstance();
        dbMan.init(context);

        pbDb = dbMan.getPbDbInterface();
        pbRows = pbDb.findRows();
    }

    @Override
    public int getCount() {
        return pbRows.size();
    }

    @Override
    public Object getItem(int position) {
        return pbRows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return pbRows.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.pbrow, parent, false);
        }

        if (imgLoader == null) {
            imgLoader = new ImageLoader(context, R.drawable.ic_not_found);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        ImageView iconImageView = (ImageView) convertView.findViewById(R.id.iv_favicon);
        TextView titleTextView = (TextView) convertView.findViewById(R.id.tv_title);
        TextView descTextView = (TextView) convertView.findViewById(R.id.tv_url);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        PbRow pbData = pbRows.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        imgLoader.DisplayImage(pbData.getSiteUrl() + "/favicon.ico", iconImageView);
        titleTextView.setText("[" + pbData.getSiteType() + "] " + pbData.getSiteName());
        descTextView.setText(pbData.getSiteUrl());

        return convertView;
    }

    public void searchItems(String w) {
        pbRows.clear();
        pbRows = pbDb.findRows(w);
    }

    public void searchAll() {
        pbRows.clear();
        pbRows = pbDb.findRows();
    }

}
