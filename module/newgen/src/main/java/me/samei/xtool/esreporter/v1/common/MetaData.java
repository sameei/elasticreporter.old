package me.samei.xtool.esreporter.v1.common;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

public class MetaData {

    public static String defualtMetaFieldPrefix = "@meta";
    public static String defaultMetaFieldPrefix = defualtMetaFieldPrefix;
    public static String defaultTimeMillisKey = "time.millis";
    public static String defaultDateTimeKey = "time.format";
    public static String defaultSourceIdKey = "source.id";
    public static String defaultIndexKey = "index.name";



    public final String prefix;
    public final String timeMillisKey;
    public final String datetimeKey;
    public final String sourceIdKey;
    public final String indexNameKey;
    public final DateTimeFormatter datetimeFormatter;

    public static MetaData defaultInstance() {
        return new MetaData(
                defualtMetaFieldPrefix,
                defaultTimeMillisKey,
                defaultDateTimeKey,
                defaultSourceIdKey,
                defaultIndexKey,
                me.samei.xjava.util.DateTime.formatterOfSimpleUTC()
        );
    }

    public static MetaData defaultInstance(String pattern, String zoneId) {
        return new MetaData(
                defualtMetaFieldPrefix,
                defaultTimeMillisKey,
                defaultDateTimeKey,
                defaultSourceIdKey,
                defaultIndexKey,
                DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.of(zoneId))
        );
    }

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
                .append("', datetimeFormatter: '").append(datetimeFormatter)
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
