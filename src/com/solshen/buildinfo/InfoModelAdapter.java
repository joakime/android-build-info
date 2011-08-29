package com.solshen.buildinfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class InfoModelAdapter extends BaseAdapter {
    private InfoModel      model;
    private LayoutInflater inflater;

    public InfoModelAdapter(LayoutInflater inflater, InfoModel model) {
        super();
        this.inflater = inflater;
        this.model = model;
    }

    public InfoModel getModel() {
        return model;
    }

    public void setModel(InfoModel model) {
        this.model = model;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public int getCount() {
        return model.size();
    }

    @Override
    public Object getItem(int position) {
        return model.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        InfoItem item = model.get(position);
        if (item == null) {
            return (-1);
        }
        return item.getViewType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        InfoItem item = model.get(position);

        if (item == null) {
            return null;
        }

        switch (item.getViewType()) {
        case InfoHeader.VIEW_TYPE:
            return getHeaderView((InfoHeader) item, convertView, parent);
        case InfoDetail.VIEW_TYPE:
            return getDetailView((InfoDetail) item, convertView, parent);
        }

        return null;
    }

    private View getDetailView(InfoDetail item, View convertView, ViewGroup parent) {
        View view = convertView;
        
        if(convertView == null) {
            view = inflater.inflate(R.layout.itemdetail, null);
        }
        
        TextView txtkey = (TextView) view.findViewById(R.id.keyname);
        TextView txtval = (TextView) view.findViewById(R.id.value);
        
        txtkey.setText(item.getKey());
        txtval.setText(item.getValue());
        
        return view;
    }

    private View getHeaderView(InfoHeader item, View convertView, ViewGroup parent) {
        TextView text = (TextView) convertView;

        if (text == null) {
            text = (TextView) inflater.inflate(R.layout.header, null);
        }
        
        text.setText(item.getHeader());

        return text;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != InfoHeader.VIEW_TYPE;
    }
}
