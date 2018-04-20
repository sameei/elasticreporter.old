package me.samei.xtool.elasticsearch_metric_reporter;

public abstract class AbstractFieldFormatter implements FieldFormatter {

    String keyEscapeRegex = "[\\\\|/.\"']";
    String valueEscapeRegex = "[\"\\n\\r]";

    String formatKey(String key) {
        return key.replaceAll(keyEscapeRegex, "_");
    }

    String formatValue(String value) {
        return "\"" + value.replaceAll(valueEscapeRegex, " ") + "\"";
    }

    String apply(String rawKey, String formattedValue) {
        return new StringBuilder()
                .append(" \"")
                .append(formatKey(rawKey))
                .append("\": ")
                .append(formattedValue)
                .toString();
    }

    String apply(String formattedKey, String keyPostfix, String formattedValue) {
        return new StringBuilder()
                .append(" \"")
                .append(formattedKey)
                .append("_")
                .append(keyPostfix)
                .append("\": ")
                .append(formattedValue)
                .toString();
    }

    String apply(String formattedKey, String keyPostfix, long value) {
        return apply(formattedKey, keyPostfix, Long.toString(value));
    }

    String apply(String formattedKey, String keyPostfix, double value) {
        return apply(formattedKey, keyPostfix, Double.toString(value));
    }

    @Override
    public String format(String key, Number num) {
        return apply(key, num.toString());
    }

    @Override
    public String format(String key, String value) {
        return apply(key, formatValue(value));
    }
}
