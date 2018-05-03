package me.samei.xtool.esreporter.v1.common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class IndexName {

    static public String PLACE_HOLDER_YEAR = "<year>";
    static public String PLACE_HOLDER_MONTH = "<month>";
    static public String PLACE_HOLDER_DAY_OF_MONTH = "<day-of-month>";

    public final String rawPattern;
    public final ZoneId zoneId;

    public IndexName(String raw, String zone) {
        rawPattern = raw;
        zoneId = ZoneId.of(zone);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(getClass().getName())
                .append("(").append(rawPattern).append(")")
                .toString();
    }

    public String generate(long time) {
        LocalDateTime datetime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(time),
                zoneId
        );

        return rawPattern
                .replaceAll(PLACE_HOLDER_YEAR, Integer.toString(datetime.getYear()))
                .replaceAll(PLACE_HOLDER_MONTH, Integer.toString(datetime.getMonthValue()))
                .replaceAll(PLACE_HOLDER_DAY_OF_MONTH, Integer.toString(datetime.getDayOfMonth()))
        ;
    }
}
