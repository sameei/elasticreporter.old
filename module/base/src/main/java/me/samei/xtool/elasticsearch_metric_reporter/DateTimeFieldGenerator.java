package me.samei.xtool.elasticsearch_metric_reporter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

public interface DateTimeFieldGenerator {

    public Collection<String> generate(long millis, FieldFormatter formatter);

    public static class ImplV1 implements DateTimeFieldGenerator {

        private final String timestampKey = "@timestamp";
        private final String datetimeKey = "@datetime";
        private final DateTimeFormatter datetimeFormattter =
                DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of("UTC"));

        @Override
        public Collection<String> generate(long millis, FieldFormatter formatter) {

            ArrayList<String> list = new ArrayList<>(2);

            list.add(formatter.format(timestampKey, millis));

            Instant temp = Instant.ofEpochMilli(millis);
            String date = datetimeFormattter.format(temp).replace("[UTC]", "");
            list.add(formatter.format(datetimeKey, date));

            return list;
        }

        private String _toString;

        @Override
        public String toString() {
            if (_toString == null) {
                _toString = new StringBuilder()
                        .append(getClass().getName())
                        .append("(")
                        .append(timestampKey)
                        .append(",")
                        .append(datetimeKey)
                        .append(", UTC")
                        .append(")")
                        .toString();

            }
            return _toString;
        }

    }

}

