package me.samei.xtool.esreporter.v1.common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class IndexName {

    static public String PLACE_HOLDER_YEAR = "<year>";
    static public String PLACE_HOLDER_MONTH = "<month>";
    static public String PLACE_HOLDER_DAY_OF_MONTH = "<day-of-month>";
    static public String PLACE_HOLDER_SOURCE_ID = "<source-id>";

    public final String pattern;
    public final ZoneId zoneId;
    public final String sourceId;

    public IndexName(String pattern, String zone, String sourceId) {
        this.pattern = pattern;
        this.zoneId = ZoneId.of(zone);
        this.sourceId = sourceId;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(getClass().getName())
                .append("( pattern: ").append(pattern)
                .append(", zone: ").append(zoneId)
                .append(", source-id: ").append(sourceId)
                .append(")")
                .toString();
    }

    public String generate(long time) {

        LocalDateTime datetime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(time),
                zoneId
        );

        return pattern
                .replaceAll(PLACE_HOLDER_YEAR, Integer.toString(datetime.getYear()))
                .replaceAll(PLACE_HOLDER_MONTH, Integer.toString(datetime.getMonthValue()))
                .replaceAll(PLACE_HOLDER_DAY_OF_MONTH, Integer.toString(datetime.getDayOfMonth()))
                .replaceAll(PLACE_HOLDER_SOURCE_ID, sourceId)
        ;
    }
}
