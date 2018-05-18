package me.samei.xtool.elasticsearch_metric_reporter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public interface IndexGenerator {

    public String indexForTime(long millis);

    public static IndexGenerator weekly(String prefix, String zone) {

        DateTimeFormatter formatter =
                (new DateTimeFormatterBuilder())
                        .parseCaseInsensitive()
                        .appendLiteral("y")
                        .appendValue(ChronoField.YEAR, 4)
                        .appendLiteral("-m")
                        .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                        .appendLiteral("-w")
                        .appendValue(ChronoField.ALIGNED_WEEK_OF_MONTH, 1)
                        .toFormatter().withZone(ZoneId.of(zone));

        return new ImplV1(prefix, formatter,"y$$$$-m$$-w$");
    }

    public static IndexGenerator weekly(String prefix) { return weekly(prefix, "UTC"); }

    public static IndexGenerator daily(String prefix, String zone) {

        DateTimeFormatter formatter =
                (new DateTimeFormatterBuilder())
                        .parseCaseInsensitive()
                        .appendLiteral("y")
                        .appendValue(ChronoField.YEAR, 4)
                        .appendLiteral("-m")
                        .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                        .appendLiteral("-d")
                        .appendValue(ChronoField.DAY_OF_MONTH, 2)
                        .toFormatter().withZone( ZoneId.of(zone) );

        return new ImplV1(prefix, formatter, "y$$$$-m$$-d$$");
    }
    public static IndexGenerator daily(String prefix) { return daily(prefix, "UTC"); }

    public static class ImplV1 implements IndexGenerator {
        private final String prefix;
        private final DateTimeFormatter datetimeFormatter;
        private final String formatShow;

        public ImplV1(String prefix, DateTimeFormatter frmt, String formatShow) {
            if (prefix == null) throw new IllegalArgumentException("'prefix' can't be null");
            if (prefix.length() == 0) throw new IllegalArgumentException("'prefix' can't be empty");
            if (frmt == null) throw new IllegalArgumentException("'formatter' can't be null");
            if (formatShow == null) throw new IllegalArgumentException("'formatShow' can't be null");
            if (formatShow.length() == 0) throw new IllegalArgumentException("'formatShow' can't be empty");
            this.prefix = prefix;
            this.datetimeFormatter = frmt;
            this.formatShow = formatShow;
        }

        @Override
        public String indexForTime(long millis) {
            Instant instant = Instant.ofEpochMilli(millis);

            return new StringBuilder().append(prefix).append("-")
                    .append(datetimeFormatter.format(instant))
                    .toString();
        }

        private String _toString;
        @Override
        public String toString() {
            if (_toString == null) {
                _toString = new StringBuilder()
                        .append(getClass().getName())
                        .append("(prefix: '")
                        .append(prefix)
                        .append("', formatter: '")
                        .append(formatShow)
                        .append("','")
                        .append(datetimeFormatter.toString())
                        .append("')")
                        .toString();
            }
            return _toString;
        }
    }
}

