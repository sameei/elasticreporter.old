package me.samei.xtool.esreporter.v1.common;

import org.slf4j.LoggerFactory;

import javax.print.DocFlavor;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.regex.Matcher;

public class IndexName {

    static public String PLACE_HOLDER_YEAR = "<year>";
    static public String PLACE_HOLDER_MONTH = "<month>";
    static public String PLACE_HOLDER_DAY_OF_MONTH = "<day_of_month>";
    static public String PLACE_HOLDER_OF_MILLIS = "<millis>";

    public final String indexPattern;
    public final String identityPattern;
    public final ZoneId zoneId;

    public IndexName(
            String indexPattern,
            String identityPattern,
            String zone
    ) {
        this.indexPattern = indexPattern;
        this.identityPattern = identityPattern;
        this.zoneId = ZoneId.of(zone);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(getClass().getName())
                .append("( indexPattern: ").append(indexPattern)
                .append(", identityPattern: ").append(identityPattern)
                .append(", zone: ").append(zoneId)
                .append(")")
                .toString();
    }

    protected String generate(String pattern, long time, Map<String, String> vars) {

        LocalDateTime datetime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(time),
                zoneId
        );

        String temp = pattern
                .replaceAll(PLACE_HOLDER_YEAR, Integer.toString(datetime.getYear()))
                .replaceAll(PLACE_HOLDER_MONTH, Integer.toString(datetime.getMonthValue()))
                .replaceAll(PLACE_HOLDER_DAY_OF_MONTH, Integer.toString(datetime.getDayOfMonth()))
                .replaceAll(PLACE_HOLDER_OF_MILLIS, Long.toString(time))
        ;

        if (vars != null && !vars.isEmpty()) {
            for(Map.Entry<String, String> item: vars.entrySet()) {
                temp = temp.replaceAll(item.getKey(), Matcher.quoteReplacement(item.getValue()));
            }
        }

        return temp;
    }

    public String generateIndex(long time, Map<String, String> vars) {
        return generate(indexPattern, time, vars);
    }

    public String generateIndex(long time) {
        return generateIndex(time, null);
    }

    public String generateId(long time, Map<String, String> vars) {
        return generate(identityPattern, time, vars);
    }

    public String generateId(long time) {
        return generateId(time, null);
    }


}
