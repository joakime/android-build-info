package com.solshen.buildinfo;

import java.util.ArrayList;

public class InfoModel extends ArrayList<InfoItem> {
    private static final long serialVersionUID = -6424763816920241105L;

    public void addHeader(String header) {
        add(new InfoHeader(header));
    }

    public void addDetail(String key, String value) {
        add(new InfoDetail(key, value));
    }

    public String asText() {
        StringBuilder buf = new StringBuilder();

        for (InfoItem info : this) {
            if (info instanceof InfoHeader) {
                buf.append("\n");
                InfoHeader header = (InfoHeader) info;
                buf.append(header.getHeader()).append("\n");
                buf.append("======================================\n");
                buf.append("\n");
            } else if (info instanceof InfoDetail) {
                InfoDetail detail = (InfoDetail) info;
                buf.append(detail.getKey()).append(" = ");
                buf.append("(").append(detail.getType()).append(") ");
                buf.append(detail.getValue()).append("\n");
            }
        }

        return buf.toString();
    }
}
