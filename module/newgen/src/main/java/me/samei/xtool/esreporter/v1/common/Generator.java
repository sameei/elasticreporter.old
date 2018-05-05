package me.samei.xtool.esreporter.v1.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class Generator {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final IndexName index;
    protected final MetaData metadata;
    protected final String sourceId;

    public Generator(
            String sourceId,
            IndexName index,
            MetaData metadata
    ) {
        this.sourceId = sourceId;
        this.index = index;
        this.metadata = metadata;
    }

    public Report generate(long time, Collection<Value> values, Formatter formatter) {

        String indexName = index.generate(time);

        ArrayList<Value> all = new ArrayList<>(values);

        all.addAll(metadata.generate(time, indexName, sourceId, formatter));

        if (all.size() == 0) return new Report(time, indexName, "{}");
        else {

            StringBuilder buf = new StringBuilder();

            Iterator<Value> iter = all.iterator();

            buf.append("{ ");
            append(buf, iter.next());

            while(iter.hasNext()) {
                buf.append(", ");
                append(buf, iter.next());
            }

            buf.append("}");

            Report report = new Report(time, indexName, buf.toString());

            logger.warn("Generate, {}", report);

            return report;
        }
    }

    protected void append(StringBuilder buf, Value value) {

        buf.append('"').append(value.key).append("\": ");

        switch (value.type) {
            case Simple: buf.append(value.value); break;
            case Quoted: buf.append('"').append(value.value).append('"'); break;
        }
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(getClass().getName())
                .append("(sourceId: '").append(sourceId)
                .append("', indexName: ").append(index)
                .append(", metadata: ").append(metadata)
                .append(")")
                .toString();
    }

}
