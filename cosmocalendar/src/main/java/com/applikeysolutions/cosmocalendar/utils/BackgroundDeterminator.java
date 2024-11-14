package com.applikeysolutions.cosmocalendar.utils;

import java.util.Date;

public class BackgroundDeterminator {
    public Date getDate() {
        return date;
    }

    public int getColor() {
        return color;
    }

    Date date;
    int color;

    public BackgroundDeterminator(Date date, int color) {
        this.date = date;
        this.color = color;
    }
}
