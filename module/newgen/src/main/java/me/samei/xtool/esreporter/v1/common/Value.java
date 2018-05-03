package me.samei.xtool.esreporter.v1.common;

import java.util.Collection;

public class Value {

    public static enum Type {
        Simple,
        Quoted
    }

    public final String key;
    public final String value;
    public final Type type;
    public final Collection<String> tags;

    public Value(
            String key,
            String value,
            Type type,
            Collection<String> tags
    ) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.tags = tags;
    }

    public Value append(String tag) {
        tags.add(tag);
        return this;
    }

    public Value remove(String tag) {
        tags.remove(tag);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(getClass().getName())
                .append("( key: '").append(key)
                .append("', value: '").append(value)
                .append("', type: ").append(type)
                .append("', tags: [");

        if (!tags.isEmpty()) {
            buf.append('\'');
            for (String tag: tags) buf.append(tag).append("','");
            buf.append('\'');
        }

        return buf.append("])").toString();
    }

}

