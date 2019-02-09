package com.example.galleryfirebase;


import android.support.annotation.RequiresApi;

import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Image {
    private String imageLink;
    private String imagePath;
    private long timestamp;

    public Image() {
    }

    public Image(String imageLink, String imagePath) {
        this.imageLink = imageLink;
        this.imagePath = imagePath;
        timestamp = new Date().getTime();
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    @Exclude
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        date.setTime(timestamp);
        return sdf.format(date);
    }
}
