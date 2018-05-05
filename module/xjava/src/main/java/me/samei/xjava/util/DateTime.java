package me.samei.xjava.util;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTime {

    public static DateTimeFormatter formatter(String pattern, String zone) {
        return DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.of(zone));
    }

    public static DateTimeFormatter formatterOfSimpleUTC() {
        return formatter("yyyy-MM-dd HH:mm:ss", "UTC");
    }

}
