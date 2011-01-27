package com.erdfelt.android.buildinfo;

import java.util.ArrayList;

public class InfoModel extends ArrayList<InfoItem> {
    private static final long serialVersionUID = -6424763816920241105L;

    public void addHeader(String header) {
        add(new InfoHeader(header));
    }

    public void addDetail(String key, String value) {
        add(new InfoDetail(key, value));
    }
}
