

flink-conf.yaml

```
metrics.reporters: debug, jobtask

metrics.reporter.debug.class: me.samei.xtool.esreporter.v1.flink.reporter.Debugger
metrics.reporter.debug.source-id: single-node
metrics.reporter.debug.es-url: http://localhost:9200
metrics.reporter.debug.index-pattern: single-index
metrics.reporter.debug.identity-pattern: <millis>
metrics.reporter.debug.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.debug.datetime-zone: UTC


metrics.reporter.jobtask.class: me.samei.xtool.esreporter.v1.flink.reporter.JobTask
metrics.reporter.jobtask.source-id: single-node
metrics.reporter.jobtask.es-url: http://localhost:9200
metrics.reporter.jobtask.index-pattern: task-<job_name>-<year>-<month>-<day_of_month>
metrics.reporter.jobtask.identity-pattern: <millis>
metrics.reporter.jobtask.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.jobtask.datetime-zone: UTC


metrics.reporter.jobtask.class: me.samei.xtool.esreporter.v1.flink.reporter.JobTask
metrics.reporter.jobtask.source-id: single-node
metrics.reporter.jobtask.es-url: http://localhost:9200
metrics.reporter.jobtask.index-pattern: task-<job_name>-<year>-<month>-<day_of_month>
metrics.reporter.jobtask.identity-pattern: <millis>-<job_id>-<tm_id>-<task_id>-<operator_id>-<subtask_index>
metrics.reporter.jobtask.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.jobtask.datetime-zone: UTC

```

```
metrics.reporters: eslog-jvm, eslog-jobs, eslog-dirty, debug

metrics.reporter.eslog-jvm.class: me.samei.xtool.esreporter.v1.flink.reporter.JVMStatReporter
metrics.reporter.eslog-jvm.source-id: jobmanager-a1
metrics.reporter.eslog-jvm.es-url: http://localhost:9200
metrics.reporter.eslog-jvm.index-pattern: stage-flink-jvm-t1-jba1-<year>-<month>-<day-of-month>
metrics.reporter.eslog-jvm.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.eslog-jvm.datetime-zone: UTC

metrics.reporter.eslog-jobs.class: me.samei.xtool.esreporter.v1.flink.reporter.JobsReporter
metrics.reporter.eslog-jobs.source-id: jobmanager-a1
metrics.reporter.eslog-jobs.es-url: http://localhost:9200
metrics.reporter.eslog-jobs.index-pattern: stage-flink-job-t1-jba1-<year>-<month>-<day-of-month>
metrics.reporter.eslog-jobs.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.eslog-jobs.datetime-zone: UTC


metrics.reporter.eslog-dirty.class: me.samei.xtool.esreporter.v1.flink.reporter.BulkReporter
metrics.reporter.eslog-dirty.source-id: jobmanager-a1
metrics.reporter.eslog-dirty.es-url: http://localhost:9200
metrics.reporter.eslog-dirty.index-pattern: stage-flink-dirty-jba1-<year>-<month>-<day-of-month>
metrics.reporter.eslog-dirty.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.eslog-dirty.datetime-zone: UTC

metrics.reporter.eslog-dirty.class: me.samei.xtool.esreporter.v1.flink.reporter.Debugger
metrics.reporter.eslog-dirty.source-id: jobmanager-a1
metrics.reporter.eslog-dirty.es-url: http://localhost:9200
metrics.reporter.eslog-dirty.index-pattern: single-index
metrics.reporter.eslog-dirty.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.eslog-dirty.datetime-zone: UTC

metric.reporter.debug.class: me.samei.xtool.esreporter.v1.flink.reporter.Slf4jDebugger
metric.reporter.debug.logger-name: MetricDebugger
```

```
conf-path.source-id: SOURCE_ID
conf-path.es-url: http://localhost:9200
conf-path.index-pattern: <source-id>-<year>-<month>-<day-of-month>
conf-path.datetime-pattern: yyyy-MM-dd HH:mm:ss
conf-path.datetime-zone: UTC
```


```
metric.reporter.debug.class: me.samei.xtool.elasticsearch_metric_reporter.Debugger
metric.reporter.debug.logger-name: MetricDebugger

```



