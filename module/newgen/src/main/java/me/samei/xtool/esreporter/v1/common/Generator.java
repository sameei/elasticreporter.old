package me.samei.xtool.esreporter.v1.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class Generator {

    static protected void append(StringBuilder buf, Value value) {

        buf.append('"').append(value.key).append("\": ");

        switch (value.type) {
            case Simple:
                buf.append(value.value);
                break;
            case Quoted:
                buf.append('"').append(value.value).append('"');
                break;
        }
    }

    static public Report apply(
            String sourceId,
            IndexName index,
            MetaData metadata,
            Formatter formatter,
            long time,
            Collection<Value> values,
            Map<String, String> vars
    ) {

        String indexName = index.generateIndex(time, vars);
        String docId = index.generateId(time, vars);

        ArrayList<Value> all = new ArrayList<>(values);

        all.addAll(metadata.generate(time, indexName, sourceId, formatter));
        all.addAll(metadata.convert(vars, formatter));

        StringBuilder buf = new StringBuilder();

        Iterator<Value> iter = all.iterator();

        buf.append("{ ");
        append(buf, iter.next());

        while (iter.hasNext()) {
            buf.append(", ");
            append(buf, iter.next());
        }

        buf.append("}");

        Report report = new Report(time, indexName, docId, buf.toString());

        return report;
    }
}
