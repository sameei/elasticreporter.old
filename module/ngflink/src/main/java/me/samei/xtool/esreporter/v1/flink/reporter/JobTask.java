package me.samei.xtool.esreporter.v1.flink.reporter;

import org.apache.flink.metrics.Metric;
import org.apache.flink.metrics.MetricGroup;

import java.util.Map;

public class JobTask extends GroupsAbstract {

    private final String SYM_JOB_NAME = "<job_name>";
    private final String SYM_TASK_NAME = "<task_name>";
    private final String SYM_SUBTASK_INDEX = "<subtask_index>";

    protected String[] keys = {
            SYM_JOB_NAME,
            SYM_TASK_NAME,
            SYM_SUBTASK_INDEX
    };

    @Override
    public String select(Metric metric, String name, MetricGroup group) {
        Map<String, String> vars = group.getAllVariables();
        logger.debug("VARS: {}", vars.size());
        if (vars.size() < keys.length) return null;
        for(String key: keys) {
            logger.debug("KEY: '{}', VAR: '{}'", key, vars.get(key));
            if (!vars.containsKey(key)) return null;
            if (vars.get(key) == null) return null;
            if (vars.get(key).isEmpty()) return null;
        }
        return name;
    }

    @Override
    public String identity(Metric metric, String metricName, MetricGroup group) {
        Map<String, String> vars = group.getAllVariables();
        logger.debug("KEY: '{}', VAR: '{}'", SYM_JOB_NAME, vars.get(SYM_JOB_NAME));
        logger.debug("KEY: '{}', VAR: '{}'", SYM_TASK_NAME, vars.get(SYM_TASK_NAME));
        logger.debug("KEY: '{}', VAR: '{}'", SYM_SUBTASK_INDEX, vars.get(SYM_SUBTASK_INDEX));
        String result = vars.get(SYM_JOB_NAME) + "." + vars.get(SYM_TASK_NAME) + "." + vars.get(SYM_SUBTASK_INDEX);
        logger.debug("RESULT: {}", result);
        return result;
    }
}
