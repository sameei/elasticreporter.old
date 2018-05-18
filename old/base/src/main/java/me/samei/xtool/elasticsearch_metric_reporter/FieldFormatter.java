package me.samei.xtool.elasticsearch_metric_reporter;

public interface FieldFormatter {

    public String format(String key, java.lang.Number num);

    public String format(String key, String value);

}
