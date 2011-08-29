package com.solshen.buildinfo;

public class InfoHeader implements InfoItem {
    public static final int VIEW_TYPE = 1;
    private String header;
    
    public InfoHeader(String header) {
        this.header = header;
    }

    @Override
    public int getViewType() {
        return VIEW_TYPE;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
