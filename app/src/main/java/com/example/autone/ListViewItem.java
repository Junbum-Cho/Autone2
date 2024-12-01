package com.example.autone;

import android.graphics.drawable.Drawable;

public class ListViewItem {
    private Drawable icon ;
    private String text ;
    private boolean checked;

    public void setIcon(Drawable icon) {
        this.icon = icon ;
    }
    public void setText(String text) {
        this.text = text ;
    }
    public void setChecked(boolean checked) {
        this.checked = checked;
    }
    public boolean isChecked() {
        return checked;
    }
    public Drawable getIcon() {
        return this.icon ;
    }
    public String getText() {
        return this.text ;
    }
}
