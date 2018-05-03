package me.samei.xtool.esreporter.v1.common;

import com.sun.istack.internal.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

public class MetaData {

    public static String defualtMetaFieldPrefix = "@meta";
    public static String defaultTimeMillisKey = "time.millis";
    public static String defaultDateTimeKey = "time.format";
    public static String defaultSourceIdKey = "source.id";
    public static String defaultIndexKey = "index.name";

    public static DateTimeFormatter datetime(String pattern, String zone) {
        return DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.of(zone));
    }

    public static DateTimeFormatter simpleUTC() {
        return datetime("yyyy-MM-dd HH:mm:ss", "UTC");
    }

    public final String prefix;
    public final String timeMillisKey;
    public final String datetimeKey;
    public final String sourceIdKey;
    public final String indexNameKey;
    public final DateTimeFormatter datetimeFormatter;

    public MetaData(
            String prefix,
            String timeMillisKey,
            String datetimeKey,
            String sourceIdKey,
            String indexNameKey,
            DateTimeFormatter datetimeFormatter
    ) {

        this.prefix = prefix;
        this.timeMillisKey = prefix + "." + timeMillisKey;
        this.datetimeKey = prefix + "." + datetimeKey;
        this.sourceIdKey = prefix + "." + sourceIdKey;
        this.indexNameKey = prefix + "." + indexNameKey;
        this.datetimeFormatter = datetimeFormatter;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(getClass().getName())
                .append("(")
                .append("prefix: '").append(prefix)
                .append("', timeMillisKey: '").append(timeMillisKey)
                .append("', datetimeKey: '").append(datetimeKey)
                .append("', sourceIdKey: '").append(sourceIdKey)
                .append("', indexNameKey: '").append(indexNameKey)
                .append("')")
                .toString();
    }


    /*public final String pattern;
    public final DateTimeFormatter dateTimeFormatter;*/


    public Collection<Value> generate(
            long time,
            String index,
            String sourceId,
            Formatter formatter
    ) {

        ArrayList<Value> values = new ArrayList<>();

        values.add(formatter.formatNum(timeMillisKey, time));

        Instant inst = Instant.ofEpochMilli(time);
        String raw = datetimeFormatter.format(inst);
        values.add(formatter.formatString(datetimeKey, raw));

        values.add(formatter.formatString(indexNameKey, index));

        values.add(formatter.formatString(sourceIdKey, sourceId));

        return values;
    }

}
