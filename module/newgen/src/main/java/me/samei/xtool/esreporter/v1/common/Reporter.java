package me.samei.xtool.esreporter.v1.common;

import java.io.IOException;
import java.util.Collection;

public class Reporter {

    static public String metaFieldsPrefix = "@meta";


    static public String metaIndexNameKey = metaFieldsPrefix + ".index.name";
    static public String metaIndexTimeMillisKey = metaFieldsPrefix + ".time.millis";
    static public String metaIndexDateTimeKey = metaFieldsPrefix + ".time.format";
    static public String metaSourceIdKey = metaFieldsPrefix + ".source.id";

    protected final ElasticSearch es;
    protected final IndexName index;
    protected final MetaData metadata;

    public Reporter(
            ElasticSearch es,
            IndexName index,
            MetaData metadata,
            boolean debug
    ) {
        this.es = es;
        this.index = index;
        this.metadata = metadata;
    }

    public String generate(long time, Collection<Value> values, Formatter formatter) throws IOException {

        String indexName = index.generate(time);

        values.addAll(metadata.generate(time, indexName, "", formatter));



        StringBuilder buf = new StringBuilder();

        buf.append("{ ");
        appendMeta(time, indexName, formatter, buf);

        for (Value value: values) {
            buf.append(", \"").append(value.key).append("\": ");
            switch (value.type) {
                case Simple: buf.append(value.value);
                case Quoted: buf.append('"').append(value).append('"');
            }
        }

        buf.append("}");

        return buf.toString();
    }

    protected void appendMeta(long time, String indexName, Formatter formatter, StringBuilder buf) {

        Value a = formatter.formatString(metaIndexNameKey, indexName);
        Value b = formatter.formatNum(metaIndexTimeMillisKey, time);

        buf.append('"')
                .append(a.key)
                .append("\": \"")
                .append(a.value)
                .append("\", \"")
                .append(b.key)
                .append("\": ")
                .append(b.value)
        ;
    }

}
