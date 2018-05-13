package me.samei.xtool.esreporter.v1.flink.util;

import org.apache.flink.metrics.Metric;
import org.apache.flink.metrics.MetricGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public interface Select {

    public String apply(Metric metric, String name, MetricGroup group);

    public static class MatchByEnd implements Select {

        private final String[] tokens;

        private final String name;

        private final Logger logger = LoggerFactory.getLogger(getClass());

        public MatchByEnd(String[] tokens, String name) {
            this.tokens = tokens;
            this.name = name;
        }

        public String apply(Metric metric, String name, MetricGroup group) {
            return apply(group.getMetricIdentifier(name));
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

        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public String apply(Metric metric, String name, MetricGroup group) {
            String id = group.getMetricIdentifier(name);
            if (logger.isTraceEnabled()) logger.trace("{}, Matched, ID: {}", name, id);
            return id;
        }

        @Override
        public String toString() { return getClass().getName(); }
    }

    public static class CheckForVariables implements Select {

        private final String[] vars;

        private final String name;

        private final boolean returnName;

        private final Logger logger = LoggerFactory.getLogger(getClass());

        public CheckForVariables(String[] vars, String name, boolean returnName) {
            this.vars = vars;
            this.name = name;
            this.returnName = returnName;
        }

        public boolean check(String name) {
            for(String var: vars) {
                if (name.equals(var)) return true;
            }
            return false;
        }

        @Override
        public String apply(Metric metric, String metricName, MetricGroup group) {
            for (Map.Entry<String, String> var: group.getAllVariables().entrySet()) {
                if (check(metricName)) {
                    String id = (!returnName || logger.isTraceEnabled()) ? group.getMetricIdentifier(metricName) : null;
                    if (logger.isTraceEnabled()) {
                        logger.trace("{}, Matched, Name: {}, ID: {}", name, metricName, id);
                    }
                    return returnName ? metricName : id;
                }
            }
            return null;
        }


        private String _toString;
        @Override
        public String toString() {
            if (_toString == null) {
                StringBuilder builder = new StringBuilder()
                        .append(getClass().getName()).append("(")
                        .append(" keys: [");

                for (String var: vars) {
                    builder.append(var).append(", ");
                }

                builder.append("]")
                        .append(", name: ").append(name)
                        .append(")").toString();

                _toString = builder.toString();
            }

            return _toString;
        }

        static public String[] JOB_TASK_VARS = {"<job_name>", "<task_name>", "<subtask_index>"};
        static public CheckForVariables forJobTask(String name, boolean returnName) {
            return new CheckForVariables(JOB_TASK_VARS,name, returnName);
        }

        static public String[] JOB_OPTS_VARS = {"<job_name>", "<operator_name>", "<subtask_index>"};
        static public CheckForVariables forJobOpts(String name, boolean returnName) {
            return new CheckForVariables(JOB_OPTS_VARS,name, returnName);
        }
    }
}
