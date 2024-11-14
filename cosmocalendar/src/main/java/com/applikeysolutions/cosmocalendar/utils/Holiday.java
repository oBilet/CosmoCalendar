package com.applikeysolutions.cosmocalendar.utils;

import java.util.Date;
import java.util.List;

public class Holiday {
    public Date date;
    public List<String> names;

    public Holiday(Date date, List<String> names) {
        this.date = date;
        this.names = names;
    }
}
