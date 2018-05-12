package me.samei.xtool.esreporter.v1.common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

public class IndexName {

    static public String PLACE_HOLDER_YEAR = "<year>";
    static public String PLACE_HOLDER_MONTH = "<month>";
    static public String PLACE_HOLDER_DAY_OF_MONTH = "<day_of_month>";

    public final String pattern;
    public final ZoneId zoneId;

    public IndexName(String pattern, String zone) {
        this.pattern = pattern;
        this.zoneId = ZoneId.of(zone);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(getClass().getName())
                .append("( pattern: ").append(pattern)
                .append(", zone: ").append(zoneId)
                .append(")")
                .toString();
    }

    public String generate(long time, Map<String, String> vars) {

        LocalDateTime datetime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(time),
                zoneId
        );

        String temp = pattern
                .replaceAll(PLACE_HOLDER_YEAR, Integer.toString(datetime.getYear()))
                .replaceAll(PLACE_HOLDER_MONTH, Integer.toString(datetime.getMonthValue()))
                .replaceAll(PLACE_HOLDER_DAY_OF_MONTH, Integer.toString(datetime.getDayOfMonth()))
        ;

        if (vars != null && !vars.isEmpty()) {
            for(Map.Entry<String, String> item: vars.entrySet()) {
                temp = temp.replaceAll(item.getKey(), item.getValue());
            }
        }

        return temp;
    }

    public String generate(long time) { return generate(time, null); }
}
