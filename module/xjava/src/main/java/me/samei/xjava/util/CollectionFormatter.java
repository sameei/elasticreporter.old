package me.samei.xjava.util;

import java.util.Collection;
import java.util.Map;

public class CollectionFormatter {

    public static interface OneKeyOneValue<K, V> {
        String format(K key, V value);
    }

    public static <K, V> void applyKV(
            Map<K, V> data,
            OneKeyOneValue<K, V> formatter,
            StringBuilder buf,
            String sep
    ) {
        for (Map.Entry<K, V> entry : data.entrySet()) {
            buf
                    .append(sep)
                    .append(formatter.format(entry.getKey(), entry.getValue()));
        }
    }

    public static interface OneKeyMultipleValues<K, V> {
        Collection<String> format(K key, V value);
    }

    public static <K, V> void applyKVs(
            Map<K, V> data,
            OneKeyMultipleValues<K, V> formatter,
            StringBuilder buf,
            String sep
    ) {
        for (Map.Entry<K, V> entry : data.entrySet()) {
            for (String str : formatter.format(entry.getKey(), entry.getValue())) {
                buf.append(sep).append(str);
            }
        }
    }

    public static interface OneValue<T> {
        String format(T input);
    }

    public static <T> void applyV(
            Collection<T> data,
            OneValue<T> formatter,
            StringBuilder buf,
            String sep
    ) {
        for (T i : data) {
            buf.append(sep).append(formatter.format(i));
        }
    }

    public static interface MultipleValues<T> {
        Collection<String> format(T input);
    }

    public static <T> void applyVs(
            Collection<T> data,
            MultipleValues<T> formatter,
            StringBuilder buf,
            String sep
    ) {
        for (T i : data) {
            for (String j : formatter.format(i)) {
                buf.append(sep).append(j);
            }
        }
    }


    public final StringBuilder buffer;
    public final String seperator;

    public CollectionFormatter(StringBuilder buf, String sep) {
        this.buffer = buf;
        this.seperator = sep;
    }

    public <K, V> CollectionFormatter applyKV(
            Map<K, V> data,
            OneKeyOneValue<K, V> formatter
    ) {
        applyKV(data, formatter, buffer, seperator);
        return this;
    }

    public <K, V> CollectionFormatter applyKVs(
            Map<K, V> data,
            OneKeyMultipleValues<K, V> formatter
    ) {
        applyKVs(data, formatter, buffer, seperator);
        return this;
    }

    public <T> CollectionFormatter applyV(
            Collection<T> data,
            OneValue<T> formatter
    ) {
        applyV(data, formatter, buffer, seperator);
        return this;
    }

    public <T> CollectionFormatter applyVs(
            Collection<T> data,
            MultipleValues<T> formatter
    ) {
        applyVs(data, formatter, buffer, seperator);
        return this;
    }

    public CollectionFormatter applyStrings(Collection<String> data) {
        for (String i : data) {
            buffer.append(seperator).append(i);
        }
        return this;
    }

    private String _toString;

    @Override
    public String toString() {
        if (_toString == null) {
            _toString = new StringBuilder()
                    .append(getClass().getName())
                    .append("(")
                    .append(seperator)
                    .append(")")
                    .toString();
        }
        return _toString;
    }
}