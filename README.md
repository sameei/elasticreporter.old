

flink-conf.yaml

```
metrics.reporters: eslog-jvm, eslog-jobs, eslog-dirty, debug

metrics.reporter.eslog-jvm.class: me.samei.xtool.esreporter.v1.flink.JVMReporter
metrics.reporter.eslog-jvm.source-id: jobmanager-a1
metrics.reporter.eslog-jvm.es-url: http://localhost:9200
metrics.reporter.eslog-jvm.index-pattern: stage-flink-jvm-t1-jba1-<year>-<month>-<day-of-month>
metrics.reporter.eslog-jvm.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.eslog-jvm.datetime-zone: UTC

metrics.reporter.eslog-jobs.class: me.samei.xtool.esreporter.v1.flink.JobsReporter
metrics.reporter.eslog-jobs.source-id: jobmanager-a1
metrics.reporter.eslog-jobs.es-url: http://localhost:9200
metrics.reporter.eslog-jobs.index-pattern: stage-flink-job-t1-jba1-<year>-<month>-<day-of-month>
metrics.reporter.eslog-jobs.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.eslog-jobs.datetime-zone: UTC


metrics.reporter.eslog-dirty.class: me.samei.xtool.esreporter.v1.flink.BulkReporter
metrics.reporter.eslog-dirty.source-id: jobmanager-a1
metrics.reporter.eslog-dirty.es-url: http://localhost:9200
metrics.reporter.eslog-dirty.index-pattern: stage-flink-dirty-jba1-<year>-<month>-<day-of-month>
metrics.reporter.eslog-dirty.datetime-pattern: yyyy-MM-dd HH:mm:ss
metrics.reporter.eslog-dirty.datetime-zone: UTC

metric.reporter.debug.class: me.samei.xtool.esreporter.v1.flink.Debugger
metric.reporter.debug.logger-name: MetricDebugger

```

```
metric.reporter.debug.class: me.samei.xtool.elasticsearch_metric_reporter.Debugger
metric.reporter.debug.logger-name: MetricDebugger

```



