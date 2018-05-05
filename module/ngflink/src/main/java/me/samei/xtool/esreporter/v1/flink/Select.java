package me.samei.xtool.esreporter.v1.flink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Select {

    public String apply(String id);

    public static class MatchByEnd implements Select {

        private final String[] tokens;

        private final String name;

        private final Logger logger = LoggerFactory.getLogger(getClass());

        public MatchByEnd(String[] tokens, String name) {
            this.tokens = tokens;
            this.name = name;
        }

        public String apply(String id) {
            for(String str: tokens) {
                if (id.endsWith(str)) {
                    if (logger.isTraceEnabled()) logger.trace("{}, Matched, Token: {}, ID: {}", name, str, id);
                    return str;
                }
            }
            if (logger.isTraceEnabled()) logger.trace("{}, NotMatched, ID: {}", name, id);
            return null;
        }


        private String _toString;
        @Override
        public String toString() {
            if (_toString == null) {
                StringBuilder builder = new StringBuilder()
                        .append(getClass().getName())
                        .append("(tokens: [");
                for(String s: tokens) { builder.append(s).append(','); }
                builder.append("], name: ").append(name).append(")");
                _toString = builder.toString();
            }
            return _toString;
        }
    }

    public static class AcceptAll implements Select {

        @Override
        public String apply(String id) { return id; }

        @Override
        public String toString() { return getClass().getName(); }
    }

}
