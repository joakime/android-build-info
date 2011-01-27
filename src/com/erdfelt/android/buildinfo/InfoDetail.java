package com.erdfelt.android.buildinfo;

public class InfoDetail implements InfoItem {
    public static final int VIEW_TYPE = 0;
    private String key;
    private String type;
    private String value;
    
    public InfoDetail(String key, String value) {
        this.key = key;
        this.value = value;
        this.type = "String";
    }

    public InfoDetail(String key, int value) {
        this.key = key;
        this.value = Integer.toString(value);
        this.type = "int";
    }

    public InfoDetail(String key, float value) {
        this.key = key;
        this.value = Float.toString(value);
        this.type = "float";
    }
    
    @Override
    public int getViewType() {
        return VIEW_TYPE;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public String getType() {
        return type;
    }
}
